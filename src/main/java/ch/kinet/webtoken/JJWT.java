/*
 * Copyright (C) 2017 - 2023 by Stefan Rothe, Sebastian Forster
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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.security.Key;
import java.util.Date;

public final class JJWT {

    public static Token parseToken(String token, SigningKeyProvider signingKeyProvider) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder().setSigningKeyResolver(new SigningKeyResolverAdapter() {

                @Override
                public Key resolveSigningKey(JwsHeader header, Claims claims) {
                    return signingKeyProvider.getSigningKey(header.getKeyId());
                }
            }).build().parseClaimsJws(token);

            Claims claims = jws.getBody();
            Date expiration = claims.getExpiration();
            Date notBefore = claims.getNotBefore();
            Date now = new Date();
            if (now.before(notBefore) || now.after(expiration)) {
                return Token.createInvalidToken(Token.TokenStatus.Expired);
            }

            return Token.createValidToken(jws.getBody());
        }
        catch (ExpiredJwtException ex) {
            return Token.createInvalidToken(Token.TokenStatus.Expired);
        }
        catch (MalformedJwtException | IllegalArgumentException ex) {
            return Token.createInvalidToken(Token.TokenStatus.Malformed);
        }
        catch (UnsupportedJwtException ex) {
            return Token.createInvalidToken(Token.TokenStatus.Unsupported);

        }
        catch (SignatureException ex) {
            return Token.createInvalidToken(Token.TokenStatus.InvalidSignature);
        }
    }
}
