/*
 * Copyright (C) 2024 by Stefan Rothe
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

public class DateSpanEntity extends Entity implements DateInterval {

    public static final String DB_END_DATE = "EndDate";
    public static final String DB_START_DATE = "StartDate";
    public static final String JSON_CURRENT = "current";
    public static final String JSON_END_DATE = "endDate";
    public static final String JSON_START_DATE = "startDate";
    private DateSpan dateSpan = DateSpan.create();

    protected DateSpanEntity(int id) {
        super(id);
    }

    public final boolean contains(LocalDate date) {
        return dateSpan.contains(date);
    }

    public String durationText() {
        return dateSpan.durationText();
    }

    @Override
    public final LocalDate getEndDate() {
        return dateSpan.getEndDate();
    }

    @Override
    public final LocalDate getStartDate() {
        return dateSpan.getStartDate();
    }

    @Persistence(ignore = true)
    public final boolean isCurrent() {
        return dateSpan.isCurrent();
    }

    public final boolean overlapsWith(DateInterval other) {
        return dateSpan.overlapsWith(other);
    }

    public final void setEndDate(LocalDate endDate) {
        dateSpan = dateSpan.withEndDate(endDate);
    }

    public final void setStartDate(LocalDate startDate) {
        dateSpan = dateSpan.withStartDate(startDate);
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = super.toJsonTerse();
        result.put(JSON_START_DATE, getStartDate());
        result.put(JSON_CURRENT, isCurrent());
        result.put(JSON_END_DATE, getEndDate());
        return result;
    }

    @Override
    protected int doCompare(Entity entity) {
        int result = 0;
        if (entity instanceof DateSpanEntity) {
            DateSpanEntity other = (DateSpanEntity) entity;
            result = dateSpan.compareTo(other.dateSpan);
        }

        if (result == 0) {
            result = super.doCompare(entity);
        }

        return result;
    }
}
