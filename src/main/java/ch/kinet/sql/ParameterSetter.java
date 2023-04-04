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
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class ParameterSetter {

    protected final Property property;

    static ParameterSetter createSimple(Connection connection, Property property, int index) {
        final MetaObject<?> propertyType = property.getType();
        if (propertyType.isClass(Binary.class)) {
            return new BinarySetter(property, index);
        }
        else if (propertyType.isBoolean()) {
            return new BooleanSetter(property, index);
        }
        else if (propertyType.isAssignableTo(Collection.class)) {
            return new CollectionSetter(connection, property, index);
        }
        else if (propertyType.isClass(Date.class)) {
            return new DateSetter(property, index);
        }
        else if (propertyType.isDouble()) {
            return new DoubleSetter(property, index);
        }
        else if (propertyType.isInteger()) {
            return new IntSetter(property, index);
        }
        else if (propertyType.isLong()) {
            return new LongSetter(property, index);
        }
        else if (propertyType.isAssignableTo(Stream.class)) {
            return new StreamSetter(connection, property, index);
        }
        else if (propertyType.isClass(String.class)) {
            return new StringSetter(property, index);
        }
        else if (propertyType.isClass(Time.class)) {
            return new TimeSetter(property, index);
        }
        else if (propertyType.isClass(Timestamp.class)) {
            return new TimestampSetter(property, index);
        }
        else if (propertyType.isClass(UUID.class)) {
            return new UUIDSetter(property, index);
        }
        throw new UnsupportedPropertyTypeException(property);
    }

    static ParameterSetter createSingleKey(Connection connection, Property property, ParameterSetter keySetter) {
        return new SingleKeySetter(connection, property, keySetter);
    }

    protected ParameterSetter(Property property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return property.getFullName();
    }

    final void setValue(PreparedStatement statement, Object value) {
        try {
            if (value == null) {
                doSetNull(statement);
            }
            else {
                doSetValue(statement, value);
            }
        }
        catch (final Exception ex) {
            throw new SetParameterException(property, ex);
        }
    }

    protected abstract void doSetNull(PreparedStatement statement) throws Exception;

    protected abstract void doSetValue(PreparedStatement statement, Object value) throws Exception;

    private static class SingleKeySetter extends ParameterSetter {

        private final ParameterSetter keySetter;

        public SingleKeySetter(Connection connection, Property property, ParameterSetter keySetter) {
            super(property);
            this.keySetter = keySetter;
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            keySetter.setValue(statement, null);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            keySetter.setValue(statement, keySetter.property.getValue(value));
        }
    }

    private abstract static class ValueSetter extends ParameterSetter {

        protected final int index;

        ValueSetter(Property property, int index) {
            super(property);
            this.index = index;
        }
    }

    private static class BinarySetter extends ValueSetter {

        public BinarySetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.BINARY);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            final Binary binaryValue = (Binary) value;
            if (binaryValue.isNull()) {
                doSetNull(statement);
            }
            else {
                statement.setBytes(index, ((Binary) value).toBytes());
            }
        }
    }

    private static class BooleanSetter extends ValueSetter {

        public BooleanSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.BOOLEAN);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setBoolean(index, (Boolean) value);
        }
    }

    private static class CollectionSetter extends ValueSetter {

        private final Connection connection;

        public CollectionSetter(Connection connection, Property property, int index) {
            super(property, index);
            this.connection = connection;
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.ARRAY);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setArray(index, connection.createArrayOf((Collection) value));
        }
    }

    private static class DateSetter extends ValueSetter {

        public DateSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.DATE);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setDate(index, ((Date) value).toSqlDate());
        }
    }

    private static class DoubleSetter extends ValueSetter {

        public DoubleSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.DOUBLE);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setDouble(index, (Double) value);
        }
    }

    private static class IntSetter extends ValueSetter {

        public IntSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.INTEGER);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setInt(index, (Integer) value);
        }
    }

    private static class LongSetter extends ValueSetter {

        public LongSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.BIGINT);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setLong(index, (Long) value);
        }
    }

    private static class StreamSetter extends ValueSetter {

        private final Connection connection;

        public StreamSetter(Connection connection, Property property, int index) {
            super(property, index);
            this.connection = connection;
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.ARRAY);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            Object object = ((Stream) value).collect(Collectors.toList());
            statement.setArray(index, connection.createArrayOf((Collection) object));
        }
    }

    private static class StringSetter extends ValueSetter {

        public StringSetter(Property property, final int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.VARCHAR);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setString(index, sanitizeString((String) value));
        }

        /**
         * Null characters may not occur in a database string (PostgreSQL). So we escape those (Yes, they can occur,
         * e.g. in Active Directory LDAP error messages!)
         */
        private static String sanitizeString(String value) {
            return value.replaceAll("\\x00", "");
        }
    }

    private static class TimeSetter extends ValueSetter {

        public TimeSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.TIME);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setTime(index, ((Time) value).toSqlTime());
        }
    }

    private static class TimestampSetter extends ValueSetter {

        public TimestampSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.TIMESTAMP);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setTimestamp(index, ((Timestamp) value).toSqlTimestamp());
        }
    }

    private static class UUIDSetter extends ValueSetter {

        public UUIDSetter(Property property, int index) {
            super(property, index);
        }

        @Override
        protected void doSetNull(PreparedStatement statement) throws Exception {
            statement.setNull(index, Types.OTHER);
        }

        @Override
        protected void doSetValue(PreparedStatement statement, Object value) throws Exception {
            statement.setObject(index, (UUID) value);
        }
    }
}
