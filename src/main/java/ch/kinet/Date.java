/*
 * Copyright (C) 2024 by Stefan Rothe
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
package ch.kinet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.Locale;

public final class Date {

    private static final DateTimeFormatter HM_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DMY_FORMAT = DateTimeFormatter.ofPattern("dd.MM.YYYY");
    private static final DateTimeFormatter LONG_FORMAT = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy");
    private static final DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern("EE dd.MM.YYYY");
    private static final DateTimeFormatter TEXT_FORMAT = DateTimeFormatter.ofPattern("d. MMMM yyyy");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public static String formatDMY(LocalDate date) {
        return date == null ? null : date.format(DMY_FORMAT);
    }

    public static String formatHM(LocalTime time) {
        return time == null ? null : time.format(HM_FORMAT);
    }

    public static String formatLong(LocalDate date) {
        return date == null ? null : date.format(LONG_FORMAT);
    }

    public static String formatShort(LocalDate date) {
        return date == null ? null : date.format(SHORT_FORMAT);
    }

    public static String formatText(LocalDate date) {
        return date == null ? null : date.format(TEXT_FORMAT);
    }

    public static String formatTimestamp(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(TIMESTAMP_FORMAT);
    }

    public static int getWeekOfYear(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end) {
        return !(date.isBefore(start) || date.isAfter(end));
    }

    public static boolean isBetween(LocalDateTime moment, LocalDateTime start, LocalDateTime end) {
        return !(moment.isBefore(start) || moment.isAfter(end));
    }

    public static LocalDate parseLocalDate(String value, LocalDate defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException ex) {
            return defaultValue;
        }
    }

    public static LocalDateTime parseLocalDateTime(String value, LocalDateTime defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return LocalDateTime.parse(value);
        }
        catch (DateTimeParseException ex) {
            return defaultValue;
        }
    }

    public static LocalTime parseLocalTime(String value, LocalTime defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            return LocalTime.parse(value);
        }
        catch (DateTimeParseException ex) {
            return defaultValue;
        }
    }
}
