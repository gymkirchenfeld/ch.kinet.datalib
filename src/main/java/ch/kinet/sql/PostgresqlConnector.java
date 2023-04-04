/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
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
package ch.kinet.sql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class PostgresqlConnector extends Connector {

    @Override
    java.sql.Connection doConnect(DbSpec spec) {
        try {
            return DriverManager.getConnection(url(spec), properties(spec));
        }
        catch (final SQLException ex) {
            if (ex.getCause() != null && ex.getCause().getClass().equals(java.net.ConnectException.class)) {
                throw new ConnectionFailedException(ex.getCause());
            }
            else if (ex.getMessage().startsWith("FATAL: no pg_hba.conf entry")) {
                throw new ConnectionRefusedException(spec, ex);
            }
            else if (ex.getMessage().startsWith("FATAL: Passwort-Authentifizierung")) {
                throw new AuthenticationFailedException(spec, ex);
            }
            else if (ex.getMessage().startsWith("FATAL: LDAP authentication failed for user")) {
                throw new AuthenticationFailedException(spec, ex);
            }
            else if (ex.getMessage().startsWith("Access denied")) {
                throw new AuthenticationFailedException(spec, ex);
            }
            else {
                throw new ConnectionFailedException(ex);
            }
        }
    }

    private String url(DbSpec spec) {
        final StringBuilder result = new StringBuilder();
        result.append("jdbc:");
        result.append("postgresql");
        result.append("://");
        result.append(spec.getDbServer());
        result.append(":");
        result.append(spec.getPort());
        result.append("/");
        result.append(spec.getDatabase());
        return result.toString();
    }

    private Properties properties(DbSpec spec) {
        final Properties result = new Properties();
        result.setProperty("user", spec.getUserName().toLowerCase());
        final char[] password = spec.getPassword();
        if (password != null) {
            result.setProperty("password", new String(spec.getPassword()));
        }

        if (spec.getSslEnabled()) {
            result.setProperty("ssl", "true");
            result.setProperty("sslmode", "verify-ca");
            result.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
        }
        return result;
    }
}
