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
    public static final String JSON_END_DATE = "endDate";
    public static final String JSON_START_DATE = "startDate";
    private DateSpan dateSpan = DateSpan.create();

    protected DateSpanEntity(int id) {
        super(id);
    }

    public final boolean contains(LocalDate date) {
        return toDateSpan().contains(date);
    }

    public String durationText() {
        return toDateSpan().durationText();
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

    @Override
    @Persistence(ignore = true)
    public final boolean isValid() {
        return dateSpan.isValid();
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

    public final DateSpan toDateSpan() {
        return dateSpan;
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = super.toJsonTerse();
        result.put(JSON_START_DATE, getStartDate());
        result.put(JSON_END_DATE, getEndDate());
        return result;
    }
}
