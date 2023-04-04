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

class Types {

    private final Class[] types;

    Types(Object[] objects) {
        types = new Class[objects.length];
        int i = 0;
        for (Object object : objects) {
            if (object == null) {
                types[i] = null;
            }
            else {
                types[i] = object.getClass();
            }

            ++i;
        }
    }

    Types(Class[] types) {
        this.types = types;
    }

    Class at(int index) {
        return types[index];
    }

    int findMismatch(Types argTypes) {
        if (types.length != argTypes.types.length) {
            return Math.min(types.length, argTypes.types.length) + 1;
        }

        for (int i = 0; i < types.length; ++i) {
            if (!matches(types[i], argTypes.types[i])) {
                return i;
            }
        }

        return -1;
    }

    boolean assignableFrom(Types argTypes) {
        if (types.length != argTypes.types.length) {
            return false;
        }

        for (int i = 0; i < types.length; ++i) {
            if (!matches(types[i], argTypes.types[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Class type : types) {
            if (result.length() != 0) {
                result.append(", ");
            }

            if (type == null) {
                result.append("null");
            }
            else {
                result.append(type.getName());
            }
        }

        return result.toString();
    }

    private static boolean matches(Class<?> paramType, Class<?> argType) {
        if (argType == null) {
            return !paramType.isPrimitive();
        }
        else if (paramType.isAssignableFrom(argType)) {
            return true;
        }
        else {
            return paramType.isAssignableFrom(normalizeType(argType));
        }
    }

    private static Class<?> normalizeType(Class<?> c) {
        if (Boolean.class.equals(c)) {
            return Boolean.TYPE;
        }
        else if (Byte.class.equals(c)) {
            return Byte.TYPE;
        }
        else if (Character.class.equals(c)) {
            return Character.TYPE;
        }
        else if (Double.class.equals(c)) {
            return Double.TYPE;
        }
        else if (Float.class.equals(c)) {
            return Float.TYPE;
        }
        else if (Integer.class.equals(c)) {
            return Integer.TYPE;
        }
        else if (Long.class.equals(c)) {
            return Long.TYPE;
        }
        else if (Short.class.equals(c)) {
            return Short.TYPE;
        }
        else {
            return c;
        }
    }
}
