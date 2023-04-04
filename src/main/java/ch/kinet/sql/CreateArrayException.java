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
package ch.kinet.sql;

public class CreateArrayException extends SqlException {

    CreateArrayException(Class<?> cl, String message) {
        super(buildMessage(cl, message));
    }

    CreateArrayException(Class<?> cl, Throwable cause) {
        super(buildMessage(cl, null), cause);
    }

    private static String buildMessage(Class<?> cl, String message) {
        final StringBuilder result = new StringBuilder();
        result.append("Cannot create array for collection of type '");
        result.append(cl.getName());
        result.append("'.");
        if (message != null) {
            result.append(' ');
            result.append(message);
        }

        return result.toString();
    }
}
