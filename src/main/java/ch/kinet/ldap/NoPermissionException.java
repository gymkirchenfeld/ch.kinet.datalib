/*
 * Copyright (C) 2015 - 2024 by Stefan Rothe
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
package ch.kinet.ldap;

public class NoPermissionException extends LdapException {

    NoPermissionException(String operation, Name dn) {
        super(buildMessage(operation, dn), null);
    }

    private static String buildMessage(String operation, Name dn) {
        StringBuilder result = new StringBuilder();
        result.append(operation);
        result.append(" of context '");
        result.append(dn);
        result.append("' is not allowed.");
        return result.toString();
    }
}
