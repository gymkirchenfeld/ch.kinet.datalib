/*
 * Copyright (C) 2016 - 2024 by Stefan Rothe
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
package ch.kinet;

import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonArray {

    private final JSONArray imp;

    public static JsonArray create() {
        return new JsonArray(new JSONArray());
    }

    public static JsonArray create(Stream<JsonObject> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEachOrdered(element -> result.add(element));
        return result;
    }

    public static JsonArray createString(Stream<String> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEachOrdered(element -> result.add(element));
        return result;
    }

    public static JsonArray createTerse(Stream<? extends Json> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEachOrdered(element -> result.addTerse(element));
        return result;
    }

    public static JsonArray createTerse(Iterable<? extends Json> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEach(element -> result.addTerse(element));
        return result;
    }

    public static JsonArray createVerbose(Stream<? extends Json> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEachOrdered(element -> result.addVerbose(element));
        return result;
    }

    public static JsonArray createVerbose(Iterable<? extends Json> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEach(element -> result.addVerbose(element));
        return result;
    }

    public static JsonArray create(String source) {
        return new JsonArray(new JSONArray(source));
    }

    public static JsonArray fromStrings(Stream<String> elements) {
        final JsonArray result = new JsonArray(new JSONArray());
        elements.forEachOrdered(element -> result.add(element));
        return result;
    }

    JsonArray(JSONArray imp) {
        this.imp = imp;
    }

    public void add(boolean value) {
        imp.put(value);
    }

    public void add(int value) {
        imp.put(value);
    }

    public void add(String value) {
        if (value != null) {
            imp.put(value);
        }
    }

    public void add(JsonArray value) {
        if (value != null) {
            imp.put(value.getImp());
        }
    }

    public void add(JsonObject value) {
        if (value != null) {
            imp.put(value.getImp());
        }
    }

    public void addTerse(Json value) {
        JsonObject object = value == null ? null : value.toJsonTerse();
        add(object);
    }

    public void addVerbose(Json value) {
        JsonObject object = value == null ? null : value.toJsonVerbose();
        add(object);
    }

    public JsonArray getArray(int index) {
        if (imp.isNull(index)) {
            return null;
        }
        else {
            JSONArray array = imp.optJSONArray(index);
            if (array == null) {
                return null;
            }
            else {
                return new JsonArray(array);
            }
        }
    }

    public boolean getBoolean(int index, boolean defaultValue) {
        if (imp.isNull(index)) {
            return defaultValue;
        }
        else {
            return imp.optBoolean(index, defaultValue);
        }
    }

    public int getInt(int index, int defaultValue) {
        if (imp.isNull(index)) {
            return defaultValue;
        }
        else {
            return imp.optInt(index, defaultValue);
        }
    }

    public JsonObject getObject(int index) {
        if (imp.isNull(index)) {
            return null;
        }
        else {
            JSONObject object = imp.optJSONObject(index);
            if (object == null) {
                return null;
            }
            else {
                return new JsonObject(object);
            }
        }
    }

    public int getObjectId(int index, int defaultValue) {
        JsonObject obj = getObject(index);
        if (obj == null) {
            return defaultValue;
        }

        return obj.getInt(Entity.JSON_ID, defaultValue);
    }

    public String getString(int index) {
        if (imp.isNull(index)) {
            return null;
        }
        else {
            return imp.optString(index);
        }
    }

    public boolean isNull(int index) {
        return imp.isNull(index);
    }

    public int length() {
        return imp.length();
    }

    @Override
    public String toString() {
        return imp.toString();
    }

    JSONArray getImp() {
        return imp;
    }
}
