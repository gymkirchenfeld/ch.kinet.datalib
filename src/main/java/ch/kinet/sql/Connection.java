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
package ch.kinet.sql;

import ch.kinet.reflect.MetaObject;
import ch.kinet.reflect.Property;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Connection {

    private static final String SEQUENCE_SELECT_SQL =
        "select nextval(?)";
    private final Map<Class<?>, Lookup<?>> lookupMap;
    private final Object sequenceSelectStatementLock;
    private java.sql.Connection connection;
    private PreparedStatement sequenceSelectStatement;
    private String user;

    public Connection() {
        lookupMap = new HashMap<>();
        sequenceSelectStatementLock = new Object();
    }

    public void addLookup(Class<?> targetClass) {
        MetaObject<?> metaObject = MetaObject.forClass(targetClass);
        if (!lookupMap.containsKey(targetClass)) {
            Property keyProperty = metaObject.keyProperty();
            if (keyProperty == null) {
                throw new NoKeyPropertyException(targetClass);
            }

            lookupMap.put(targetClass, new Lookup(keyProperty));
        }
    }

    public void close() {
        if (connection == null) {
            return;
        }

        closing();
        try {
            connection.close();
            connection = null;
        }
        catch (final SQLException ex) {
            // ignore
        }
    }

    public void connect(DbSpec spec) {
        user = spec.getUserName();
        connection = Connector.connect(spec);
        sequenceSelectStatement = prepareStatement(SEQUENCE_SELECT_SQL);
        connected();
    }

    @SuppressWarnings("unchecked")
    public <T> void delete(String schemaName, T object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        MetaObject<T> metaObject = MetaObject.forClass((Class<T>) object.getClass());
        List<Condition> where = new ArrayList<>();
        for (Property property : metaObject.keyProperties()) {
            where.add(Condition.equals(property.getName(), property.getValue(object)));
        }

        delete(schemaName, (Class<T>) object.getClass(), Condition.and(where));
    }

    public <T> void delete(String schemaName, Class<T> targetClass, Condition where) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        DeleteStatement.execute(this, schemaName, targetClass, where);
    }

    public <T> void deleteAll(String schemaName, Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        DeleteStatement.execute(this, schemaName, targetClass, null);
    }

    public String getUser() {
        return user;
    }

    public <T> T insert(String schemaName, Class<T> targetClass, Map<String, Object> propertyValues) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        if (propertyValues == null) {
            throw new NullPointerException("propertyValues");
        }

        return InsertStatement.create(this, schemaName, targetClass).execute(propertyValues);
    }

    public int nextId(String sequenceName) {
        try {
            synchronized (sequenceSelectStatementLock) {
                sequenceSelectStatement.setString(1, sequenceName);
                sequenceSelectStatement.execute();
                ResultSet resultSet = sequenceSelectStatement.getResultSet();
                resultSet.next();
                return (int) resultSet.getLong(1);
            }
        }
        catch (SQLException ex) {
            throw new QuerySequenceException(sequenceName, ex);
        }
    }

    public <T> T lookup(Class<T> targetClass, Object key) {
        Lookup<T> lookup = lookupFor(targetClass);
        if (lookup == null) {
            return null;
        }
        else {
            return lookup.get(key);
        }
    }

    public <T> Stream<T> select(String schemaName, Class<T> targetClass, Condition where) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        return SelectStatement.execute(this, schemaName, targetClass, where).stream();
    }

    public <T> Stream<T> selectAll(String schemaName, Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        return select(schemaName, targetClass, null);
    }

    public <T> T selectOne(String schemaName, Class<T> targetClass, Condition where) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        List<T> result = select(schemaName, targetClass, where).collect(Collectors.toList());
        if (result.size() == 1) {
            return result.get(0);
        }
        else {
            return null;
        }
    }

    public <T> T tryInsert(String schemaName, Class<T> targetClass, Map<String, Object> propertyValues) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        if (propertyValues == null) {
            throw new NullPointerException("propertyValues");
        }

        try {
            return insert(schemaName, targetClass, propertyValues);
        }
        catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void update(String schemaName, T object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        UpdateStatement.create(this, schemaName, (Class<T>) object.getClass(), null).execute(object);
    }

    public <T> void update(String schemaName, T object, String... propertyNames) {
        update(schemaName, object, new HashSet<>(Arrays.asList(propertyNames)));
    }

    @SuppressWarnings("unchecked")
    public <T> void update(String schemaName, T object, Set<String> propertyNames) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        if (propertyNames.isEmpty()) {
            return;
        }

        UpdateStatement.<T>create(this, schemaName, (Class<T>) object.getClass(), propertyNames).execute(object);
    }

    public <T> void update(String schemaName, Class<T> targetClass,
                           Map<String, Object> properties, Condition where) {
        UpdateMultiStatement.<T>create(this, schemaName, targetClass, properties.keySet(), where).execute(properties);
    }

    protected void closing() {
    }

    protected void connected() {
    }

    <T> boolean isLookup(Class<T> targetClass) {
        return lookupMap.containsKey(targetClass);
    }

    @SuppressWarnings("unchecked")
    <T> Lookup<T> lookupFor(Class<T> targetClass) {
        return (Lookup<T>) lookupMap.get(targetClass);
    }

    java.sql.PreparedStatement prepareStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        }
        catch (SQLException ex) {
            throw new StatementPreparationException(sql, ex);
        }
    }

    Array createArrayOf(Collection collection) {
        if (collection.isEmpty()) {
            return null;
        }

        final Iterator it = collection.iterator();
        if (!it.hasNext()) {
            throw new CreateArrayException(null, "Cannot determine element type of empty collection .");
        }

        final Object element = it.next();
        String type = null;
        if (element instanceof String) {
            type = "text";
        }
        else if (element instanceof Double) {
            type = "float";
        }
        else if (element instanceof Integer) {
            type = "integer";
        }
        else if (element == null) {
            throw new CreateArrayException(null, "Cannot determine type of null element in collection .");
        }
        else {
            throw new CreateArrayException(null, "Cannot handle collection with elements of type " + element.getClass() + ".");
        }

        try {
            return connection.createArrayOf(type, collection.toArray());
        }
        catch (SQLException ex) {
            throw new CreateArrayException(collection.getClass(), ex);
        }
    }
}
