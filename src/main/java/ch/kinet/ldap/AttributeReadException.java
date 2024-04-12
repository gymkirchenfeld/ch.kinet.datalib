/*
 * Copyright (C) 2013 - 2024 by Stefan Rothe
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

public class AttributeReadException extends LdapException {

    AttributeReadException(Name dn, String attributeName, Throwable cause) {
        super(buildMessage(dn, attributeName, cause), cause);
    }

    private static String buildMessage(Name dn, String attributeName, Throwable cause) {
        StringBuilder result = new StringBuilder();
        result.append("Reading value of attribute '");
        result.append(attributeName);
        result.append("' in the context '");
        result.append(attributeName);
        result.append("' failed: ");
        result.append(cause.toString());
        return result.toString();
    }
}
