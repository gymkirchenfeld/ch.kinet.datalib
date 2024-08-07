/*
 * Copyright (C) 2021 - 2024 by Stefan Rothe
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

public abstract class Dict {

    private static final String START_DATE = "startDate";
    private static final String START_TIME = "startTime";
    private static final String END_DATE = "endDate";
    private static final String END_TIME = "endTime";

    public abstract boolean getBoolean(String key, boolean defaultValue);

    public final int getInt(String key) {
        return getInt(key, 0);
    }

    public final LocalDate getDate(String key) {
        return getDate(key, null);
    }

    public final LocalDate getDate(String key, LocalDate defaultValue) {
        return Date.parseLocalDate(getString(key), defaultValue);
    }

    /**
     * Extracts a date interval from the <code>startDate</code> and <code>endDate</code> keys.
     *
     * @return date interval
     */
    public final DateSpan getDateSpan() {
        // default values ensure that DateSpan is invalid if either start or end date are not specified
        return DateSpan.of(getDate(START_DATE, LocalDate.MAX), getDate(END_DATE, LocalDate.MIN));
    }

    public final DateTimeSpan getDateTimeSpan() {
        return DateTimeSpan.of(getDate(START_DATE), getTime(START_TIME), getDate(END_DATE), getTime(END_TIME));
    }

    public final LocalDateTime getDateTime(String keyPrefix) {
        return getDateTime(keyPrefix, null);
    }

    public final LocalDateTime getDateTime(String keyPrefix, LocalDateTime defaultValue) {
        LocalDate date = getDate(keyPrefix + "Date");
        LocalTime time = getTime(keyPrefix + "Time");
        if (date == null || time == null) {
            return defaultValue;
        }

        return LocalDateTime.of(date, time);
    }

    public abstract double getDouble(String key, double defaultValue);

    public final double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    public abstract int getInt(String key, int defaultValue);

    public final String getString(String key) {
        return getString(key, null);
    }

    public abstract String getString(String key, String defaultValue);

    public final LocalTime getTime(String key) {
        return Dict.this.getTime(key, null);
    }

    public final LocalTime getTime(String key, LocalTime defaultValue) {
        return Date.parseLocalTime(getString(key), defaultValue);
    }

    public abstract boolean hasKey(String key);

    public abstract boolean isEmpty();
}
