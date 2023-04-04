/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class Time implements Comparable<Time> {

    private static final ThreadLocal<SimpleDateFormat> PRINT_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH.mm");
            //return new SimpleDateFormat("kk.mm");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> HM_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm");
            //return new SimpleDateFormat("kk:mm");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> HMS_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> ICALENDAR_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HHmmss");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> ISO8601_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            // example: 2012-04-23T18:25:43.511Z
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        }
    };
    private static final int DAY = 25;
    private static final int MONTH = 9;
    private static final int YEAR = 1974;
    private final Calendar calendar;

    public static Time create(int hour, int minute, int second) {
        return create(hour, minute, second, 0);
    }

    public static Time create(int hour, int minute, int second, int millisecond) {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return new Time(calendar);
    }

    public static Time from(java.sql.Time value) {
        if (value == null) {
            return null;
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(value);
        return new Time(calendar);
    }

    public static Time now() {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(calendar);
    }

    public static Time parseHM(String value, Time defaultValue) {
        if (Util.isEmpty(value)) {
            return null;
        }

        try {
            final Calendar calendar = new GregorianCalendar();
            calendar.setTime(HM_FORMAT.get().parse(value));
            return new Time(calendar);
        }
        catch (ParseException ex) {
            return defaultValue;
        }
    }

    /**
     * Parses a time from an ISO 8601 string. The string must have the format 'yyyy-MM-dd'T'HH:mm:ss.SSS'Z''. Returns
     * <code>defaultValue</code> if the string does not represent a valid time.
     *
     * @param value string to be parsed
     * @return date represented by value or <code>defaultValue</code>
     */
    public static Time parseISO8601(String value, Time defaultValue) {
        if (Util.isEmpty(value)) {
            return null;
        }

        try {
            final Calendar calendar = new GregorianCalendar();
            calendar.setTime(ISO8601_FORMAT.get().parse(value));
            return new Time(calendar);
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
        catch (final ParseException ex) {
            return defaultValue;
        }
    }

    private Time(Calendar calendar) {
        this.calendar = calendar;
        this.calendar.set(YEAR, MONTH, DAY);
    }

    public Time addHours(int amount) {
        return add(Calendar.HOUR_OF_DAY, amount);
    }

    public Time addMilliseconds(int amount) {
        return add(Calendar.MILLISECOND, amount);
    }

    public Time addMinutes(int amount) {
        return add(Calendar.MINUTE, amount);
    }

    public Time addSeconds(int amount) {
        return add(Calendar.SECOND, amount);
    }

    public boolean after(Time other) {
        return calendar.after(other.calendar);
    }

    public boolean before(Time other) {
        return calendar.before(other.calendar);
    }

    @Override
    public int compareTo(Time other) {
        return calendar.compareTo(other.calendar);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Time) {
            final Time other = (Time) object;
            return !(this.before(other) || this.after(other));
        }
        else {
            return super.equals(object);
        }
    }

    public String formatHM() {
        return HM_FORMAT.get().format(toJavaDate());
    }

    public String formatHMS() {
        return HMS_FORMAT.get().format(toJavaDate());
    }

    public String formatICalendar() {
        return ICALENDAR_FORMAT.get().format(toJavaDate());
    }

    public String formatISO8601() {
        return ISO8601_FORMAT.get().format(toJavaDate());
    }

    public String formatPrint() {
        return PRINT_FORMAT.get().format(toJavaDate());
    }

    public int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinutes() {
        return calendar.get(Calendar.MINUTE);
    }

    public int getSeconds() {
        return calendar.get(Calendar.SECOND);
    }

    public int getMilliseconds() {
        return calendar.get(Calendar.MILLISECOND);
    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }

    public java.util.Date toJavaDate() {
        return calendar.getTime();
    }

    public java.sql.Time toSqlTime() {
        return new java.sql.Time(calendar.getTimeInMillis());
    }

    @Override
    public String toString() {
        return formatHMS();
    }

    private Time add(int field, int amount) {
        final Calendar result = new GregorianCalendar();
        result.setTimeInMillis(calendar.getTimeInMillis());
        result.add(field, amount);
        return new Time(result);
    }
}
