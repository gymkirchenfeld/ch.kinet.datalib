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

public final class Request<T> {

    public enum Method {
        Delete, Get, Patch, Post, Put
    };

    private final T authorisation;
    private final Data body;
    private final Method method;
    private final String path;
    private final Query query;

    public static <T> Request<T> createDelete(T authorisation, String path, Query query) {
        return new Request(Request.Method.Delete, authorisation, path, query, null);
    }

    public static <T> Request<T> createGet(T authorisation, String path, Query query) {
        return new Request(Request.Method.Get, authorisation, path, query, null);
    }

    public static <T> Request<T> withBody(Method method, T authorisation, String path, Data body) {
        return new Request(method, authorisation, path, null, body);
    }

    protected Request(Method method, T authorisation, String path, Query query, Data body) {
        assert method != null;
        assert authorisation != null;
        assert path != null;

        this.authorisation = authorisation;
        this.body = body;
        this.method = method;
        this.path = path;
        this.query = query;
    }

    public final T getAuthorisation() {
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

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(method.toString().toUpperCase());
        result.append(' ');
        result.append(path);
        if (query != null) {
            result.append(query.toString());
        }

        return result.toString();
    }
}
