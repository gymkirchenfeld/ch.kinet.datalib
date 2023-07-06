/*
 * Copyright (C) 2011 - 2023 by Stefan Rothe, Sebastian Forster
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a date of the Gregorian calendar.
 */
public final class Date implements Comparable<Date> {

    public enum DayOfWeek {
        Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
    };
    private static final Map<Integer, DayOfWeek> DAY_OF_WEEK_MAP = createDayOfWeekMap();
    private static final ThreadLocal<SimpleDateFormat> DMY_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> ICALENDAR_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> ISO8601_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> LONG_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EEEE, d. MMMM yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> TEXT_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("d. MMMM yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> SHORT_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("EE dd.MM.yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> YMD_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };

    private final Calendar calendar;

    /**
     * Creates a date.
     *
     * @param day the day of the month (1 to 31)
     * @param month the month (1 to 12)
     * @param year the year
     *
     * @return the date
     */
    public static Date create(int day, int month, int year) {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(year, month - 1, day);
        return new Date(calendar);
    }

    public static String formatDMY(Date date) {
        return date == null ? "" : DMY_FORMAT.get().format(date.toJavaDate());
    }

    public static Date from(long value) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(value);
        return new Date(calendar);
    }

    public static Date from(java.util.Date value) {
        if (value == null) {
            return null;
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(value);
        return new Date(calendar);
    }

    public static Date from(java.sql.Date value) {
        if (value == null) {
            return null;
        }

        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(value.getTime());
        return new Date(calendar);
    }

    /**
     * Parses a date from a string. The string must have the format 'DD.MM.YYYY'. Returns null if the string is null or
     * empty. Throws a ParseException if the string does not represent a valid date
     *
     * @param value string to be parsed
     * @return date represented by value or null
     * @throws java.text.ParseException if <code>value</code> does not represent a valid date.
     */
    public static Date parseDMY(String value) throws ParseException {
        if (Util.isEmpty(value)) {
            return null;
        }

        return from(DMY_FORMAT.get().parse(value));
    }

    public static DateSpan span(Date startDay, Date endDay) {
        return new DateSpanImp(startDay, endDay);
    }

    /**
     * Parses a date from a string. The string must have the format 'DD.MM.YYYY'. Returns null if the string does not
     * represent a valid date.
     *
     * @param value string to be parsed
     * @return date represented by value or null
     */
    public static Date tryParseDMY(String value) {
        try {
            return parseDMY(value);
        }
        catch (final NumberFormatException ex) {
            return null;
        }
        catch (final ParseException ex) {
            return null;
        }
    }

    /**
     * Parses a date from an ISO 8601 string. The string must have the format 'yyyy-MM-dd'. Returns null if the string
     * does not represent a valid date.
     *
     * @param value string to be parsed
     * @return date represented by value or null
     */
    public static Date tryParseISO8601(String value, Date defaultValue) {
        if (Util.isEmpty(value)) {
            return defaultValue;
        }

        try {
            return from(ISO8601_FORMAT.get().parse(value));
        }
        catch (final NumberFormatException ex) {
            return defaultValue;
        }
        catch (final ParseException ex) {
            return defaultValue;
        }
    }

    /**
     * Parses a date from a string. The string must have the format 'YYYY-MM-DD'. Returns null if the string is null or
     * empty. Throws a ParseException if the string does not represent a valid date
     *
     * @param value string to be parsed
     * @return date represented by value or null
     * @throws java.text.ParseException if <code>value</code> does not represent a valid date.
     */
    public static Date parseYMD(String value) throws ParseException {
        if (Util.isEmpty(value)) {
            return null;
        }

        return from(YMD_FORMAT.get().parse(value));
    }

    /**
     * Parses a date from a string. The string must have the format 'YYYY-MM-DD'. Returns null if the string does not
     * represent a valid date.
     *
     * @param value string to be parsed
     * @return date represented by value or null
     */
    public static Date tryParseYMD(String value) {
        try {
            return parseYMD(value);
        }
        catch (ParseException ex) {
            return null;
        }
    }

    /**
     * Returns the date of today.
     *
     * @return the date of today
     */
    public static Date today() {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Date(calendar);
    }

    /**
     * Returns the date of tomorrow.
     *
     * @return the date of tomorrow
     */
    public static Date tomorrow() {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.DATE, 1);
        return new Date(calendar);
    }

    private Date(final Calendar calendar) {
        this.calendar = calendar;
        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);
        this.calendar.set(Calendar.MILLISECOND, 0);
    }

    public Date addDays(int amount) {
        return add(Calendar.DATE, amount);
    }

    public Date addMonths(int amount) {
        return add(Calendar.MONTH, amount);
    }

    public Date addYears(int amount) {
        return add(Calendar.YEAR, amount);
    }

    public boolean after(Date other) {
        return calendar.after(other.calendar);
    }

    public boolean before(Date other) {
        return calendar.before(other.calendar);
    }

    public boolean between(Date first, Date last) {
        return ((first == null) || !calendar.before(first.calendar)) &&
            ((last == null) || !calendar.after(last.calendar));
    }

    @Override
    public int compareTo(Date other) {
        return calendar.compareTo(other.calendar);
    }

    public int daysUntil(Date date) {
        if (date == null) {
            throw new NullPointerException("date");
        }

        long diffTime = date.calendar.getTimeInMillis() - calendar.getTimeInMillis();
        return (int) (diffTime / (24 * 60 * 60 * 1000));
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Date) {
            final Date other = (Date) object;
            return !(before(other) || after(other));
        }
        else {
            return super.equals(object);
        }
    }

    public Date firstDayOfMonth() {
        return create(1, getMonth(), getYear());
    }

    public String formatDMY() {
        return DMY_FORMAT.get().format(toJavaDate());
    }

    public String formatICalendar() {
        return ICALENDAR_FORMAT.get().format(toJavaDate());
    }

    public String formatISO8601() {
        return ISO8601_FORMAT.get().format(toJavaDate());
    }

    public String formatLong() {
        return LONG_FORMAT.get().format(toJavaDate());
    }

    public String formatShort() {
        return SHORT_FORMAT.get().format(toJavaDate());
    }

    public String formatText() {
        return TEXT_FORMAT.get().format(toJavaDate());
    }

    public int getDay() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public DayOfWeek getDayOfWeek() {
        return DAY_OF_WEEK_MAP.get(calendar.get(Calendar.DAY_OF_WEEK));
    }

    public int getDayOfWeekInt() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public String getDayOfWeekLong() {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
    }

    public String getDayOfWeekShort() {
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
    }

    /**
     * Returns the number of the month (1 to 12).
     *
     * @return the number of the month
     */
    public int getMonth() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * Returns the full name of the month in the current locale.
     *
     * @return the full name of the month
     */
    public String getMonthLong() {
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }

    public String getMonthShort() {
        return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }

    public String getMonthYearLong() {
        final StringBuilder result = new StringBuilder();
        result.append(getMonthLong());
        result.append(' ');
        result.append(getYear());
        return result.toString();
    }

    public int getWeek() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Returns the year.
     *
     * @return the year
     */
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public int hashCode() {
        return calendar.hashCode();
    }

    public java.util.Date toJavaDate() {
        return calendar.getTime();
    }

    public java.sql.Date toSqlDate() {
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    @Override
    public String toString() {
        return formatDMY();
    }

    private Date add(int field, int amount) {
        final Calendar result = new GregorianCalendar();
        result.setTimeInMillis(calendar.getTimeInMillis());
        result.add(field, amount);
        return new Date(result);
    }

    private static Map<Integer, DayOfWeek> createDayOfWeekMap() {
        final Map<Integer, DayOfWeek> result = new HashMap<>();
        result.put(Calendar.MONDAY, DayOfWeek.Monday);
        result.put(Calendar.TUESDAY, DayOfWeek.Tuesday);
        result.put(Calendar.WEDNESDAY, DayOfWeek.Wednesday);
        result.put(Calendar.THURSDAY, DayOfWeek.Thursday);
        result.put(Calendar.FRIDAY, DayOfWeek.Friday);
        result.put(Calendar.SATURDAY, DayOfWeek.Saturday);
        result.put(Calendar.SUNDAY, DayOfWeek.Sunday);
        return result;
    }

    private static class DateSpanImp implements DateSpan {

        private final Date startDate;
        private final Date endDate;

        private DateSpanImp(Date startDay, Date endDay) {
            this.startDate = startDay;
            this.endDate = endDay;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof DateSpan) {
                final DateSpan other = (DateSpan) object;
                return Util.equal(startDate, other.getStartDate()) && Util.equal(endDate, other.getEndDate());
            }
            else {
                return super.equals(object);
            }
        }

        @Override
        public boolean contains(Date day) {
            return day != null && day.between(startDate, endDate);
        }

        @Override
        public Date getEndDate() {
            return endDate;
        }

        @Override
        public Date getStartDate() {
            return startDate;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + Objects.hashCode(startDate);
            hash = 23 * hash + Objects.hashCode(endDate);
            return hash;
        }

        @Override
        public boolean overlaps(DateSpan other) {
            if (other == null) {
                return false;
            }

            final Date otherStart = other.getStartDate();
            final Date otherEnd = other.getEndDate();
            // an interval without start and end always overlaps
            if ((startDate == null && endDate == null) || (otherStart == null && otherEnd == null)) {
                return true;
            }

            // two intervals without start or end always overlap
            if ((startDate == null && otherStart == null) || (endDate == null && otherEnd == null)) {
                return true;
            }

            // here, both intervals have either a start or an end or both
            if (startDate == null || otherEnd == null) {
                return !endDate.before(otherStart);
            }

            if (endDate == null || otherStart == null) {
                return !startDate.after(otherEnd);
            }

            return !endDate.before(otherStart) && !startDate.after(otherEnd);
        }

        @Override
        public String toString() {
            final StringBuilder result = new StringBuilder();
            result.append(getStartDateText());
            result.append(" – ");
            result.append(getEndDateText());
            return result.toString();
        }

        private String getEndDateText() {
            if (endDate == null) {
                return null;
            }

            return endDate.formatDMY();
        }

        private String getStartDateText() {
            if (startDate == null) {
                return null;
            }

            return startDate.formatDMY();
        }
    }
}
