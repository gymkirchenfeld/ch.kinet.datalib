/*
 * Copyright (C) 2022 by Sebastian Forster, Stefan Rothe
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

import ch.kinet.Binary;
import ch.kinet.ICalendar;
import ch.kinet.Json;
import ch.kinet.JsonObject;
import java.util.Base64;

public class Data implements Json {

    private static final String JSON_DATA = "data";
    private static final String JSON_FILE_NAME = "fileName";
    private static final String JSON_MIME_TYPE = "mimeType";
    private static final String MIME_TYPE_CSV = "text/csv";
    private static final String MIME_TYPE_ICAL = "text/calendar";
    private static final String MIME_TYPE_JSON = "application/json";
    private static final String MIME_TYPE_PDF = "application/pdf";
    private static final String MIME_TYPE_PNG = "image/png";
    private static final String MIME_TYPE_TEXT = "text/plain";
    private final Binary data;
    private final String fileName;
    private final String mimeType;

    public static Data empty() {
        return new Data(null, null, null);
    }

    public static Data from(byte[] data) {
        return new Data(Binary.from(data), null, null);
    }

    public static Data fromJson(JsonObject json) {
        String mimeType = json.getString(JSON_MIME_TYPE);
        String fileName = json.getString(JSON_FILE_NAME);
        String base64Data = json.getString(JSON_DATA);
        if (base64Data == null) {
            return Data.empty();
        }

        try {
            byte[] data = Base64.getDecoder().decode(base64Data);
            return new Data(Binary.from(data), fileName, mimeType);
        }
        catch (IllegalArgumentException ex) {
            return Data.empty();
        }
    }

    public static Data csv(String data, String fileName) {
        return new Data(Binary.encodeUTF8(data), fileName, MIME_TYPE_CSV);
    }

    public static Data ical(ICalendar data, String fileName) {
        return new Data(Binary.encodeUTF8(data.toString()), fileName, MIME_TYPE_ICAL);
    }

    public static Data json(Json data) {
        return new Data(Binary.encodeUTF8(data.toString()), null, MIME_TYPE_JSON);
    }

    public static Data pdf(Binary data, String fileName) {
        return new Data(data, fileName, MIME_TYPE_PDF);
    }

    public static Data png(Binary data, String fileName) {
        return new Data(data, fileName, MIME_TYPE_PNG);
    }

    public static Data text(String data) {
        return new Data(Binary.encodeUTF8(data), null, MIME_TYPE_TEXT);
    }

    private Data(Binary data, String fileName, String mimeType) {
        this.data = data;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public Binary getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = JsonObject.create();
        if (data != null) {
            result.put(JSON_DATA, data.toBase64());
            result.put(JSON_FILE_NAME, fileName);
            result.put(JSON_MIME_TYPE, mimeType);
        }

        return result;
    }

    @Override
    public JsonObject toJsonVerbose() {
        return toJsonTerse();
    }
}
