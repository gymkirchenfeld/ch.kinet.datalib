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

public class HttpException extends RuntimeException {

    private final int code;

    public HttpException(Throwable cause) {
        super(cause);
        this.code = 0;
    }

    public HttpException(int code) {
        super(buildMessage(code));
        this.code = code;
    }

    public HttpException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public HttpException(int code, String message) {
        super(message);
        this.code = code;
    }

    public final int getCode() {
        return code;
    }

    private static String buildMessage(int code) {
        final StringBuilder result = new StringBuilder();
        result.append("HTTP response: ");
        result.append(code);
        result.append(" ");
        result.append(responseMessage(code));
        return result.toString();
    }

    private static String responseMessage(int code) {
        switch (code) {
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            default:
                return "";
        }
    }
}
