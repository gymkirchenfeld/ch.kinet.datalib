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
import java.util.HashMap;
import java.util.Map;

class InsertStatement<T> extends Statement<T> {

    private final Map<String, String> autoIncrements;
    private final MetaObject<T> metaObject;

    static <T> InsertStatement<T> create(Connection connection, String schemaName, Class<T> dataClass) {
        return new InsertStatement<>(new InsertStatementBuilder<>(connection, schemaName, dataClass));
    }

    private InsertStatement(InsertStatementBuilder<T> builder) {
        super(builder);
        this.autoIncrements = builder.autoIncrements();
        this.metaObject = builder.metaObject();
    }

    T execute(Map<String, Object> propertyValues) {
        for (Map.Entry<String, String> entry : autoIncrements.entrySet()) {
            propertyValues.put(entry.getKey(), connection().nextId(entry.getValue()));
        }

        for (Property property : metaObject.persistentProperties()) {
            Object value = propertyValues.get(property.getName());
            setParam(property.getName(), value);
        }

        doExecute();
        T result = metaObject.newInstance(new PropertyValueAdapter(propertyValues));
        if (connection().isLookup(dataClass())) {
            connection().lookupFor(dataClass()).add(result);
        }

        return result;
    }

    private static class PropertyValueAdapter implements PropertyValues {

        private final Map<String, Object> map;

        public PropertyValueAdapter(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public boolean containsValue(String propertyName) {
            return map.containsKey(propertyName);
        }

        @Override
        public Object getValue(String propertyName) {
            return map.get(propertyName);
        }
    }

    private static class InsertStatementBuilder<U> extends StatementBuilder<U> {

        private final Map<String, String> autoIncrements;

        InsertStatementBuilder(Connection connection, String schemaName, Class<U> targetClass) {
            super(connection, schemaName, targetClass);
            this.autoIncrements = new HashMap<>();
            for (Property property : metaObject().persistentProperties()) {
                switch (property.getPropertyInit()) {
                    case AutoIncrement:
                        autoIncrements.put(property.getName(), sequenceName(property.getName()));
                        break;
                    case Manual:
                        break;
                }

                addPropertySetter(property);
            }

            append("insert into ");
            appendTableName();
            append(" (");
            appendColumnNames();
            append(" ) values (");
            appendParameters();
            append(")");
        }

        final Map<String, String> autoIncrements() {
            return autoIncrements;
        }

        private void appendColumnNames() {
            boolean first = true;
            for (String columnName : columnNames()) {
                if (first) {
                    first = false;
                }
                else {
                    append(", ");
                }

                append(columnName);
            }
        }

        private void appendParameters() {
            for (int i = 0; i < columnNames().size(); ++i) {
                if (i > 0) {
                    append(", ");
                }

                append("?");
            }
        }
    }
}
