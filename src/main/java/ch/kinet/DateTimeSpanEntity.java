/*
 * Copyright (C) 2023 - 2024 by Stefan Rothe
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

import ch.kinet.reflect.Persistence;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateTimeSpanEntity extends Entity implements DateTimeInterval {

    public static final String DB_END_DATE = "EndDate";
    public static final String DB_END_TIME = "EndTime";
    public static final String DB_START_DATE = "StartDate";
    public static final String DB_START_TIME = "StartTime";
    public static final String JSON_END_DATE = "endDate";
    public static final String JSON_END_TIME = "endTime";
    public static final String JSON_START_DATE = "startDate";
    public static final String JSON_START_TIME = "startTime";
    private DateTimeSpan impl = DateTimeSpan.of(null, null);

    protected DateTimeSpanEntity(int id) {
        super(id);
    }

    @Override
    public int compareTo(Entity entity) {
        if (entity instanceof DateTimeSpanEntity) {
            DateTimeSpanEntity other = (DateTimeSpanEntity) entity;
            return Util.compare(impl, other.impl);
        }
        else {
            return super.compareTo(entity);
        }
    }

    public boolean contains(LocalDate date) {
        return impl.contains(date);
    }

    public String durationText() {
        return impl.durationText();
    }

    public LocalDateTime endDateTime() {
        return impl.endDateTime();
    }

    @Override
    public final LocalDate getEndDate() {
        return impl.getEndDate();
    }

    @Override
    public final LocalTime getEndTime() {
        return impl.getEndTime();
    }

    @Override
    public final LocalDate getStartDate() {
        return impl.getStartDate();
    }

    @Override
    public final LocalTime getStartTime() {
        return impl.getStartTime();
    }

    @Persistence(ignore = true)
    public boolean isValid() {
        return impl.isValid();
    }

    public final boolean overlapsWith(DateTimeInterval other) {
        return impl.overlapsWith(other);
    }

    public final void setEndDate(LocalDate endDate) {
        impl = DateTimeSpan.of(getStartDate(), getStartTime(), endDate, getEndTime());
    }

    public final void setEndTime(LocalTime endTime) {
        impl = DateTimeSpan.of(getStartDate(), getStartTime(), getEndDate(), endTime);
    }

    public final void setStartDate(LocalDate startDate) {
        impl = DateTimeSpan.of(startDate, getStartTime(), getEndDate(), getEndTime());
    }

    public final void setStartTime(LocalTime startTime) {
        impl = DateTimeSpan.of(getStartDate(), startTime, getEndDate(), getEndTime());
    }

    public LocalDateTime startDateTime() {
        return impl.startDateTime();
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = super.toJsonTerse();
        result.put(JSON_START_DATE, getStartDate());
        result.put(JSON_START_TIME, getStartTime());
        result.put(JSON_END_DATE, getEndDate());
        result.put(JSON_END_TIME, getEndTime());
        return result;
    }

    public DateTimeSpan toLocalDateTimeSpan() {
        return impl;
    }
}
