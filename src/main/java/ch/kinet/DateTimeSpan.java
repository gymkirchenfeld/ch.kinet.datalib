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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DateTimeSpan implements Comparable<DateTimeSpan>, DateTimeInterval {

    public static DateTimeSpan of(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        return new DateTimeSpan(startDate, startTime, endDate, endTime);
    }

    public static DateTimeSpan of(LocalDate startDate, LocalDate endDate) {
        return new DateTimeSpan(startDate, null, endDate, null);
    }

    public static DateTimeSpan of(DateInterval interval) {
        return of(interval.getStartDate(), interval.getEndDate());
    }

    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;

    private DateTimeSpan(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
    }

    @Override
    public int compareTo(DateTimeSpan other) {
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

    public boolean contains(LocalDate date) {
        return date != null && Date.isBetween(date, startDate, endDate);
    }

    public boolean contains(LocalDateTime instant) {
        return instant != null && Date.isBetween(instant, startDateTime(), endDateTime());
    }

    public String durationText() {
        StringBuilder result = new StringBuilder();
        result.append(Date.formatDMY(startDate));
        result.append(" – ");
        result.append(Date.formatDMY(endDate));
        return result.toString();
    }

    public LocalDateTime endDateTime() {
        return makeEndDateTime(endDate, endTime);
    }

    public boolean isCurrent() {
        return contains(LocalDateTime.now());
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

        if (endDate.isBefore(startDate)) {
            return false;
        }

        if (startDate.equals(endDate) && startTime != null && endTime != null && endTime.isBefore(startTime)) {
            return false;
        }

        return true;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    public boolean isAllDay(LocalDate date) {
        return takesPlaceOn(date) &&
            (startTime == null || startDate.isBefore(date)) &&
            (endTime == null || endDate.isAfter(date));
    }

    public boolean overlapsWith(DateInterval other) {
        if (other == null) {
            return false;
        }

        LocalDate otherStart = other.getStartDate();
        LocalDate otherEnd = other.getEndDate();
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
            return !endDate.isBefore(otherStart);
        }

        if (endDate == null || otherStart == null) {
            return !startDate.isAfter(otherEnd);
        }

        return !endDate.isBefore(otherStart) && !startDate.isAfter(otherEnd);
    }

    /**
     * Checks if this time span overlaps the other time span.
     *
     * @param other the other time span
     * @return <code>true</code> if this time span overlaps the other time span, otherwise <code>false</code>
     */
    public boolean overlapsWith(DateTimeInterval other) {
        LocalDateTime otherStart = makeStartDateTime(other.getStartDate(), other.getStartTime());
        LocalDateTime otherEnd = makeEndDateTime(other.getEndDate(), other.getEndTime());
        return !(!startDateTime().isBefore(otherEnd) || !endDateTime().isAfter(otherStart));
    }

    public LocalDateTime startDateTime() {
        return makeStartDateTime(startDate, startTime);
    }

    public boolean takesPlaceOn(LocalDate date) {
        return Date.isBetween(date, startDate, endDate);
    }

    private static LocalDateTime makeEndDateTime(LocalDate endDate, LocalTime endTime) {
        return LocalDateTime.of(
            endDate == null ? LocalDate.MAX : endDate,
            endTime == null ? LocalTime.MAX : endTime
        );
    }

    private static LocalDateTime makeStartDateTime(LocalDate startDate, LocalTime startTime) {
        return LocalDateTime.of(
            startDate == null ? LocalDate.MIN : startDate,
            startTime == null ? LocalTime.MIN : startTime
        );
    }
}
