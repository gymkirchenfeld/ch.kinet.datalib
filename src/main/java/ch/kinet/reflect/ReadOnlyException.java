/*
 * Copyright (C) 2012 by Stefan Rothe
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
package ch.kinet.reflect;

public class ReadOnlyException extends RuntimeException {

    ReadOnlyException(Property property) {
        super(buildMessage(property));
    }

    private static String buildMessage(Property property) {
        StringBuilder result = new StringBuilder();
        result.append("Cannot set the value of read-only property '");
        result.append(property.getName());
        result.append("' of type ");
        result.append(property.getType().getName());
        result.append(".");
        return result.toString();
    }
}
