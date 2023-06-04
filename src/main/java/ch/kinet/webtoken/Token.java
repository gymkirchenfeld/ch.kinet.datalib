/*
 * Copyright (C) 2021 - 2023 by Stefan Rothe, Sebastian Forster
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
package ch.kinet.webtoken;

import io.jsonwebtoken.Claims;

public class Token {

    public enum TokenStatus {
        Expired, InvalidSignature, Malformed, Unsupported, Valid
    }

    static Token createValidToken(Claims claims) {
        return new Token(claims, TokenStatus.Valid);
    }

    static Token createInvalidToken(TokenStatus status) {
        return new Token(null, status);
    }

    private final Claims claims;
    private final TokenStatus status;

    private Token(Claims claims, TokenStatus status) {
        this.claims = claims;
        this.status = status;
    }

    public String getAccountName() {
        if (claims == null) {
            return null;
        }

        if (claims.containsKey("unique_name")) {
            return claims.get("unique_name").toString();
        }
        else if (claims.containsKey("preferred_username")) {
            return claims.get("preferred_username").toString();
        }
        else {
            return null;
        }
    }

    public String getApplicationId() {
        if (claims == null) {
            return null;
        }

        return claims.get("aud").toString();
    }

    public TokenStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String key : claims.keySet()) {
            result.append(key);
            result.append("=");
            result.append(claims.get(key));
            result.append("\n");
        }

        return result.toString();
    }
}
