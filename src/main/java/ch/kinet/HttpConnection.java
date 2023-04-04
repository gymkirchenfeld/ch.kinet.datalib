/*
 * MIT License
 *
 * Copyright (c) 2016 Stefan Rothe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ch.kinet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public final class HttpConnection {

    private final HttpURLConnection connection;

    HttpConnection(HttpURLConnection connection) {
        this.connection = connection;
    }

    public int getResponseCode() {
        try {
            return connection.getResponseCode();
        }
        catch (IOException ex) {
            return -1;
        }
    }

    public String getResponseMessage() {
        try {
            return connection.getResponseMessage();
        }
        catch (IOException ex) {
            return "Connection error";
        }
    }

    public String readResponse() {
        InputStream in = null;
        try {
            int code = connection.getResponseCode();
            if (code >= 400) {
                throw new HttpException(code);
            }

            in = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
            final StringBuilder response = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                response.append(line);
                response.append('\n');
                line = reader.readLine();
            }

            return response.toString();
        }
        catch (IOException ex) {
            throw new HttpException(ex);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (final IOException ex) {
                // ignore
            }
        }
    }

    public void setHeader(String key, String value) {
        connection.setRequestProperty(key, value);
    }

    public void writeBody(String body) {
        if (Util.isEmpty(body)) {
            return;
        }

        try {
            connection.setDoOutput(true);
            byte[] bodyData = body.getBytes("UTF8");
            connection.setRequestProperty("Content-Length", Integer.toString(bodyData.length));
            connection.getOutputStream().write(bodyData);
        }
        catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
