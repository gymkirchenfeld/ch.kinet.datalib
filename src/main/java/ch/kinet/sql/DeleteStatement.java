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

class DeleteStatement<T> extends Statement<T> {

    static <S> void execute(Connection connection, String schemaName, Class<S> targetClass, Condition where) {
        DeleteStatement<S> statement = null;
        try {
            statement = create(connection, schemaName, targetClass, where);
            statement.execute();
        }
        finally {
            if (statement != null) {
                statement.dispose();
            }
        }
    }

    private static <T> DeleteStatement<T> create(Connection connection, String schemaName, Class<T> targetClass,
                                                 Condition where) {
        return new DeleteStatement<>(new DeleteStatementBuilder<>(connection, schemaName, targetClass, where));
    }

    private DeleteStatement(DeleteStatementBuilder<T> builder) {
        super(builder);
    }

    public void execute() {
        doExecute();
    }

    private static class DeleteStatementBuilder<U> extends StatementBuilder<U> {

        private final Condition where;

        DeleteStatementBuilder(Connection connection, String schemaName, Class<U> targetClass, Condition where) {
            super(connection, schemaName, targetClass);
            this.where = where;
            build();
        }

        private void build() {
            append("delete from ");
            appendTableName();
            appendWhereClause(where);
        }
    }
}
