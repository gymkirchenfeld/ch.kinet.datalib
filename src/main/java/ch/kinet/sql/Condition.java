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

import java.util.Collection;
import ch.kinet.DateInterval;

public abstract class Condition {

    public static Condition and(Condition... conditions) {
        return new And(conditions);
    }

    public static Condition and(Collection<Condition> conditions) {
        return new And(conditions.toArray(new Condition[0]));
    }

    public static Condition between(String parameterName, Object lowerBound, Object upperBound) {
        return new Between(parameterName, lowerBound, upperBound);
    }

    public static Condition during(String parameterName, DateInterval duration) {
        if (duration.getEndDate() == null) {
            return greaterOrEqual(parameterName, duration.getStartDate());
        }
        else {
            if (duration.getStartDate() == null) {
                return smallerOrEqual(parameterName, duration.getEndDate());
            }
            else {
                return between(parameterName, duration.getStartDate(), duration.getEndDate());
            }
        }
    }

    public static Condition equals(String propertyName, Object value) {
        return new Equals(propertyName, value);
    }

    public static Condition greater(String propertyName, Object value) {
        return new Greater(propertyName, value);
    }

    public static Condition greaterOrEqual(String propertyName, Object value) {
        return new GreaterOrEqual(propertyName, value);
    }

    public static Condition ilike(String propertyName, String value) {
        return new ILike(propertyName, value);
    }

    public static Condition isNull(String propertyName) {
        return new IsNull(propertyName);
    }

    public static Condition not(Condition condition) {
        return new Not(condition);
    }

    public static Condition notEquals(String propertyName, Object value) {
        return new NotEquals(propertyName, value);
    }

    public static Condition or(Condition... conditions) {
        return new Or(conditions);
    }

    public static Condition smaller(String propertyName, Object value) {
        return new Smaller(propertyName, value);
    }

    public static Condition smallerOrEqual(String propertyName, Object value) {
        return new SmallerOrEqual(propertyName, value);
    }

    abstract <T> void visit(StatementBuilder<T> builder);

    protected Condition() {
    }

    private static class And extends Condition {

        private final Condition[] children;

        And(Condition[] children) {
            this.children = children;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            boolean first = true;
            for (Condition child : children) {
                if (first) {
                    first = false;
                }
                else {
                    builder.append(" and ");
                }

                builder.append("(");
                child.visit(builder);
                builder.append(")");
            }
        }
    }

    private static class Between extends Condition {

        private final Object lowerBound;
        private final String propertyName;
        private final Object upperBound;

        Between(String propertyName, Object lowerBound, Object upperBound) {
            this.lowerBound = lowerBound;
            this.propertyName = propertyName;
            this.upperBound = upperBound;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" between ");
            builder.appendBoundParameter(propertyName, lowerBound);
            builder.append(" and ");
            builder.appendBoundParameter(propertyName, upperBound);
        }
    }

    private static class Equals extends Condition {

        private final String propertyName;
        private final Object value;

        Equals(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" = ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class Greater extends Condition {

        private final String propertyName;
        private final Object value;

        Greater(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" > ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class GreaterOrEqual extends Condition {

        private final String propertyName;
        private final Object value;

        GreaterOrEqual(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" >= ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class ILike extends Condition {

        private final String propertyName;
        private final String value;

        ILike(String propertyName, String value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" ilike ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class IsNull extends Condition {

        private final String propertyName;

        IsNull(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" is null");
        }
    }

    private static class Not extends Condition {

        private final Condition condition;

        Not(Condition condition) {
            this.condition = condition;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.append(" not ");
            builder.append("(");
            condition.visit(builder);
            builder.append(")");
        }
    }

    private static class NotEquals extends Condition {

        private final String propertyName;
        private final Object value;

        NotEquals(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" <> ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class Or extends Condition {

        private final Condition[] children;

        Or(Condition[] children) {
            this.children = children;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            boolean first = true;
            for (Condition child : children) {
                if (first) {
                    first = false;
                }
                else {
                    builder.append(" or ");
                }

                builder.append("(");
                child.visit(builder);
                builder.append(")");
            }
        }
    }

    private static class Smaller extends Condition {

        private final String propertyName;
        private final Object value;

        Smaller(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" < ");
            builder.appendBoundParameter(propertyName, value);
        }
    }

    private static class SmallerOrEqual extends Condition {

        private final String propertyName;
        private final Object value;

        SmallerOrEqual(String propertyName, Object value) {
            this.propertyName = propertyName;
            this.value = value;
        }

        @Override
        <T> void visit(StatementBuilder<T> builder) {
            builder.appendFieldName(propertyName);
            builder.append(" <= ");
            builder.appendBoundParameter(propertyName, value);
        }
    }
}
