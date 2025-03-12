/*
 * MIT License
 *
 * Copyright (c) 2016 - 2024 Stefan Rothe
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
import java.net.URI;
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
    private final String keyStoreType;

    public static HttpClient create() {
        return new HttpClient();
    }

    public static HttpClient createPKIX(String keyStorePath, char[] keyStorePassword, char[] keyPassword,
                                        String trustStorePath, char[] trustStorePassword) {
        return new HttpClient("PKIX", "PKCS12", keyStorePath, keyStorePassword, keyPassword,
                              trustStorePath, trustStorePassword);
    }

    public static HttpClient createJKS(String keyStorePath, char[] keyStorePassword, char[] keyPassword,
                                       String trustStorePath, char[] trustStorePassword) {
        return new HttpClient(null, null, keyStorePath, keyStorePassword, keyPassword, trustStorePath, trustStorePassword);
    }

    private HttpClient() {
        keyStoreType = KeyStore.getDefaultType();
        sslContext = null;
    }

    private HttpClient(String managerFactoryType, String keyStoreType, String keyStorePath, char[] keyStorePassword,
                       char[] keyPassword, String trustStorePath, char[] trustStorePassword) {
        if (Util.isEmpty(managerFactoryType)) {
            managerFactoryType = KeyManagerFactory.getDefaultAlgorithm();
        }

        this.keyStoreType = Util.isEmpty(keyStoreType) ? KeyStore.getDefaultType() : keyStoreType;
        try {
            // init key manager
            KeyManager[] kms = null;
            if (keyStorePath != null) {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(managerFactoryType);
                kmf.init(loadKeyStore(keyStorePath, keyStorePassword), keyPassword);
                kms = kmf.getKeyManagers();
            }

            // init trust manager
            TrustManager[] tms = null;
            if (trustStorePath != null) {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(managerFactoryType);
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

    public HttpConnection put(String url) {
        return openConnection(url, "PUT");
    }    

    private HttpConnection openConnection(String url, String method) {
        try {
            URLConnection connection = URI.create(url).toURL().openConnection();
            if (connection instanceof HttpsURLConnection && sslContext != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
            }

            if (connection instanceof HttpURLConnection) {
                HttpURLConnection conn = (HttpURLConnection) connection;
                conn.setRequestMethod(method);
                return new HttpConnection(conn);
            }
        }
        catch (IOException ex) {
            throw new HttpException(ex);
        }

        return null;
    }

    private KeyStore loadKeyStore(String path, char[] password) throws Exception {
        KeyStore result = KeyStore.getInstance(keyStoreType);
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
