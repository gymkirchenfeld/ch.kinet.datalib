/*
 * Copyright (C) 2011 - 2016 by Stefan Rothe
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

public class ArgumentMismatchException extends ObjectCreationException {

    ArgumentMismatchException(Class<?> targetClass, String parameterName, int pos, Class expected, Class got) {
        super(targetClass, buildMessage(targetClass, parameterName, pos, expected, got));
    }

    private static String buildMessage(Class<?> targetClass, String parameterName, int pos, Class expected, Class got) {
        StringBuilder result = new StringBuilder();
        result.append("Invalid argument for parameter ");
        result.append(parameterName);
        result.append(" at position ");
        result.append(pos);
        result.append(" for constructor of ");
        result.append(targetClass.getName());
        result.append(": expected ");
        result.append(expected);
        result.append(", got ");
        result.append(got);
        result.append(".");
        return result.toString();
    }
}
