/*
 * Copyright (C) 2022 - 2025 by Sebastian Forster, Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet.http;

import ch.kinet.Data;
import ch.kinet.JsonObject;
import ch.kinet.Util;
import com.google.gson.JsonSyntaxException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.PrematureJwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.xnio.Options;

public final class Server<T> implements HttpHandler {

    private static final String MIME_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_PATCH = "PATCH";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final Map<String, String> defaultHeaders = createDefaultHeaders();
    private static final Set<String> acceptedContentTypes = createAcceptedContentTypes();
    private final ServerImplementation<T> serverImplementation;

    public static <T> void start(int port, int ioThreads, int workerThreads,
                                 ServerImplementation<T> requestHandler) {
        Undertow server = Undertow.builder()
            .setWorkerThreads(port)
            .setWorkerOption(Options.WORKER_IO_THREADS, ioThreads)
            .setWorkerOption(Options.WORKER_TASK_CORE_THREADS, workerThreads)
            .setWorkerOption(Options.WORKER_TASK_MAX_THREADS, workerThreads)
            .addHttpListener(port, "localhost")
            .setHandler(new Server(requestHandler))
            .build();
        server.start();
    }

    private static Set<String> createAcceptedContentTypes() {
        Set<String> result = new HashSet<>();
        result.add(Data.MIME_TYPE_JSON);
        result.add(Data.MIME_TYPE_JSON);
        return result;
    }

    private static Map<String, String> createDefaultHeaders() {
        Map<String, String> result = new HashMap<>();
        result.put("Access-Control-Allow-Headers", "Authorization,Content-Type");
        result.put("Access-Control-Allow-Methods", "DELETE,GET,PATCH,POST,PUT");
        result.put("Access-Control-Allow-Origin", "*");
        result.put("Access-Control-Expose-Headers", "Content-Disposition");
        result.put("Access-Control-Max-Age", "0");
        result.put("Content-Security-Policy", "base-uri 'none'; connect-src 'none'; default-src 'none'; form-action 'none'; frame-ancestors 'none'; script-src 'none'");
        result.put("Strict-Transport-Security", "max-age=15552000; includeSubDomains; preload");
        return result;
    }

    private Server(ServerImplementation<T> requestHandler) {
        this.serverImplementation = requestHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // handle requests in separate threads
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        exchange.startBlocking();
        Response response = null;
        T authorisation = null;

        // check authorisation
        String authorisationHeader = exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
        if (Util.startsWith(authorisationHeader, "Bearer ")) {
            String token = authorisationHeader.substring(7);
            try {
                Jws<Claims> jws = Jwts.parserBuilder().setSigningKeyResolver(new SigningKeyResolverAdapter() {

                    @Override
                    public Key resolveSigningKey(JwsHeader header, Claims claims) {
                        return serverImplementation.getSigningKey(header.getKeyId());
                    }
                }).build().parseClaimsJws(token);
                authorisation = serverImplementation.checkAuthorisation(jws.getBody());
                if (authorisation == null) {
                    response = Response.unauthorized();
                }
            }
            catch (PrematureJwtException ex) {
                response = Response.unauthorized("Token is not yet valid.");
            }
            catch (ExpiredJwtException ex) {
                response = Response.unauthorized("Token has expired.");
            }
            catch (MalformedJwtException | IllegalArgumentException | JsonSyntaxException ex) {
                response = Response.unauthorized("Invalid token.");
            }
            catch (UnsupportedJwtException ex) {
                response = Response.unauthorized("Unsupported token type.");
            }
            catch (SignatureException ex) {
                response = Response.unauthorized("Invalid token signature.");
            }
        }
        else {
            authorisation = serverImplementation.publicAuthorisation();
        }

        if (authorisation != null) {
            response = processRequest(authorisation, exchange);
        }

        translateResponse(exchange, response);
        exchange.endExchange();
    }

    private Response processRequest(T authorisation, HttpServerExchange exchange) {
        try {
            String method = exchange.getRequestMethod().toString();
            switch (method) {
                case METHOD_DELETE:
                    return processDelete(authorisation, exchange);
                case METHOD_GET:
                    return processGet(authorisation, exchange);
                case METHOD_OPTIONS:
                    return Response.ok();
                case METHOD_PATCH:
                    return processRequestWithBody(authorisation, exchange, Request.Method.Patch);
                case METHOD_POST:
                    return processRequestWithBody(authorisation, exchange, Request.Method.Post);
                case METHOD_PUT:
                    return processRequestWithBody(authorisation, exchange, Request.Method.Put);
                default:
                    return Response.methodNotAllowed();
            }

        }
        catch (RuntimeException ex) {
            serverImplementation.handleException(ex);
            return Response.internalServerError();
        }
    }

    private Response processDelete(T authorisation, HttpServerExchange exchange) {
        Request r = Request.createDelete(authorisation, exchange.getRequestPath(), parseQuery(exchange));
        return serverImplementation.handleRequest(r);
    }

    private Response processGet(T authorisation, HttpServerExchange exchange) {
        Request r = Request.createGet(authorisation, exchange.getRequestPath(), parseQuery(exchange));
        return serverImplementation.handleRequest(r);
    }

    private Data extractFirstFile(HttpServerExchange exchange) {
        FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
        try {
            FormData form = parser.parseBlocking();
            for (String name : form) {
                for (FormData.FormValue value : form.get(name)) {
                    if (value.isFileItem()) {
                        return Data.file(value.getFileItem().getInputStream(), value.getFileName());
                    }
                }
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return null;
    }

    private Response processRequestWithBody(T authorisation, HttpServerExchange exchange, Request.Method method) {
        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
        if (Util.isEmpty(contentType)) {
            return Response.unsupportedMediaType();
        }

        Data body;
        if (contentType.startsWith(MIME_MULTIPART_FORM_DATA)) {
            body = extractFirstFile(exchange);
            if (body == null) {
                return Response.badRequest("Invalid form data");
            }
        }
        else {
            int pos = contentType.indexOf(';');
            if (pos >= 0) {
                contentType = contentType.substring(0, pos);
            }

            if (!acceptedContentTypes.contains(contentType)) {
                return Response.unsupportedMediaType();
            }

            body = parseBody(exchange, contentType);
            if (body == null) {
                return Response.internalServerError();
            }
        }

        Request<T> r = Request.withBody(method, authorisation, exchange.getRequestPath(), parseQuery(exchange), body);
        return serverImplementation.handleRequest(r);
    }

    private Data parseBody(HttpServerExchange exchange, String contentType) {
        try {
            switch (contentType) {
                case Data.MIME_TYPE_JSON:
                    return parseJsonBody(exchange);
                case Data.MIME_TYPE_TEXT:
                    return parseTextBody(exchange);
                default:
                    return parseBinaryBody(exchange, contentType);
            }
        }
        catch (IOException | RuntimeException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Data parseBinaryBody(HttpServerExchange exchange, String contentType) throws IOException {
        return Data.binary(exchange.getInputStream(), null, contentType);
    }

    private Data parseJsonBody(HttpServerExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return Data.json(JsonObject.create(builder.toString()));
    }

    private Data parseTextBody(HttpServerExchange exchange) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return Data.text(builder.toString());
    }

    private Query parseQuery(HttpServerExchange exchange) {
        Map<String, String[]> queryParameters = new HashMap<>();
        for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            queryParameters.put(entry.getKey(), entry.getValue().toArray(new String[0]));
        }

        return Query.create(queryParameters);
    }

    private void translateResponse(HttpServerExchange exchange, Response response) {
        HeaderMap headers = exchange.getResponseHeaders();
        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            headers.add(new HttpString(entry.getKey()), entry.getValue());
        }

        Data body = response.getBody();
        exchange.setStatusCode(response.getStatus());
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, body.mimeType());
        String fileName = body.fileName();
        if (fileName != null) {
            exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, createContentDispositionHeader(fileName));
        }
        try {
            if (!body.isEmpty()) {
                exchange.getOutputStream().write(body.toBytes());
            }
        }
        catch (IOException ex) {
            exchange.setStatusCode(Status.INTERNAL_SERVER_ERROR);
            exchange.endExchange();
            return;
        }

        // required!
        exchange.endExchange();
    }

    private static String encodeURIComponent(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            return s;
        }

    }

    private String createContentDispositionHeader(String fileName) {
        StringBuilder result = new StringBuilder("attachment; filename*=UTF-8''");
        result.append(encodeURIComponent(fileName));
        return result.toString();
    }
}
