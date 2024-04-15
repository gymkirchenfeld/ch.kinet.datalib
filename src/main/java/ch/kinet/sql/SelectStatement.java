/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
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
import ch.kinet.reflect.PropertyValues;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SelectStatement<T> extends Statement<T> {

    private final MetaObject<T> metaObject;
    private final Map<String, ResultGetter> getters;

    static <S> List<S> execute(Connection connection, String schemaName, Class<S> targetClass, Condition where) {
        SelectStatement<S> statement = null;
        try {
            statement = create(connection, schemaName, targetClass, where);
            return statement.execute();
        }
        finally {
            if (statement != null) {
                statement.dispose();
            }
        }
    }

    private static <T> SelectStatement<T> create(Connection connection, String schemaName, Class<T> targetClass,
                                                 Condition where) {
        return new SelectStatement<>(new SelectStatementBuilder<>(connection, schemaName, targetClass, where));
    }

    private SelectStatement(SelectStatementBuilder<T> builder) {
        super(builder);
        this.metaObject = builder.metaObject();
        this.getters = builder.getters();
    }

    List<T> execute() {
        super.doExecute();
        ResultSet resultSet = null;
        try {
            resultSet = statement.getResultSet();
            if (connection().isLookup(dataClass())) {
                return executeWithLookup(resultSet, connection().lookupFor(dataClass()));
            }
            else {
                return executeWithoutLookup(resultSet);
            }
        }
        catch (SQLException ex) {
            throw new ResultSetException(this, ex);
        }
        finally {
            try {
                resultSet.close();
            }
            catch (SQLException ex) {
                // ignore
            }
        }
    }

    private List<T> executeWithLookup(ResultSet resultSet, Lookup<T> lookup) throws SQLException {
        final List<T> result = new ArrayList<>();
        final PropertyValueAdapter pva = new PropertyValueAdapter(getters, resultSet);
        while (resultSet.next()) {
            final Object key = pva.getValue(lookup.getKeyPropertyName());
            T element = lookup.get(key);
            if (element == null) {
                element = metaObject.newInstance(pva);
                lookup.add(element);
            }
            else {
                metaObject.updateInstance(element, pva);
            }
            result.add(element);
        }

        return result;
    }

    private List<T> executeWithoutLookup(ResultSet resultSet) throws SQLException {
        final List<T> result = new ArrayList<>();
        final PropertyValueAdapter pva = new PropertyValueAdapter(getters, resultSet);
        while (resultSet.next()) {
            result.add(metaObject.newInstance(pva));
        }

        return result;
    }

    private static class PropertyValueAdapter implements PropertyValues {

        private final Map<String, ResultGetter> getters;
        private final ResultSet resultSet;

        public PropertyValueAdapter(Map<String, ResultGetter> retrievers, ResultSet resultSet) {
            this.getters = retrievers;
            this.resultSet = resultSet;
        }

        @Override
        public boolean containsValue(String propertyName) {
            return getters.containsKey(propertyName);
        }

        @Override
        public Object getValue(String propertyName) {
            final ResultGetter getter = getters.get(propertyName);
            if (getter == null) {
                return null;
            }
            else {
                return getter.getValue(resultSet);
            }
        }
    }

    private static class SelectStatementBuilder<T> extends StatementBuilder<T> {

        private final Set<String> columnNames;
        private final Map<String, ResultGetter> getters;
        private final Condition where;

        SelectStatementBuilder(Connection connection, String schemaName, Class<T> targetClass, Condition where) {
            super(connection, schemaName, targetClass);
            this.columnNames = new HashSet<>();
            this.getters = new HashMap<>();
            this.where = where;
            build();
        }

        final Map<String, ResultGetter> getters() {
            return getters;
        }

        private void appendColumnNames() {
            boolean first = true;
            for (String fieldName : columnNames) {
                if (first) {
                    first = false;
                }
                else {
                    append(", ");
                }

                append(fieldName);
            }
        }

        private void build() {
            for (Property property : metaObject().persistentProperties()) {
                final ResultGetter getter = ResultGetter.create(connection(), property);
                getters.put(property.getName(), getter);
                columnNames.add(getter.columnName());
            }

            append("select ");
            appendColumnNames();
            append(" from ");
            appendTableName();
            appendWhereClause(where);
        }
    }
}
