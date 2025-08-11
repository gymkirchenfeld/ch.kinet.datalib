/*
 * Copyright (C) 2013 - 2021 by Stefan Rothe
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import javax.imageio.ImageIO;

/**
 * Represents binary data. The data is internally stored as an array of bytes. This class provides methods to load and
 * save binary data to streams and files. Other methods allow to convert the data to other formats such as image or
 * string representations.
 *
 * Use Binary objects for the following data:
 * <ul>
 * <li>Images</li>
 * <li>IP addresses and MAC addresses</li>
 * </ul>
 */
public final class Binary implements Comparable<Binary> {

    private final byte[] data;

    public static Binary encodeJPG(BufferedImage image) throws IOException {
        if (image == null) {
            return new Binary(null);
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return new Binary(baos.toByteArray());
    }

    public static Binary encodePNG(BufferedImage image) throws IOException {
        if (image == null) {
            return new Binary(null);
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new Binary(baos.toByteArray());
    }

    public static Binary encodeUTF8(String text) {
        if (text == null) {
            return new Binary(null);
        }

        // Add Unicode byte order mark so Microsoft can recognize the encoding.
        // not needed anymore/Google Calender ics not working
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //out.write(0xef);
        //out.write(0xbb);
        //out.write(0xbf);
        out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
        return new Binary(out.toByteArray());
    }

    public static Binary parse(String text, int radix, char separator) {
        if (Util.isEmpty(text)) {
            return new Binary(null);
        }

        final String[] parts = Util.split(text, separator);
        final byte[] result = new byte[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            final int b = Integer.parseInt(parts[i], radix);
            if (b < 0 || 255 < b) {
                throw new NumberFormatException("'" + b + "' is not a valid unsigned byte value.");
            }

            result[i] = (byte) (b & 0xFF);
        }

        return new Binary(result);
    }

    public static Binary load(URL url) throws IOException {
        if (url == null) {
            return new Binary(null);
        }

        try ( InputStream in = url.openStream()) {
            return load(in);
        }
    }

    public static Binary load(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }

        try ( FileInputStream in = new FileInputStream(file)) {
            return load(in);
        }
    }

    public static Binary from(byte[] data) {
        return new Binary(data);
    }

    @Override
    public int compareTo(Binary other) {
        final int length = Math.min(data.length, other.data.length);
        for (int i = 0; i < length; ++i) {
            final int result = data[i] - other.data[i];
            if (result != 0) {
                return result;
            }
        }

        return data.length - other.data.length;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Binary) {
            return Arrays.equals(data, ((Binary) object).data);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public boolean isNull() {
        return data == null;
    }

    public void save(File file) throws IOException {
        try ( FileOutputStream out = new FileOutputStream(file)) {
            write(out);
        }
    }

    public BufferedImage toBufferedImage() {
        if (data == null) {
            return null;
        }

        try {
            return ImageIO.read(new ByteArrayInputStream(data));
        }
        catch (IOException ex) {
            return null;
        }
    }

    public String toBase64() {
        return data == null ? null : Base64.getEncoder().encodeToString(data);
    }

    public byte[] toBytes() {
        return data == null ? null : Arrays.copyOf(data, data.length);
    }

    public String toString(int radix, char separator, int numDigits) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            final StringBuilder segment = new StringBuilder();
            segment.append(Integer.toString(data[i] & 0xFF, radix));

            // prepend leading zeros if necessary
            while (segment.length() < numDigits) {
                segment.insert(0, '0');
            }

            result.append(segment);

            if (i < data.length - 1) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    public String toString(int radix, char separator) {
        return toString(radix, separator, 0);
    }

    @Override
    public String toString() {
        return toString(16, ' ');
    }

    public void write(OutputStream out) throws IOException {
        if (!isNull()) {
            out.write(data);
            out.flush();
        }
    }

    private Binary(byte[] data) {
        this.data = data;
    }

    private static Binary load(InputStream in) throws IOException {
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return new Binary(baos == null ? null : baos.toByteArray());
        }
    }
}
