/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe, Sebastian Forster
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class Timestamp implements Comparable<Timestamp> {

    private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> ICALENDAR_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> ISO8601_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            // example: 2012-04-23T18:25:43.511Z
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        }
    };
    private static final DateFormat DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    private final Calendar calendar;

    public static Timestamp create(Date date, Time time) {
        if (date == null) {
            throw new NullPointerException("date");
        }

        if (time == null) {
            throw new NullPointerException("time");
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinutes());
        calendar.set(Calendar.SECOND, time.getSeconds());
        calendar.set(Calendar.MILLISECOND, time.getMilliseconds());
        return new Timestamp(calendar);
    }

    public static Timestamp from(long value) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(value);
        return new Timestamp(calendar);
    }

    public static Timestamp from(java.util.Date value) {
        if (value == null) {
            return null;
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(value);
        return new Timestamp(calendar);
    }

    public static Timestamp from(java.sql.Timestamp value) {
        if (value == null) {
            return null;
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(value.getTime());
        return new Timestamp(calendar);
    }

    public static Timestamp now() {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Timestamp(calendar);
    }

    public static Timestamp parseISO8601(String value, Timestamp defaultValue) {
        if (Util.isEmpty(value)) {
            return defaultValue;
        }

        try {
            java.util.Date date = ISO8601_FORMAT.get().parse(value);
            final Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return new Timestamp(calendar);
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
        catch (final ParseException ex) {
            return defaultValue;
        }
    }

    private Timestamp(Calendar calendar) {
        this.calendar = calendar;
    }

    public Timestamp addDays(int amount) {
        return add(Calendar.DATE, amount);
    }

    public Timestamp addHours(int amount) {
        return add(Calendar.HOUR_OF_DAY, amount);
    }

    public Timestamp addMinutes(int amount) {
        return add(Calendar.MINUTE, amount);
    }

    public Timestamp addMonths(int amount) {
        return add(Calendar.MONTH, amount);
    }

    public Timestamp addSeconds(int amount) {
        return add(Calendar.SECOND, amount);
    }

    public Timestamp addYears(int amount) {
        return add(Calendar.YEAR, amount);
    }

    public boolean after(Timestamp other) {
        return calendar.after(other.calendar);
    }

    public boolean before(Timestamp other) {
        return calendar.before(other.calendar);
    }

    public boolean between(Timestamp first, Timestamp last) {
        return !(calendar.before(first.calendar) || calendar.after(last.calendar));
    }

    @Override
    public int compareTo(Timestamp other) {
        return calendar.compareTo(other.calendar);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Timestamp) {
            final Timestamp other = (Timestamp) object;
            return !(before(other) || after(other));
        }
        else {
            return super.equals(object);
        }
    }

    public String formatTimestamp() {
        return TIMESTAMP_FORMAT.get().format(toJavaDate());
    }

    public String formatDateTime() {
        return DATE_TIME_FORMAT.format(toJavaDate());
    }

    public String formatICalendar() {
        return ICALENDAR_FORMAT.get().format(toJavaDate());
    }

    public String formatISO8601() {
        return ISO8601_FORMAT.get().format(toJavaDate());
    }

    @Deprecated
    public String formatJSON() {
        return ISO8601_FORMAT.get().format(toJavaDate());
    }

    public Date getDate() {
        return Date.create(calendar.get(Calendar.DAY_OF_MONTH),
                           calendar.get(Calendar.MONTH) + 1,
                           calendar.get(Calendar.YEAR));
    }

    public Time getTime() {
        return Time.create(calendar.get(Calendar.HOUR_OF_DAY),
                           calendar.get(Calendar.MINUTE),
                           calendar.get(Calendar.SECOND),
                           calendar.get(Calendar.MILLISECOND));
    }

    /**
     * Returns the timestamp in Epoch milliseconds.
     *
     * @return the timestamp in Epoch milliseconds
     */
    public long getEpochMillis() {
        return calendar.getTimeInMillis();
    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }

    public java.util.Date toJavaDate() {
        return calendar.getTime();
    }

    public java.sql.Timestamp toSqlTimestamp() {
        return new java.sql.Timestamp(calendar.getTimeInMillis());
    }

    @Override
    public String toString() {
        return formatTimestamp();
    }

    private Timestamp add(int field, int amount) {
        final Calendar result = new GregorianCalendar();
        result.setTimeInMillis(calendar.getTimeInMillis());
        result.add(field, amount);
        return new Timestamp(result);
    }
}
