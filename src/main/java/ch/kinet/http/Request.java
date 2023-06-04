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

import ch.kinet.Data;

public final class Request {

    public enum Method {
        Delete, Get, Patch, Post, Put
    };

    private final String authorisation;
    private final Data body;
    private final Method method;
    private final String path;
    private final Query query;

    public static Request createDelete(String authorisation, String path, Query query) {
        return new Request(Request.Method.Delete, authorisation, path, query, null);
    }

    public static Request createGet(String authorisation, String path, Query query) {
        return new Request(Request.Method.Get, authorisation, path, query, null);
    }

    public static Request withBody(Method method, String authorisation, String path, Data body) {
        return new Request(method, authorisation, path, null, body);
    }

    protected Request(Method method, String authorisation, String path, Query query, Data body) {
        this.authorisation = authorisation;
        this.body = body;
        this.method = method;
        this.path = path;
        this.query = query;
    }

    public final String getAuthorisation() {
        return authorisation;
    }

    public final Data getBody() {
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
