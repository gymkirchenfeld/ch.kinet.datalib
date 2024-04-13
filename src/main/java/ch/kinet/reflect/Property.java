/*
 * Copyright (C) 2011, 2012 by Stefan Rothe
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Property implements Comparable<Property> {

    private final boolean isKey;
    private final boolean isPersistent;
    private final String name;
    private final MetaObject<?> owner;
    private final PropertyInit propertyInit;
    private final Method readMethod;
    private final Class<?> type;
    private final Method writeMethod;

    Property(MetaObject<?> owner, String name, Method readMethod) {
        assert owner != null;
        assert readMethod != null;
        assert (readMethod.getModifiers() & Modifier.PUBLIC) != 0;
        assert readMethod.getParameterTypes().length == 0;

        Persistence persistence = readMethod.getAnnotation(Persistence.class);
        this.name = name;
        this.isPersistent = persistence == null || !persistence.ignore();
        this.isKey = persistence != null && persistence.key();
        this.owner = owner;
        this.propertyInit = findPropertyInit(persistence);
        this.readMethod = readMethod;
        this.type = readMethod.getReturnType();
        this.writeMethod = findWriteMethod(readMethod.getDeclaringClass(), this.name, this.type);
    }

    @Override
    public int compareTo(Property o) {
        if (o == null) {
            return 1;
        }
        else {
            return this.name.compareTo(o.name);
        }
    }

    public MetaObject<?> getDeclaringType() {
        return this.owner;
    }

    public String getFullName() {
        return this.readMethod.getDeclaringClass().getName() + "." + this.name;
    }

    public String getName() {
        return this.name;
    }

    public PropertyInit getPropertyInit() {
        return this.propertyInit;
    }

    public Class<?> getPropertyClass() {
        return this.type;
    }

    public MetaObject<?> getType() {
        return MetaObject.forClass(this.type);
    }

    public Object getValue(Object object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        if (!this.owner.isAssignableFrom(object.getClass())) {
            throw new ObjectMismatchException(this, object);
        }

        try {
            return this.readMethod.invoke(object);
        }
        catch (IllegalAccessException ex) {
            // Cannot happen, read method is guaranteed to have public access
        }
        catch (IllegalArgumentException ex) {
            // Cannot happen, read method has no arguments
        }
        catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            else {
                Logger.getLogger(Property.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    public boolean isAssignableFrom(Property other) {
        if (other == null) {
            throw new NullPointerException("other");
        }

        return this.isWritable() && this.type.isAssignableFrom(other.type);
    }

    public boolean isKey() {
        return this.isKey;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    public boolean isWritable() {
        return this.writeMethod != null;
    }

    public void setValue(Object object, Object value) {
        if (this.writeMethod == null) {
            throw new ReadOnlyException(this);
        }

        if (object == null) {
            throw new NullPointerException("object");
        }

        if (!this.owner.isAssignableFrom(object.getClass())) {
            throw new ObjectMismatchException(this, object);
        }

        try {
            this.writeMethod.invoke(object, value);
        }
        catch (IllegalAccessException ex) {
            // Cannot happen, write method is guaranteed to have public access
        }
        catch (IllegalArgumentException ex) {
            throw new ValueMismatchException(this, value);
        }
        catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            else {
                Logger.getLogger(Property.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String toString() {
        return this.getFullName();
    }

    private static PropertyInit findPropertyInit(Persistence persistence) {
        if (persistence == null) {
            return PropertyInit.Manual;
        }
        else {
            return persistence.init();
        }
    }

    private static Method findWriteMethod(Class<?> cl, String name, Class type) {
        try {
            Method result = cl.getMethod("set" + name, type);
            if ((result.getModifiers() & Modifier.PUBLIC) == 0) {
                return null;
            }

            return result;
        }
        catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
