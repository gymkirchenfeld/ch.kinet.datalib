/*
 * Copyright (C) 2021 by Stefan Rothe
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

public abstract class Dict {

    public abstract boolean getBoolean(String key, boolean defaultValue);

    public final Date getDate(String key) {
        return getDate(key, null);
    }

    public abstract Date getDate(String key, Date defaultValue);

    public final int getInt(String key) {
        return getInt(key, 0);
    }

    public abstract double getDouble(String key, double defaultValue);

    public final double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    /**
     * Extracts a date span from the <code>startDay</code> and <code>endDay</code> keys.
     *
     * @return date span
     */
    public final DateSpan getDateSpan() {
        return Date.span(getDate("startDate"), getDate("endDate"));
    }

    public abstract int getInt(String key, int defaultValue);

    public final String getString(String key) {
        return getString(key, null);
    }

    public abstract String getString(String key, String defaultValue);

    public final Time getTime(String key) {
        return getTime(key, null);
    }

    public final Time getTime(String key, Time defaultValue) {
        String value = getString(key);
        if (Util.length(value) == 5) {
            return Time.parseHM(value, defaultValue);
        }

        return Time.parseISO8601(value, defaultValue);
    }

    /**
     * Extracts a timestamp from the date and time specified under <code>[keyPrefix]Date</code> and
     * <code>[keyPrefix]Time</code>.
     *
     * @param keyPrefix the key prefix
     * @return the timestamp
     */
    public final Timestamp getTimestamp(String keyPrefix) {
        return getTimestamp(keyPrefix, null);
    }

    /**
     * Extracts a timestamp from the date and time specified under <code>[keyPrefix]Date</code> and
     * <code>[keyPrefix]Time</code>.
     *
     * @param keyPrefix the key prefix
     * @param defaultValue the default value
     * @return the timestamp
     */
    public final Timestamp getTimestamp(String keyPrefix, Timestamp defaultValue) {
        Date day = getDate(keyPrefix + "Date");
        Time time = getTime(keyPrefix + "Time");
        if (day == null || time == null) {
            return defaultValue;
        }

        return Timestamp.create(day, time);
    }

    public abstract boolean hasKey(String key);

    public abstract boolean isEmpty();
}
