/*
 * Copyright (C) 2022 - 2023 by Sebastian Forster, Stefan Rothe
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
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Server implements HttpHandler {

    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_PATCH = "PATCH";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final Map<String, String> defaultHeaders = createDefaultHeaders();
    private static final Set<String> acceptedContentTypes = createAcceptedContentTypes();
    private final RequestHandler requestHandler;

    public static void start(int port, RequestHandler requestHandler) {
        Undertow server = Undertow.builder()
            .addHttpListener(port, "localhost")
            .setHandler(new Server(requestHandler))
            .build();
        server.start();
    }

    private static Set<String> createAcceptedContentTypes() {
        Set<String> result = new HashSet<>();
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

    private Server(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        exchange.startBlocking();
        Response response;
        try {
            String method = exchange.getRequestMethod().toString();
            switch (method) {
                case METHOD_DELETE:
                    response = handleDelete(exchange);
                    break;
                case METHOD_GET:
                    response = handleGet(exchange);
                    break;
                case METHOD_OPTIONS:
                    response = Response.ok();
                    break;
                case METHOD_PATCH:
                    response = handleRequestWithBody(exchange, Request.Method.Patch);
                    break;
                case METHOD_POST:
                    response = handleRequestWithBody(exchange, Request.Method.Post);
                    break;
                case METHOD_PUT:
                    response = handleRequestWithBody(exchange, Request.Method.Put);
                    break;
                default:
                    response = Response.methodNotAllowed();
                    break;
            }

        }
        catch (RuntimeException ex) {
            requestHandler.handleException(ex);
            response = Response.internalServerError();
        }

        translateResponse(exchange, response);
        exchange.endExchange();
    }

    private Response handleDelete(HttpServerExchange exchange) {
        Request r = Request.createDelete(parseAuthorisation(exchange), exchange.getRequestPath(), parseQuery(exchange));
        return requestHandler.handleRequest(r);
    }

    private Response handleGet(HttpServerExchange exchange) {
        Request r = Request.createGet(parseAuthorisation(exchange), exchange.getRequestPath(), parseQuery(exchange));
        return requestHandler.handleRequest(r);
    }

    private Response handleRequestWithBody(HttpServerExchange exchange, Request.Method method) {
        String contentType = exchange.getRequestHeaders().getFirst(Headers.CONTENT_TYPE);
        int pos = contentType.indexOf(';');
        if (pos >= 0) {
            contentType = contentType.substring(0, pos);
        }

        if (!acceptedContentTypes.contains(contentType)) {
            return Response.unsupportedMediaType();
        }

        Data body = parseBody(exchange, contentType);
        if (body == null) {
            return Response.internalServerError();
        }

        Request r = Request.withBody(method, parseAuthorisation(exchange), exchange.getRequestPath(), body);
        return requestHandler.handleRequest(r);
    }

    private String parseAuthorisation(HttpServerExchange exchange) {
        return exchange.getRequestHeaders().getFirst(Headers.AUTHORIZATION);
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

    private String createContentDispositionHeader(String fileName) {
        StringBuilder result = new StringBuilder("attachment; filename=\"");
        result.append(fileName);
        result.append("\"");
        return result.toString();
    }
}
