/*
 * Copyright (C) 2023 - 2024 by Stefan Rothe, Sebastian Forster
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

public final class TimeSpan implements Comparable<TimeSpan>, TimeSpanI {

    public static TimeSpan create(Date startDate, Time startTime, Date endDate, Time endTime) {
        return new TimeSpan(startDate, startTime, endDate, endTime);
    }

    public static TimeSpan create(Date startDate, Date endDate) {
        return new TimeSpan(startDate, null, endDate, null);
    }

    private final Date startDate;
    private final Time startTime;
    private final Date endDate;
    private final Time endTime;

    private TimeSpan(Date startDate, Time startTime, Date endDate, Time endTime) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
    }

    @Override
    public int compareTo(TimeSpan other) {
        int result = Util.compare(startDate, other.startDate);
        if (result == 0) {
            result = Util.compare(startTime, other.startTime);
        }

        if (result == 0) {
            result = Util.compare(endDate, other.endDate);
        }

        if (result == 0) {
            result = Util.compare(endTime, other.endTime);
        }

        return result;
    }

    public boolean contains(Date date) {
        return date != null && date.between(startDate, endDate);
    }

    public DateSpanI dateSpan() {
        return DateSpan.create(startDate, endDate);
    }

    public Timestamp endDateTime() {
        return makeEndDateTime(endDate, endTime);
    }

    /**
     * Checks if this time span is valid. A valid time span has a start date and an end date that doesn't lie before the
     * start date. If start and date date are equal, the end time may not lie before the start time.
     *
     * @return <tt>true</tt> if the time span is valid, otherwise <tt>false</tt>
     */
    public boolean isValid() {
        if (startDate == null || endDate == null) {
            return false;
        }

        if (endDate.before(startDate)) {
            return false;
        }

        if (startDate.equals(endDate) && startTime != null && endTime != null && endTime.before(startTime)) {
            return false;
        }

        return true;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public Time getEndTime() {
        return endTime;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Time getStartTime() {
        return startTime;
    }

    public boolean overlapsWith(DateSpanI other) {
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

    /**
     * Checks if this time span overlaps the other time span.
     *
     * @param other the other time span
     * @return <code>true</code> if this time span overlaps the other time span, otherwise <code>false</code>
     */
    public boolean overlapsWith(TimeSpanI other) {
        Timestamp otherStart = makeStartDateTime(other.getStartDate(), other.getStartTime());
        Timestamp otherEnd = makeEndDateTime(other.getEndDate(), other.getEndTime());
        return !(!startDateTime().before(otherEnd) || !endDateTime().after(otherStart));
    }

    public Timestamp startDateTime() {
        return makeStartDateTime(startDate, startTime);
    }

    private static Timestamp makeEndDateTime(Date endDate, Time endTime) {
        return Timestamp.create(
            endDate == null ? Date.today() : endDate,
            endTime == null ? Time.create(23, 59, 59) : endTime
        );
    }

    private static Timestamp makeStartDateTime(Date startDate, Time startTime) {
        return Timestamp.create(
            startDate,
            startTime == null ? Time.create(0, 0, 0) : startTime
        );
    }
}
