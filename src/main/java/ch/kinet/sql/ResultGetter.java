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

import ch.kinet.Binary;
import ch.kinet.reflect.Property;
import java.sql.Array;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

abstract class ResultGetter {

    protected final Property property;

    static ResultGetter create(Connection connection, Property property) {
        return create(connection, property, StatementBuilder.sqlName(property.getName()));
    }

    static ResultGetter create(Connection connection, Property property, String columnName) {
        if (connection.isLookup(property.getPropertyClass())) {
            Property key = property.getType().keyProperty();
            if (key == null) {
                throw new UnsupportedPropertyTypeException(property);
            }

            return new LookupGetter(connection, property, key);
        }

        ResultGetter result = createSimple(property, columnName);
        if (result == null) {
            throw new UnsupportedPropertyTypeException(property);
        }

        return result;
    }

    private static ResultGetter createSimple(Property property, String columnName) {
        Class<?> propertyClass = property.getPropertyClass();
        if (propertyClass.equals(Binary.class)) {
            return new BinaryGetter(property, columnName);
        }      
        else if (propertyClass.equals(Optional.class)) {
            return new OptionalGetter(property, columnName);
        }
        else if (propertyClass.equals(Boolean.TYPE)) {
            return new BooleanGetter(property, columnName);
        }
        else if (propertyClass.equals(Double.TYPE)) {
            return new DoubleGetter(property, columnName);
        }
        else if (propertyClass.equals(Integer.TYPE)) {
            return new IntGetter(property, columnName);
        }
        else if (propertyClass.equals(LocalDate.class)) {
            return new LocalDateGetter(property, columnName);
        }
        else if (propertyClass.equals(LocalDateTime.class)) {
            return new LocalDateTimeGetter(property, columnName);
        }
        else if (propertyClass.equals(LocalTime.class)) {
            return new LocalTimeGetter(property, columnName);
        }
        else if (propertyClass.equals(Long.TYPE)) {
            return new LongGetter(property, columnName);
        }
        else if (Stream.class.isAssignableFrom(propertyClass)) {
            return new StreamGetter(property, columnName);
        }
        else if (Set.class.isAssignableFrom(propertyClass)) {
            return new SetGetter(property, columnName);
        }
        else if (List.class.isAssignableFrom(propertyClass)) {
            return new ListGetter(property, columnName);
        }
        else if (propertyClass.equals(String.class)) {
            return new StringGetter(property, columnName);
        }
        else if (propertyClass.equals(UUID.class)) {
            return new UUIDGetter(property, columnName);
        }
        else {
            return null;
        }
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

    final Object getValue(ResultSet resultSet) {
        try {
            return doGetValue(resultSet);
        }
        catch (Exception ex) {
            throw new ResultGetterException(property, ex);
        }
    }

    protected abstract Object doGetValue(ResultSet resultSet) throws Exception;

    private static class LookupGetter extends ResultGetter {

        private final Lookup lookup;
        private final ResultGetter retriever;

        public LookupGetter(Connection connection, Property property, Property key) {
            super(property);
            String columnName = StatementBuilder.sqlName(property.getName() + key.getName());
            this.lookup = connection.lookupFor(property.getPropertyClass());
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

    private static class OptionalGetter extends ValueGetter {

        public OptionalGetter(Property property, String columnName) {
            super(property, columnName);               
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            Object value = resultSet.getObject(columnName);     
            return value == null ? Optional.empty() : Optional.of(value);        
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
            Array array = resultSet.getArray(columnName);
            if (array == null) {
                return null;
            }

            return Arrays.asList((Object[]) array.getArray());
        }
    }

    private static class LocalDateGetter extends ValueGetter {

        public LocalDateGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            java.sql.Date value = resultSet.getDate(columnName);
            return value == null ? null : value.toLocalDate();
        }
    }

    private static class LocalDateTimeGetter extends ValueGetter {

        public LocalDateTimeGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            java.sql.Timestamp value = resultSet.getTimestamp(columnName);
            return value == null ? null : value.toLocalDateTime();
        }
    }

    private static class LocalTimeGetter extends ValueGetter {

        public LocalTimeGetter(Property property, String columnName) {
            super(property, columnName);
        }

        @Override
        protected Object doGetValue(ResultSet resultSet) throws Exception {
            java.sql.Time value = resultSet.getTime(columnName);
            return value == null ? null : value.toLocalTime();
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
            Array array = resultSet.getArray(columnName);
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
            Array array = resultSet.getArray(columnName);
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
