/*
 * Copyright (C) 2012 - 2024 by Stefan Rothe
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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

class Statement<T> {

    private final Connection connection;
    private final Class<T> dataClass;
    private final Map<String, ParameterSetter> parameterSetters;
    private final String sql;
    final PreparedStatement statement;

    protected Statement(StatementBuilder<T> builder) {
        this.connection = builder.connection();
        this.dataClass = builder.dataClass();
        this.parameterSetters = builder.parameterSetters();
        this.sql = builder.sql();
        this.statement = connection.prepareStatement(this.sql);
        final List<BoundParameterSetter> boundParameterSetters = builder.boundParameterSetters();
        for (BoundParameterSetter element : boundParameterSetters) {
            element.set(this.statement);
        }
    }

    @Override
    public String toString() {
        return sql;
    }

    void dispose() {
        try {
            statement.close();
        }
        catch (SQLException ex) {
            // ignore
        }
    }

    void setParam(String name, Object value) {
        final ParameterSetter setter = parameterSetters.get(name);
        if (setter != null) {
            setter.setValue(statement, value);
        }
        else {
            System.out.println("INFO: No setter for property '" + name + "'.");
        }
    }

    protected final Connection connection() {
        return connection;
    }

    protected final Class<T> dataClass() {
        return dataClass;
    }

    protected void doExecute() {
        try {
            statement.execute();
        }
        catch (SQLException ex) {
            throw new StatementExecutionException(sql, ex);
        }
    }

    static class BoundParameterSetter {

        private final ParameterSetter setter;
        private final Object value;

        BoundParameterSetter(ParameterSetter setter, Object value) {
            this.setter = setter;
            this.value = value;
        }

        void set(PreparedStatement statement) {
            this.setter.setValue(statement, value);
        }
    }
}
