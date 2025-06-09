/*
 * Copyright (C) 2021 - 2025 by Sebastian Forster, Stefan Rothe
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
import ch.kinet.ICalendar;
import ch.kinet.Json;
import ch.kinet.JsonArray;
import ch.kinet.JsonObject;
import java.util.stream.Stream;

public class Response {

    private static final String JSON_RESULT = "result";

    public enum ContentType {
        None, File, Image, Text
    };
    private final int status;
    private final Data body;

    public static Response badRequest() {
        return new Response(Status.BAD_REQUEST);
    }

    public static Response badRequest(String message) {
        return new Response(Status.BAD_REQUEST, Data.text(message));
    }

    public static Response created() {
        return new Response(Status.CREATED);
    }

    public static Response createdJsonTerse(Json object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter 'object' is null.");
        }

        JsonObject root = JsonObject.create();
        root.putTerse(JSON_RESULT, object);
        return new Response(Status.CREATED, Data.json(root));
    }

    public static Response createdJsonVerbose(Json object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter 'object' is null.");
        }

        JsonObject root = JsonObject.create();
        root.putVerbose(JSON_RESULT, object);
        return new Response(Status.CREATED, Data.json(root));
    }

    public static Response file(Data data) {
        return new Response(Status.OK, data);
    }

    public static Response forbidden() {
        return new Response(Status.FORBIDDEN);
    }

    public static Response ical(ICalendar data, String fileName) {
        return new Response(Status.OK, Data.ical(data, fileName));
    }

    public static Response internalServerError() {
        return new Response(Status.INTERNAL_SERVER_ERROR);
    }

    public static Response internalServerError(String message) {
        return new Response(Status.INTERNAL_SERVER_ERROR, Data.text(message));
    }

    public static Response json(JsonObject result) {
        if (result == null) {
            return notFound();
        }

        JsonObject root = JsonObject.create();
        root.put(JSON_RESULT, result);
        return new Response(Status.OK, Data.json(root));
    }

    public static Response jsonArray(JsonArray array) {
        JsonObject root = JsonObject.create();
        root.put(JSON_RESULT, array);
        return new Response(Status.OK, Data.json(root));
    }

    public static Response jsonArray(Stream<JsonObject> stream) {
        JsonObject root = JsonObject.create();
        root.put(JSON_RESULT, JsonArray.create(stream));
        return new Response(Status.OK, Data.json(root));
    }

    public static Response jsonArrayTerse(Stream<? extends Json> stream) {
        return jsonArray(stream.map(Json::toJsonTerse));
    }

    public static Response jsonArrayVerbose(Stream<? extends Json> stream) {
        return jsonArray(stream.map(Json::toJsonVerbose));
    }

    public static Response jsonTerse(Json result) {
        if (result == null) {
            return notFound();
        }

        JsonObject root = JsonObject.create();
        root.putTerse(JSON_RESULT, result);
        return new Response(Status.OK, Data.json(root));
    }

    public static Response jsonVerbose(Json result) {
        if (result == null) {
            return notFound();
        }

        JsonObject root = JsonObject.create();
        root.putVerbose(JSON_RESULT, result);
        return new Response(Status.OK, Data.json(root));
    }

    public static Response methodNotAllowed() {
        return new Response(Status.METHOD_NOT_ALLOWED);
    }

    public static Response notFound() {
        return new Response(Status.NOT_FOUND);
    }

    public static Response noContent() {
        return new Response(Status.NO_CONTENT);
    }

    public static Response ok() {
        return new Response(Status.OK);
    }

    public static Response unauthorized() {
        return new Response(Status.UNAUTHORIZED);
    }

    public static Response unauthorized(String message) {
        return new Response(Status.UNAUTHORIZED, Data.text(message));
    }

    public static Response unsupportedMediaType() {
        return new Response(Status.UNSUPPORTED_MEDIA_TYPE);
    }

    private Response(int status) {
        this(status, Data.empty());
    }

    private Response(int status, Data body) {
        this.body = body;
        this.status = status;
    }

    public Data getBody() {
        return body;
    }

    public int getStatus() {
        return status;
    }
}
