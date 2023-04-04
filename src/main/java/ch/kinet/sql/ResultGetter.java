/*
 * Copyright (C) 2012 - 2023 by Stefan Rothe
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

import ch.kinet.Binary;
import ch.kinet.Date;
import ch.kinet.Time;
import ch.kinet.Timestamp;
import ch.kinet.reflect.MetaObject;
import ch.kinet.reflect.Property;
import java.sql.Array;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

abstract class ResultGetter {

    protected final Property property;

    static ResultGetter create(Connection connection, Property property) {
        return create(connection, property, StatementBuilder.sqlName(property.getName()));
    }

    static ResultGetter create(Connection connection, Property property, String columnName) {
        final Property key = property.getType().keyProperty();
        if (key == null) {
            return createSimple(property, columnName);
        }
        else {
            return new SingleKeyGetter(connection, property, key);
        }
    }

    private static ResultGetter createSimple(Property property, String columnName) {
        final MetaObject<?> propertyType = property.getType();
        if (propertyType.isClass(Binary.class)) {
            return new BinaryGetter(property, columnName);
        }
        else if (propertyType.isBoolean()) {
            return new BooleanGetter(property, columnName);
        }
        else if (propertyType.isClass(Date.class)) {
            return new DateGetter(property, columnName);
        }
        else if (propertyType.isDouble()) {
            return new DoubleGetter(property, columnName);
        }
        else if (propertyType.isInteger()) {
            return new IntGetter(property, columnName);
        }
        else if (propertyType.isLong()) {
            return new LongGetter(property, columnName);
        }
        else if (propertyType.isAssignableTo(Stream.class)) {
            return new StreamGetter(property, columnName);
        }
        else if (propertyType.isAssignableTo(Set.class)) {
            return new SetGetter(property, columnName);
        }
        else if (propertyType.isAssignableTo(List.class)) {
            return new ListGetter(property, columnName);
        }
        else if (propertyType.isClass(String.class)) {
            return new StringGetter(property, columnName);
        }
        else if (propertyType.isClass(Time.class)) {
            return new TimeGetter(property, columnName);
        }
        else if (propertyType.isClass(Timestamp.class)) {
            return new TimestampGetter(property, columnName);
        }
        else if (propertyType.isClass(UUID.class)) {
            return new UUIDGetter(property, columnName);
        }
        throw new UnsupportedPropertyTypeException(property);
    }

    protected ResultGetter(Property property) {
        this.property = property;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(property.getName());
        result.append('(');
        result.append(columnName());
        result.append(')');
        return result.toString();
    }

    abstract String columnName();

    final String propertyName() {
        return property.getName();
    }

    final Object getValue(ResultSet resultSet) {
        try {
            return doGetValue(resultSet);
        }
        catch (Exception ex) {
            throw new ResultGetterException(property, ex);
        }
    }

    protected abstract Object doGetValue(ResultSet resultSet) throws Exception;

    private static class SingleKeyGetter extends ResultGetter {

        private final Lookup lookup;
        private final ResultGetter retriever;

        public SingleKeyGetter(Connection connection, Property property, Property key) {
            super(property);
            final String columnName = StatementBuilder.sqlName(property.getName() + key.getName());
            this.lookup = connection.lookupFor(property.getType());
            if (this.lookup == null) {
                throw new MissingLookupException(property.getType());
            }

            this.retriever = create(connection, key, columnName);
        }

        @Override
        String columnName() {
            return retriever.columnName();
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return lookup.get(retriever.getValue(resultSet));
        }
    }

    private abstract static class ValueGetter extends ResultGetter {

        protected final String columnName;

        ValueGetter(Property property, String columnName) {
            super(property);
            this.columnName = columnName;
        }

        @Override
        String columnName() {
            return columnName;
        }
    }

    private static class BinaryGetter extends ValueGetter {

        public BinaryGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return Binary.from(resultSet.getBytes(columnName));
        }
    }

    private static class BooleanGetter extends ValueGetter {

        public BooleanGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return resultSet.getBoolean(columnName);
        }
    }

    private static class DateGetter extends ValueGetter {

        public DateGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return Date.from(resultSet.getDate(columnName));
        }
    }

    private static class DoubleGetter extends ValueGetter {

        public DoubleGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return resultSet.getDouble(columnName);
        }
    }

    private static class IntGetter extends ValueGetter {

        public IntGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return resultSet.getInt(columnName);
        }
    }

    private static class ListGetter extends ValueGetter {

        public ListGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            final Array array = resultSet.getArray(columnName);
            if (array == null) {
                return null;
            }

            return Arrays.asList((Object[]) array.getArray());
        }
    }

    private static class LongGetter extends ValueGetter {

        public LongGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return resultSet.getLong(columnName);
        }
    }

    private static class SetGetter extends ValueGetter {

        public SetGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            final Array array = resultSet.getArray(columnName);
            if (array == null) {
                return null;
            }

            return new HashSet<>(Arrays.asList((Object[]) array.getArray()));
        }
    }

    private static class StreamGetter extends ValueGetter {

        public StreamGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            final Array array = resultSet.getArray(columnName);
            if (array == null) {
                return Stream.empty();
            }

            return Arrays.asList((Object[]) array.getArray()).stream();
        }
    }

    private static class StringGetter extends ValueGetter {

        public StringGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return resultSet.getString(columnName);
        }
    }

    private static class TimeGetter extends ValueGetter {

        public TimeGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return Time.from(resultSet.getTime(columnName));
        }
    }

    private static class TimestampGetter extends ValueGetter {

        public TimestampGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return Timestamp.from(resultSet.getTimestamp(columnName));
        }
    }

    private static class UUIDGetter extends ValueGetter {

        public UUIDGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            return UUID.fromString(resultSet.getString(columnName));
        }
    }
}
