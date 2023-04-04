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
import java.util.Set;

public class UpdateStatement<T> extends Statement<T> {

    private final Set<String> propertyNames;

    static <T> UpdateStatement<T> create(Connection connection, String schemaName,
                                         Class<T> dataClass, Set<String> propertyNames) {
        return new UpdateStatement<>(
            new UpdateStatementBuilder<>(connection, schemaName, dataClass, propertyNames), propertyNames);
    }

    @SuppressWarnings("unchecked")
    void execute(T object) {
        final MetaObject<T> metaObject = MetaObject.forClass((Class<T>) object.getClass());
        for (Property property : metaObject.persistentProperties()) {
            if (property.isKey() ||
                (property.isWritable() &&
                (propertyNames == null || propertyNames.contains(property.getName())))) {
                setParam(property.getName(), property.getValue(object));
            }
        }

        doExecute();
    }

    private UpdateStatement(UpdateStatementBuilder<T> builder, Set<String> propertyNames) {
        super(builder);
        this.propertyNames = propertyNames;
    }

    private static class UpdateStatementBuilder<U> extends StatementBuilder<U> {

        public UpdateStatementBuilder(Connection connection, String schemaName, Class<U> targetClass,
                                      Set<String> propertyNames) {
            super(connection, schemaName, targetClass);
            for (Property property : metaObject().persistentProperties()) {
                if (property.isWritable() && !metaObject().keyProperties().contains(property) &&
                    (propertyNames == null || propertyNames.contains(property.getName()))) {
                    addPropertySetter(property);
                }
            }

            int lastValuePropertyIndex = columnNames().size();

            for (Property property : metaObject().keyProperties()) {
                addPropertySetter(property);
            }

            append("update ");
            appendTableName();
            append(" set ");
            boolean first = true;
            for (int i = 0; i < lastValuePropertyIndex; ++i) {
                if (first) {
                    first = false;
                }
                else {
                    append(", ");
                }

                append(columnNames().get(i));
                append(" = ?");
            }

            append(" where ");
            first = true;
            for (int i = lastValuePropertyIndex; i < columnNames().size(); ++i) {
                if (first) {
                    first = false;
                }
                else {
                    append(" and ");
                }

                append(columnNames().get(i));
                append(" = ?");
            }
        }
    }
}
