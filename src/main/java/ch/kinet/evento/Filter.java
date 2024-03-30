/*
 * Copyright (C) 2017 - 2021 by Stefan Rothe, Sebastian Forster, Tom Jampen
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
package ch.kinet.evento;

public final class Filter {

    public enum Operator {
        And, Or, Single
    }

    private final Operator operator;
    private final String[] texts;

    public static Filter and(String... texts) {
        return new Filter(Operator.And, texts);
    }

    public static Filter equal(String text) {
        String[] texts = {text};
        return new Filter(Operator.Single, texts);
    }

    public static Filter or(String... texts) {
        return new Filter(Operator.Or, texts);
    }

    private Filter(Operator operator, String[] texts) {
        this.operator = operator;
        this.texts = texts;
    }

    public Operator getOperator() {
        return operator;
    }

    public String[] getTexts() {
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(operator);
        result.append("Filter(");
        boolean first = true;
        for (String text : texts) {
            if (first) {
                first = false;
            }
            else {
                result.append(", ");
            }

            result.append(text);
        }

        return result.toString();
    }
}
