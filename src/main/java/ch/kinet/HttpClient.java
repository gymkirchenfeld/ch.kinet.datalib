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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class HttpClient {

    private SSLContext sslContext;

    public static HttpClient create() {
        return new HttpClient();
    }

    public static HttpClient create(String keyStorePath, char[] keyStorePassword, char[] keyPassword,
                                    String trustStorePath, char[] trustStorePassword) {
        return new HttpClient(keyStorePath, keyStorePassword, keyPassword, trustStorePath, trustStorePassword);
    }

    private HttpClient() {
        sslContext = null;
    }

    private HttpClient(String keyStorePath, char[] keyStorePassword, char[] keyPassword,
                       String trustStorePath, char[] trustStorePassword) {
        try {
            // init key manager
            KeyManager[] kms = null;
            if (keyStorePath != null) {
                final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(loadKeyStore(keyStorePath, keyStorePassword), keyPassword);
                kms = kmf.getKeyManagers();
            }

            // init trust manager
            TrustManager[] tms = null;
            if (trustStorePath != null) {
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(loadKeyStore(trustStorePath, trustStorePassword));
                tms = tmf.getTrustManagers();
            }

            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kms, tms, null);
        }
        catch (final Exception ex) {
            throw new HttpException(ex);
        }
    }

    public HttpConnection get(String url) {
        return openConnection(url, "GET");
    }

    public HttpConnection post(String url) {
        return openConnection(url, "POST");
    }

    private HttpConnection openConnection(String url, String method) {
        try {
            final URLConnection connection = new URL(url).openConnection();
            if (connection instanceof HttpsURLConnection && sslContext != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
            }

            if (connection instanceof HttpURLConnection) {
                final HttpURLConnection conn = (HttpURLConnection) connection;
                conn.setRequestMethod(method);
                return new HttpConnection(conn);
            }
        }
        catch (IOException ex) {
            throw new HttpException(ex);
        }

        return null;
    }

    private static KeyStore loadKeyStore(String path, char[] password) throws Exception {
        final KeyStore result = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(path);
            result.load(fi, password);
            return result;
        }
        finally {
            if (fi != null) {
                try {
                    fi.close();
                }
                catch (Exception ex) {
                    // ignore
                }
            }
        }
    }
}
