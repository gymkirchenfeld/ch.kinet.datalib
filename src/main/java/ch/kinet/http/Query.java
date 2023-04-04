/*
 * Copyright (C) 2018 - 2021 by Sebastian Forster, Stefan Rothe
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

import ch.kinet.Date;
import ch.kinet.Dict;
import ch.kinet.Json;
import ch.kinet.JsonArray;
import ch.kinet.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Query extends Dict implements Json {

    private final Map<String, String[]> query;

    public static Query create(Map<String, String[]> query) {
        return new Query(query);
    }

    private Query(Map<String, String[]> query) {
        this.query = query;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if (!hasKey(key)) {
            return defaultValue;
        }

        return "true".equals(this.getString(key));
    }

    @Override
    public Date getDate(String key, Date defaultValue) {
        Date result = Date.tryParseISO8601(getString(key), null);
        if (result == null) {
            result = Date.tryParseDMY(getString(key));
            if (result == null) {
                result = defaultValue;
            }
        }

        return result;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        final String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(values[0]);
        }
        catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        final String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(values[0]);
        }
        catch (NumberFormatException ex) {
            return 0;
        }
    }

    public int[] getInts(String key) {
        final String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return new int[0];
        }

        final List<Integer> ints = new ArrayList<>();
        for (final String value : values) {
            try {
                ints.add(Integer.parseInt(value));
            }
            catch (NumberFormatException ex) {
                // ignore
            }
        }

        final int[] result = new int[ints.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = ints.get(i);
        }

        return result;
    }

    @Override
    public String getString(String key, String defaultValue) {
        String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return defaultValue;
        }

        return values[0];
    }

    public String[] getStrings(String key) {
        String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return new String[0];
        }

        return values;
    }

    @Override
    public boolean hasKey(String key) {
        String[] values = query.get(key);
        if (values == null || values.length == 0) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        return query.isEmpty();
    }

    @Override
    public JsonObject toJsonTerse() {
        final JsonObject result = JsonObject.create();
        for (Map.Entry<String, String[]> entry : query.entrySet()) {
            final JsonArray list = JsonArray.create();
            for (String string : entry.getValue()) {
                list.add(string);
            }

            result.put(entry.getKey(), list);
        }

        return result;
    }

    @Override
    public JsonObject toJsonVerbose() {
        return toJsonTerse();
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String[]> entry : query.entrySet()) {
            for (String string : entry.getValue()) {
                if (result.length() > 0) {
                    result.append("&");
                }

                result.append(entry.getKey());
                result.append("=");
                result.append(string);
            }
        }

        return result.toString();
    }
}
