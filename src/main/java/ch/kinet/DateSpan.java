/*
 * Copyright (C) 2024 by Stefan Rothe, Sebastian Forster
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
import java.util.Objects;

public final class DateSpan implements DateInterval {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public static DateSpan create() {
        return new DateSpan(null, null);
    }

    public static DateSpan of(LocalDate date) {
        return new DateSpan(date, date);
    }

    public static DateSpan of(LocalDate startDate, LocalDate endDate) {
        return new DateSpan(startDate, endDate);
    }

    public static DateSpan workWeek(LocalDate date) {
        LocalDate monday = date;
        while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
            monday = monday.minusDays(1);
        }

        return of(monday, monday.plusDays(5));
    }

    private DateSpan(LocalDate startDay, LocalDate endDay) {
        this.startDate = startDay;
        this.endDate = endDay;
    }

    public boolean contains(LocalDate date) {
        return date != null &&
            (startDate == null || !date.isBefore(startDate)) &&
            (endDate == null || !date.isAfter(endDate));
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
            return Util.equal(startDate, other.startDate) && Util.equal(endDate, other.endDate);
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

    @Override
    public boolean isValid() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    public boolean overlapsWith(DateInterval other) {
        if (other == null || !isValid()) {
            return false;
        }

        return !(startDate.isAfter(other.getEndDate()) || endDate.isBefore(other.getStartDate()));
    }

    public DateSpan withEndDate(LocalDate endDate) {
        return of(startDate, endDate);
    }

    public DateSpan withStartDate(LocalDate startDate) {
        return of(startDate, endDate);
    }
}
