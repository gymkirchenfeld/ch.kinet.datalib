/*
 * Copyright (C) 2016 - 2021 by Sebastian Forster, Stefan Rothe
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

import ch.kinet.http.Data;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JsonObject extends Dict implements Json {

    private final JSONObject imp;

    public static JsonObject create() {
        return new JsonObject(new JSONObject());
    }

    public static JsonObject create(String source) {
        return new JsonObject(new JSONObject(source));
    }

    public static JsonObject create(JsonObject original) {
        String[] keys = original.keySet().toArray(new String[original.keySet().size()]);
        return new JsonObject(new JSONObject(original.imp, keys));
    }

    public static JsonObject createFromUrl(String url) {
        try {
            String result = HttpClient.create().get(url).readResponse();
            //System.out.println(result);
            return JsonObject.create(result);
        }
        catch (Exception ex) {
            return null;
        }

    }

    JsonObject(final JSONObject imp) {
        this.imp = imp;
    }

    public JsonArray getArray(String key) {
        if (imp.isNull(key)) {
            return null;
        }
        else {
            JSONArray array = imp.optJSONArray(key);
            if (array == null) {
                return null;
            }

            return new JsonArray(array);
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if (imp.isNull(key)) {
            return defaultValue;
        }

        return imp.optBoolean(key, defaultValue);
    }

    public Data getData(String key) {
        JsonObject object = getObject(key);
        if (object == null) {
            return Data.empty();
        }

        return Data.fromJson(object);
    }

    @Override
    public Date getDate(String key, Date defaultValue) {
        if (imp.isNull(key)) {
            return defaultValue;
        }

        final String raw = imp.optString(key);
        Date candidate = Date.tryParseISO8601(raw, null);
        if (candidate == null) {
            candidate = Date.tryParseDMY(raw);
        }

        return candidate;
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        if (imp.isNull(key)) {
            return defaultValue;
        }

        return imp.optDouble(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        if (imp.isNull(key)) {
            return defaultValue;
        }

        return imp.optInt(key, defaultValue);
    }

    public JsonObject getObject(String key) {
        if (imp.isNull(key)) {
            return null;
        }

        JSONObject object = imp.optJSONObject(key);
        if (object == null) {
            return null;
        }

        return new JsonObject(object);
    }

    public int getObjectId(String key, int defaultValue) {
        JsonObject obj = getObject(key);
        if (obj == null) {
            return defaultValue;
        }

        return obj.getInt(Entity.JSON_ID, defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        if (imp.isNull(key)) {
            return defaultValue;
        }

        return imp.optString(key);
    }

    @Override
    public boolean hasKey(String key) {
        return imp.has(key);
    }

    @Override
    public boolean isEmpty() {
        return imp.isEmpty();
    }

    public boolean isNull(String key) {
        return imp.isNull(key);
    }

    public Set<String> keySet() {
        return imp.keySet();
    }

    public void put(String key, boolean value) {
        imp.put(key, value);
    }

    public void put(String key, double value) {
        if (!Double.isNaN(value)) {
            imp.put(key, value);
        }
    }

    public void put(String key, int value) {
        imp.put(key, value);
    }

    public void put(String key, String value) {
        if (Util.isEmpty(value)) {
            putNull(key);
        }
        else {
            imp.put(key, value);
        }
    }

    public void put(String key, Date value) {
        if (value == null) {
            putNull(key);
        }
        else {
            imp.put(key, value.formatISO8601());
        }
    }

    public void put(String key, Time value) {
        if (value == null) {
            putNull(key);
        }
        else {
            imp.put(key, value.formatHM());
        }
    }

    public void put(Timestamp value) {
        if (value == null) {
            putNull("date");
            putNull("time");
        }
        else {
            put("date", value.getDate());
            put("time", value.getTime());
        }
    }

    public void put(String keyPrefix, Timestamp value) {
        if (value == null) {
            putNull(keyPrefix + "Date");
            putNull(keyPrefix + "Time");
        }
        else {
            put(keyPrefix + "Date", value.getDate());
            put(keyPrefix + "Time", value.getTime());
        }
    }

    public void put(String key, JsonArray value) {
        if (value == null) {
            putNull(key);
        }
        else {
            imp.put(key, value.getImp());
        }
    }

    public void put(String key, JsonObject value) {
        if (value == null) {
            putNull(key);
        }
        else {
            imp.put(key, value.getImp());
        }
    }

    public void putNull(String key) {
        imp.put(key, JSONObject.NULL);
    }

    public void putTerse(String key, Json value) {
        JsonObject object = value == null ? null : value.toJsonTerse();
        put(key, object);
    }

    public void putVerbose(String key, Json value) {
        JsonObject object = value == null ? null : value.toJsonVerbose();
        put(key, object);
    }

    @Override
    public JsonObject toJsonTerse() {
        return this;
    }

    @Override
    public JsonObject toJsonVerbose() {
        return this;
    }

    @Override
    public String toString() {
        return imp.toString();
    }

    JSONObject getImp() {
        return imp;
    }
}
