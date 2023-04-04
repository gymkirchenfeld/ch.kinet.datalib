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

public class ObjectCreationException extends RuntimeException {

    private final Class<?> targetClass;

    ObjectCreationException(Class<?> targetClass, Throwable cause) {
        super(buildMessage(targetClass), cause);
        this.targetClass = targetClass;
    }

    protected ObjectCreationException(Class<?> targetClass, String message) {
        super(message);
        this.targetClass = targetClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    private static String buildMessage(Class<?> targetClass) {
        StringBuilder result = new StringBuilder();
        result.append("Cannot create object of class '");
        result.append(targetClass.getName());
        result.append("'.");
        return result.toString();
    }
}
