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
package ch.kinet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class Data implements Json {

    private static final String JSON_DATA = "data";
    private static final String JSON_FILE_NAME = "fileName";
    private static final String JSON_MIME_TYPE = "mimeType";
    public static final String MIME_TYPE_CSV = "text/csv";
    public static final String MIME_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MIME_TYPE_ICAL = "text/calendar";
    public static final String MIME_TYPE_JSON = "application/json";
    public static final String MIME_TYPE_TEXT = "text/plain";
    public static final String MIME_TYPE_PDF = "application/pdf";
    public static final String MIME_TYPE_PNG = "image/png";

    public static Data binary(InputStream in, String fileName, String mimeType) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos == null ? empty() : new BinaryData(baos.toByteArray(), fileName, mimeType);
        }
    }

    public static Data csv(String content, String fileName) {
        return new TextData(content, fileName, MIME_TYPE_CSV);
    }

    public static Data empty() {
        return new EmptyData();
    }

    public static Data ical(ICalendar data, String fileName) {
        return new TextData(data.toString(), fileName, MIME_TYPE_ICAL);
    }

    public static Data json(JsonObject content) {
        return new JsonData(content, null);
    }

    public static Data jsonEncodedBinary(JsonObject json) {
        String mimeType = json.getString(JSON_MIME_TYPE);
        String fileName = json.getString(JSON_FILE_NAME);
        String base64Data = json.getString(JSON_DATA);
        if (base64Data == null) {
            return empty();
        }

        try {
            return new BinaryData(Base64.getDecoder().decode(base64Data), fileName, mimeType);
        }
        catch (IllegalArgumentException ex) {
            return empty();
        }
    }

    public static Data pdf(byte[] content, String fileName) {
        return new BinaryData(content, fileName, MIME_TYPE_PDF);
    }

    public static Data png(Binary content, String fileName) {
        if (content.isNull()) {
            return empty();
        }

        return new BinaryData(content.toBytes(), fileName, MIME_TYPE_PNG);
    }

    public static final Data text(String content) {
        return new TextData(content, null, MIME_TYPE_TEXT);
    }

    private final String fileName;
    private final String mimeType;

    private Data(String fileName, String mimeType) {
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public abstract boolean isEmpty();

    public final String fileName() {
        return fileName;
    }

    public final String mimeType() {
        return mimeType;
    }

    public abstract byte[] toBytes();

    public final Binary toBinary() {
        return Binary.from(toBytes());
    }

    @Override
    public JsonObject toJsonVerbose() {
        return toJsonTerse();
    }

    private static class EmptyData extends Data {

        public EmptyData() {
            super(null, null);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public byte[] toBytes() {
            return new byte[0];
        }

        @Override
        public JsonObject toJsonTerse() {
            return JsonObject.create();
        }

        @Override
        public String toString() {
            return null;
        }

    }

    private static class BinaryData extends Data {

        private final byte[] content;

        public BinaryData(byte[] content, String fileName, String mimeType) {
            super(fileName, mimeType);
            this.content = content;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public byte[] toBytes() {
            return content;
        }

        @Override
        public JsonObject toJsonTerse() {
            JsonObject result = JsonObject.create();
            result.put(JSON_DATA, encodeBase64(content));
            result.put(JSON_FILE_NAME, fileName());
            result.put(JSON_MIME_TYPE, mimeType());
            return result;
        }

        @Override
        public String toString() {
            return toJsonTerse().toString();
        }
    }

    private static class JsonData extends Data {

        private final JsonObject content;

        private JsonData(JsonObject content, String fileName) {
            super(fileName, MIME_TYPE_JSON);
            this.content = content;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.isEmpty();
        }

        @Override
        public byte[] toBytes() {
            return encodeUTF8(content.toString());
        }

        @Override
        public JsonObject toJsonTerse() {
            return JsonObject.create(content);
        }

        @Override
        public String toString() {
            return content.toString();
        }
    }

    private static class TextData extends Data {

        private final String content;

        private TextData(String content, String fileName, String mimeType) {
            super(fileName, mimeType);
            this.content = content;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.isEmpty();
        }

        @Override
        public byte[] toBytes() {
            return encodeUTF8(content);
        }

        @Override
        public JsonObject toJsonTerse() {
            return JsonObject.create(content);
        }

        @Override
        public String toString() {
            return content;
        }
    }

    private static String encodeBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    private static byte[] encodeUTF8(String content) {
        // Add Unicode byte order mark so Microsoft can recognize the encoding.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0xef);
        out.write(0xbb);
        out.write(0xbf);
        out.writeBytes(content.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
