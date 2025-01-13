/*
 * Copyright (C) 2024 - 2025 by Stefan Rothe, Sebastian Forster
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

public final class DateSpan implements Comparable<DateSpan>, DateInterval {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public static DateSpan create() {
        return new DateSpan(null, null);
    }

    public static DateSpan endingAt(LocalDate endDate) {
        return new DateSpan(null, endDate);
    }

    public static DateSpan of(LocalDate date) {
        return new DateSpan(date, date);
    }

    public static DateSpan of(LocalDate startDate, LocalDate endDate) {
        return new DateSpan(startDate, endDate);
    }

    public static DateSpan of(DateInterval interval) {
        return new DateSpan(interval.getStartDate(), interval.getEndDate());
    }

    public static DateSpan of(DateTimeInterval interval) {
        return new DateSpan(interval.getStartDate(), interval.getEndDate());
    }

    public static DateSpan startingAt(LocalDate startDate) {
        return new DateSpan(startDate, null);
    }

    public static DateSpan workWeek(LocalDate date) {
        LocalDate monday = date;
        while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            monday = monday.minusDays(1);
        }

        return of(monday, monday.plusDays(5));
    }

    private DateSpan(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public int compareTo(DateSpan other) {
        int result = Util.compare(startDate, other.startDate);
        if (result == 0) {
            result = Util.compare(endDate, other.endDate);
        }

        return result;
    }

    public boolean contains(LocalDate date) {
        return date != null &&
            (startDate == null || !date.isBefore(startDate)) &&
            (endDate == null || !date.isAfter(endDate));
    }

    public boolean contains(DateInterval interval) {
        return interval != null &&
            (startDate == null || !startDate.isAfter(interval.getStartDate())) &&
            (endDate == null || !endDate.isBefore(interval.getEndDate()));
    }

    public long countDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public String durationText() {
        StringBuilder result = new StringBuilder();
        result.append(Date.formatDMY(startDate));
        result.append(" – ");
        result.append(Date.formatDMY(endDate));
        return result.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DateSpan) {
            DateSpan other = (DateSpan) object;
            return startDate.equals(other.startDate) && endDate.equals(other.endDate);
        }
        else {
            return super.equals(object);
        }
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(startDate);
        hash = 23 * hash + Objects.hashCode(endDate);
        return hash;
    }

    public boolean isCurrent() {
        return contains(LocalDate.now());
    }

    public boolean isValid() {
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }

    public boolean overlapsWith(DateInterval interval) {
        if (interval == null || !isValid()) {
            return false;
        }

        LocalDate thisStart = ensureStartDate(startDate);
        LocalDate thisEnd = ensureEndDate(endDate);
        LocalDate otherStart = ensureStartDate(interval.getStartDate());
        LocalDate otherEnd = ensureEndDate(interval.getEndDate());
        return !(thisStart.isAfter(otherEnd) || thisEnd.isBefore(otherStart));
    }

    public Stream<LocalDate> streamDates() {
        return startDate.datesUntil(endDate.plusDays(1));
    }

    public DateSpan withEndDate(LocalDate endDate) {
        return of(startDate, endDate);
    }

    public DateSpan withStartDate(LocalDate startDate) {
        return of(startDate, endDate);
    }

    private static LocalDate ensureStartDate(LocalDate startDate) {
        return startDate == null ? LocalDate.MIN : startDate;
    }

    private static LocalDate ensureEndDate(LocalDate endDate) {
        return endDate == null ? LocalDate.MAX : endDate;
    }
}
