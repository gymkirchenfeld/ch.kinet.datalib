/*
 * Copyright (C) 2011 by Stefan Rothe
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

public class PropertySortException extends RuntimeException {

    PropertySortException(MetaObject metaObject, Property first, Property second) {
        super(buildMessage(metaObject, first, second));
    }

    private static String buildMessage(MetaObject metaObject, Property first, Property second) {
        StringBuilder result = new StringBuilder();
        result.append("Properties ");
        result.append(first.getName());
        result.append(" and ");
        result.append(second.getName());
        result.append(" of class ");
        result.append(metaObject.getFullName());
        result.append(" have same sort index.");
        return result.toString();
    }
}
