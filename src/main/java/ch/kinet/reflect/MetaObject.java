/*
 * Copyright (C) 2011 - 2021 by Stefan Rothe
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents meta information about a java class.
 *
 * @param <T> Type of the class that this class represents
 */
public final class MetaObject<T> {

    private static final Map<Class<?>, MetaObject<?>> REGISTRY = new HashMap<Class<?>, MetaObject<?>>();
    private final Constructor<T> constructor;
    private final List<Property> keyProperties;
    private final Set<Property> persistentProperties;
    private final Set<Property> properties;
    private final Map<String, Property> propertyInfos;
    private final MetaObject<? super T> superMetaObject;
    private final Class<T> targetClass;
    private final List<String> ctorPropertyNames;
    private final Set<Property> nonCtorProperties;

    /**
     * Returns the meta object for the specified class.
     *
     * @param <S> type of the specified class.
     * @param targetClass the class for which the meta object should be retrieved.
     * @return meta object for the specified class.
     */
    @SuppressWarnings("unchecked")
    public static <S> MetaObject<S> forClass(Class<S> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        if (!REGISTRY.containsKey(targetClass)) {
            REGISTRY.put(targetClass, new MetaObject<>(targetClass));
        }

        // Unchecked conversion:
        return (MetaObject<S>) REGISTRY.get(targetClass);
    }

    private MetaObject(Class<T> targetClass) {
        this.targetClass = targetClass;
        constructor = findConstructor(targetClass);
        propertyInfos = findProperties(this, targetClass);
        superMetaObject = findSuperMetaObject(targetClass);
        // These initializations depend on propertyInfos and superMetaObject
        properties = initProperties();
        keyProperties = initKeyProperties();
        persistentProperties = initPersistentProperties();

        ctorPropertyNames = new ArrayList<>();
        nonCtorProperties = new HashSet<>(properties);
        if (constructor != null) {
            PropertyInitializer pi = constructor.getAnnotation(PropertyInitializer.class);
            if (pi != null) {
                ctorPropertyNames.addAll(Arrays.asList(pi.value()));
                for (String propertyName : ctorPropertyNames) {
                    nonCtorProperties.remove(property(propertyName));
                }
            }
        }
    }

    public String getFullName() {
        return targetClass.getName();
    }

    /**
     * Returns the name of the class represented by this meta object.
     *
     * @return name of the class
     */
    public String getName() {
        return targetClass.getSimpleName();
    }

    public boolean isArray() {
        return targetClass.isArray();
    }

    public boolean isArrayOf(Class<?> cls) {
        return targetClass.isArray() && targetClass.getComponentType().equals(cls);
    }

    public boolean isAssignableFrom(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException("cls");
        }

