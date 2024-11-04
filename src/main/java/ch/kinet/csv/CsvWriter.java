/*
 * Copyright (C) 2022 - 2024 by Stefan Rothe
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
package ch.kinet.csv;

import ch.kinet.Data;
import ch.kinet.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CsvWriter {

    public static CsvWriter create(int columnCount) {
        return new CsvWriter(columnCount);
    }

    public static CsvWriter create(Stream<String> headers) {
        List<String> list = headers.collect(Collectors.toList());
        CsvWriter result = new CsvWriter(list.size());
        list.forEach(title -> result.append(title));
        return result;
    }

    private final StringBuilder csv = new StringBuilder();
    private final int count;
    private int column;
    private boolean hideZero;

    private CsvWriter(int columnCount) {
        count = columnCount - 1;
    }

    public Data toData(String fileName) {
        return Data.csv(csv.toString(), fileName);
    }

    public void append() {
        nextColumn();
    }

    public void append(LocalDate content) {
        if (content != null) {
            csv.append(Date.formatDMY(content));
        }

        nextColumn();
    }

    public void append(double content) {
        if (!hideZero || content != 0) {
            csv.append(content);
        }

        nextColumn();
    }

    public void append(int content) {
        if (!hideZero || content != 0) {
            csv.append(content);
        }

        nextColumn();
    }

    public void append(String content) {
        if (content != null && !content.isEmpty()) {
            csv.append('"');
            csv.append(content);
            csv.append('"');
        }

        nextColumn();
    }

    public void setHideZero(boolean hideZero) {
        this.hideZero = hideZero;
    }

    @Override
    public String toString() {
        return csv.toString();
    }

    private void nextColumn() {
        if (column < count) {
            csv.append(';');
            ++column;
        }
        else {
            csv.append('\n');
            column = 0;
        }
    }
}
