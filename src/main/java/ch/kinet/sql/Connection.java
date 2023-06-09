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

import ch.kinet.Timestamp;
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

public class Connection {

    private static final String NOW_SELECT_SQL =
        "select now()";
    private static final String ROLES_SELECT_SQL =
        "select role_name as RoleName from information_schema.enabled_roles";
    private static final String SEQUENCE_SELECT_SQL =
        "select nextval(?)";
    private final Map<MetaObject<?>, Lookup<?>> lookupMap;
    private final Object sequenceSelectStatementLock;
    private java.sql.Connection connection;
    private PreparedStatement nowStatement;
    private PreparedStatement rolesSelectStatement;
    private PreparedStatement sequenceSelectStatement;
    private String user;

    public Connection() {
        lookupMap = new HashMap<>();
        sequenceSelectStatementLock = new Object();
    }

    public final void addLookup(Class<?> targetClass) {
        final MetaObject<?> metaObject = MetaObject.forClass(targetClass);
        if (!lookupMap.containsKey(metaObject)) {
            final Property keyProperty = metaObject.keyProperty();
            if (keyProperty == null) {
                throw new NoKeyPropertyException(targetClass);
            }

            lookupMap.put(metaObject, new Lookup(keyProperty));
        }
    }

    public final void close() {
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

    public final void connect(DbSpec spec) {
        user = spec.getUserName();
        connection = Connector.connect(spec);
        nowStatement = prepareStatement(NOW_SELECT_SQL);
        rolesSelectStatement = prepareStatement(ROLES_SELECT_SQL);
        sequenceSelectStatement = prepareStatement(SEQUENCE_SELECT_SQL);
        connected();
    }

    @SuppressWarnings("unchecked")
    public final <T> void delete(String schemaName, T object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        final MetaObject<T> metaObject = MetaObject.forClass((Class<T>) object.getClass());
        final List<Condition> where = new ArrayList<>();
        for (Property property : metaObject.keyProperties()) {
            where.add(Condition.equals(property.getName(), property.getValue(object)));
        }

        delete(schemaName, (Class<T>) object.getClass(), Condition.and(where));
    }

    public final <T> void delete(String schemaName, Class<T> targetClass, Condition where) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        DeleteStatement.execute(this, schemaName, targetClass, where);
    }

    public final <T> void deleteAll(String schemaName, Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        DeleteStatement.execute(this, schemaName, targetClass, null);
    }

    public final List<String> enabledRoles() {
        try {
            rolesSelectStatement.execute();
            List<String> result = new ArrayList<>();
            ResultSet resultSet = rolesSelectStatement.getResultSet();
            while (resultSet.next()) {
                result.add(resultSet.getString("RoleName"));
            }

            return result;
        }
        catch (SQLException ex) {
            throw new QueryRolesException(ex);
        }
    }

    public final String getUser() {
        return user;
    }

    public final <T> T insert(String schemaName, Class<T> targetClass, Map<String, Object> propertyValues) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        if (propertyValues == null) {
            throw new NullPointerException("propertyValues");
        }

        return InsertStatement.create(this, schemaName, targetClass).execute(propertyValues);
    }

    public final int nextId(String sequenceName) {
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

    public final <T> T lookup(Class<T> targetClass, Object key) {
        final Lookup<T> lookup = lookupFor(MetaObject.forClass(targetClass));
        if (lookup == null) {
            return null;
        }
        else {
            return lookup.get(key);
        }
    }

    public final Timestamp now() {
        try {
            nowStatement.execute();
            final ResultSet resultSet = nowStatement.getResultSet();
            resultSet.next();
            return Timestamp.from(resultSet.getTimestamp(1));
        }
        catch (SQLException ex) {
            throw new QueryTimestampException(ex);
        }
    }

    public final <T> Stream<T> select(String schemaName, Class<T> targetClass, Condition where) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        return SelectStatement.execute(this, schemaName, targetClass, where).stream();
    }

    public final <T> Stream<T> selectAll(String schemaName, Class<T> targetClass) {
        if (targetClass == null) {
            throw new NullPointerException("targetClass");
        }

        return select(schemaName, targetClass, null);
    }

    public final <T> T selectOne(String schemaName, Class<T> targetClass, Condition where) {
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

    public final <T> T tryInsert(String schemaName, Class<T> targetClass, Map<String, Object> propertyValues) {
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
    public final <T> void update(String schemaName, T object) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        UpdateStatement.create(this, schemaName, (Class<T>) object.getClass(), null).execute(object);
    }

    public final <T> void update(String schemaName, T object, String... propertyNames) {
        update(schemaName, object, new HashSet<>(Arrays.asList(propertyNames)));
    }

    @SuppressWarnings("unchecked")
    public final <T> void update(String schemaName, T object, Set<String> propertyNames) {
        if (object == null) {
            throw new NullPointerException("object");
        }

        if (propertyNames.isEmpty()) {
            return;
        }

        UpdateStatement.<T>create(this, schemaName, (Class<T>) object.getClass(), propertyNames).execute(object);
    }

    public final <T> void update(String schemaName, Class<T> targetClass,
                                 Map<String, Object> properties, Condition where) {
        UpdateMultiStatement.<T>create(this, schemaName, targetClass, properties.keySet(), where).execute(properties);
    }

    protected void closing() {
    }

    protected void connected() {
    }

    @SuppressWarnings("unchecked")
    <T> Lookup<T> lookupFor(MetaObject<T> metaObject) {
        return (Lookup<T>) lookupMap.get(metaObject);
    }

    final java.sql.PreparedStatement prepareStatement(String sql) {
        try {
            return connection.prepareStatement(sql);
        }
        catch (SQLException ex) {
            throw new StatementPreparationException(sql, ex);
        }
    }

    final Array createArrayOf(Collection collection) {
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
