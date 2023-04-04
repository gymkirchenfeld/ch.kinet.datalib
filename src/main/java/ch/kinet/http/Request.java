/*
 * Copyright (C) 2021 - 2023 by Sebastian Forster, Stefan Rothe
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

import ch.kinet.JsonObject;

public final class Request {

    public enum Method {
        Delete, Get, Post, Put
    };

    private final String authorisation;
    private final JsonObject body;
    private final Method method;
    private final String path;
    private final Query query;

    public static Request createDelete(String authorisation, String path, Query query) {
        return new Request(Request.Method.Delete, authorisation, path, query, null);
    }

    public static Request createGet(String authorisation, String path, Query query) {
        return new Request(Request.Method.Get, authorisation, path, query, null);
    }

    public static Request createPost(String authorisation, String path, JsonObject body) {
        return new Request(Request.Method.Post, authorisation, path, null, body);
    }

    public static Request createPut(String authorisation, String path, JsonObject body) {
        return new Request(Request.Method.Put, authorisation, path, null, body);
    }

    protected Request(Method method, String authorisation, String path, Query query, JsonObject body) {
        this.authorisation = authorisation;
        this.body = body;
        this.method = method;
        this.path = path;
        this.query = query;
    }

    public final String getAuthorisation() {
        return authorisation;
    }

    public final JsonObject getBody() {
        return body;
    }

    public final Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public final Query getQuery() {
        return query;
    }
}
