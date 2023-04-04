/*
 * Copyright (C) 2018 - 2021 by Stefan Rothe
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

import ch.kinet.reflect.Property;
import java.util.Map;
import java.util.Set;

public class UpdateMultiStatement<T> extends Statement<T> {

    private final Set<String> propertyNames;

    static <T> UpdateMultiStatement<T> create(Connection connection, String schemaName,
                                              Class<T> dataClass, Set<String> propertyNames,
                                              Condition where) {
        return new UpdateMultiStatement<>(
            new UpdateMultiStatementBuilder<>(connection, schemaName, dataClass, propertyNames, where), propertyNames);
    }

    @SuppressWarnings("unchecked")
    void execute(Map<String, Object> properties) {
        for (String propertyName : properties.keySet()) {
            if (propertyNames.contains(propertyName)) {
                setParam(propertyName, properties.get(propertyName));
            }
        }

        doExecute();
    }

    private UpdateMultiStatement(UpdateMultiStatementBuilder<T> builder, Set<String> propertyNames) {
        super(builder);
        this.propertyNames = propertyNames;
    }

    private static class UpdateMultiStatementBuilder<U> extends StatementBuilder<U> {

        public UpdateMultiStatementBuilder(Connection connection, String schemaName, Class<U> targetClass,
                                           Set<String> propertyNames, Condition where) {
            super(connection, schemaName, targetClass);
            for (Property property : metaObject().persistentProperties()) {
                if (propertyNames.contains(property.getName())) {
                    addPropertySetter(property);
                }
            }

            append("update ");
            appendTableName();
            append(" set ");
            boolean first = true;
            for (String columnName : columnNames()) {
                if (first) {
                    first = false;
                }
                else {
                    append(", ");
                }

                append(columnName);
                append(" = ?");
            }

            appendWhereClause(where);
        }
    }
}