        return targetClass.isAssignableFrom(cls);
    }

    public boolean isAssignableTo(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException("cls");
        }

        return cls.isAssignableFrom(targetClass);
    }

    public boolean isBoolean() {
        return targetClass == Boolean.class || targetClass == Boolean.TYPE;
    }

    public boolean isByte() {
        return targetClass == Byte.class || targetClass == Byte.TYPE;
    }

    public boolean isChar() {
        return targetClass == Character.class || targetClass == Character.TYPE;
    }

    public boolean isClass(Class<?> cls) {
        if (cls == null) {
            throw new NullPointerException("cls");
        }

        return targetClass.equals(cls);
    }

    public boolean isComparable() {
        return isBoolean() || isByte() || isChar() ||
            isDouble() || isFloat() || isInteger() ||
            isLong() || isShort() || Comparable.class.isAssignableFrom(targetClass);
    }

    public boolean isDouble() {
        return targetClass == Double.class || targetClass == Double.TYPE;
    }

    public boolean isFloat() {
        return targetClass == Float.class || targetClass == Float.TYPE;
    }

    public boolean isInteger() {
        return targetClass == Integer.class || targetClass == Integer.TYPE;
    }

    public boolean isLong() {
        return targetClass == Long.class || targetClass == Long.TYPE;
    }

    public boolean isShort() {
        return targetClass == Short.class || targetClass == Short.TYPE;
    }

    /**
     * Returns the key property of the class represented by this meta object. Returns null if the represented class has
     * none or more than one key properties.
     *
     * @return key property or null
     */
    public Property keyProperty() {
        if (keyProperties.size() == 1) {
            return keyProperties.toArray(new Property[1])[0];
        }
        else {
            return null;
        }
    }

    /**
     * Returns list of all key properties. The list is sorted alphabetically by the properties' names.
     *
     * @return list of keyy properties
     */
    public List<Property> keyProperties() {
        return keyProperties;
    }

    /**
     * Creates a new instance of the class represented by this meta object and initializes the object's properties with
     * the specified values.
     *
     * @param propertyValues
     * @return
     */
    public T newInstance(PropertyValues propertyValues) {
        if (constructor == null) {
            throw new NoConstructorException(targetClass);
        }

        List<Object> initArgList = new ArrayList<>();
        for (String propertyName : ctorPropertyNames) {
            initArgList.add(propertyValues.getValue(propertyName));
        }

        Object[] initArgs = initArgList.toArray();
        Types paramTypes = new Types(constructor.getParameterTypes());
        Types argTypes = new Types(initArgs);
        int pos = paramTypes.findMismatch(argTypes);
        if (pos != -1) {
            throw new ArgumentMismatchException(targetClass, ctorPropertyNames.get(pos), pos,
                                                paramTypes.at(pos), argTypes.at(pos));
        }

        try {
            T result = constructor.newInstance(initArgs);
            for (Property property : nonCtorProperties) {
                if (property.isWritable() && propertyValues.containsValue(property.getName())) {
                    property.setValue(result, propertyValues.getValue(property.getName()));
                }
            }

            return result;
        }
        catch (InstantiationException ex) {
            throw new ObjectCreationException(targetClass, ex);
        }
        catch (IllegalAccessException ex) {
            throw new ObjectCreationException(targetClass, ex);
        }
        catch (IllegalArgumentException ex) {
            throw new ObjectCreationException(targetClass, ex);
        }
        catch (InvocationTargetException ex) {
            throw new ObjectCreationException(targetClass, ex.getCause());
        }
    }

    public Set<Property> persistentProperties() {
        return persistentProperties;
    }

    /**
     * Returns a list of all properties of the class represented by this meta object.
     *
     * @return list of all properties of the class represented by this meta object.
     */
    public Set<Property> properties() {
        return properties;
    }

    public Property property(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        Property result = doGetProperty(name);
        if (result == null) {
            throw new NoSuchPropertyException(this, name);
        }

        return result;
    }

    public List<String> propertyNames() {
        List<String> result;
        if (superMetaObject != null) {
            result = superMetaObject.propertyNames();
        }
        else {
            result = new ArrayList<>();
        }

        result.addAll(propertyInfos.keySet());
        return result;

    }

    @Override
    public String toString() {
        return targetClass.getName();
    }

    public void updateInstance(T object, PropertyValues propertyValues) {
        for (Property property : properties) {
            if (property.isWritable() && propertyValues.containsValue(property.getName())) {
                property.setValue(object, propertyValues.getValue(property.getName()));
            }
        }
    }

    Property doGetProperty(String name) {
        if (propertyInfos.containsKey(name)) {
            return propertyInfos.get(name);
        }
        else if (superMetaObject != null) {
            return superMetaObject.doGetProperty(name);
        }
        else {
            return null;
        }
    }

    private List<Property> initKeyProperties() {
        Set<Property> result = new TreeSet<>();
        if (superMetaObject != null) {
            result.addAll(superMetaObject.keyProperties);
        }

        for (Property property : propertyInfos.values()) {
            if (property.isKey()) {
                result.add(property);
            }
        }

        return Collections.unmodifiableList(new ArrayList<>(result));
    }

    private Set<Property> initProperties() {
        Set<Property> result = new HashSet<>();
        if (superMetaObject != null) {
            result.addAll(superMetaObject.properties);
        }

        result.addAll(propertyInfos.values());
        return Collections.unmodifiableSet(result);
    }

    private Set<Property> initPersistentProperties() {
        Set<Property> result = new HashSet<>();
        if (superMetaObject != null) {
            result.addAll(superMetaObject.persistentProperties);
        }

        for (Property property : propertyInfos.values()) {
            if (property.isPersistent()) {
                result.add(property);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    private static <S> MetaObject<? super S> findSuperMetaObject(Class<S> targetClass) {
        Class<? super S> superClass = targetClass.getSuperclass();
        if (superClass == null) {
            return null;
        }
        else {
            return forClass(superClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <S> Constructor<S> findConstructor(Class<S> targetClass) {
        List<Constructor<S>> ctors = new ArrayList<>();
        for (Constructor<?> ctor : targetClass.getConstructors()) {
            if (ctor.getAnnotation(PropertyInitializer.class) != null &&
                (ctor.getModifiers() & Modifier.PUBLIC) != 0) {
                // Unchecked conversion:
                ctors.add((Constructor<S>) ctor);
            }
        }

        if (ctors.size() > 1) {
            throw new PropertyInitializerException();
        }

        if (ctors.size() == 1) {
            return ctors.get(0);
        }
        else {
            try {
                return targetClass.getConstructor();
            }
            catch (NoSuchMethodException ex) {
                return null;
            }
            catch (SecurityException ex) {
                return null;
            }
        }
    }

    private static <S> Map<String, Property> findProperties(MetaObject metaObject, Class<S> targetClass) {
        Map<String, Property> result = new HashMap<>();
        for (Method method : targetClass.getDeclaredMethods()) {
            checkMethod(metaObject, method, result);
        }

        return result;
    }

    private static void checkMethod(MetaObject metaObject, Method method, Map<String, Property> result) {
        // Do not allow java.lang.Object.getClass() method
        if (method.getName().equals("getClass")) {
            return;
        }

        int modifiers = method.getModifiers();
        // Allow only public methods
        if ((modifiers & Modifier.PUBLIC) == 0) {
            return;
        }

        // Don't allow static methods
        if ((modifiers & Modifier.STATIC) != 0) {
            return;
        }

        // Allow only methods with no parameters
        if (method.getParameterTypes().length != 0) {
            return;
        }

        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            methodName = methodName.substring(3);
            if (result.containsKey(methodName)) {
                throw new DuplicatePropertyException(metaObject, methodName);
            }

            result.put(methodName, new Property(metaObject, methodName, method));
        }

        if (methodName.startsWith("is") && Boolean.TYPE.equals(method.getReturnType())) {
            methodName = methodName.substring(2);
            if (result.containsKey(methodName)) {
                throw new DuplicatePropertyException(metaObject, methodName);
            }

            result.put(methodName, new Property(metaObject, methodName, method));
        }
    }
}
