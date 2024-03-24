/*
 * Copyright (C) 2023 by Stefan Rothe
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

public class DateSpanEntity extends Entity implements DateSpanI {

    public static final String DB_END_DATE = "EndDate";
    public static final String DB_START_DATE = "StartDate";
    public static final String JSON_END_DATE = "endDate";
    public static final String JSON_START_DATE = "startDate";
    private final DateSpan dateSpan = DateSpan.create();

    protected DateSpanEntity(int id) {
        super(id);
    }

    @Override
    public final boolean contains(Date date) {
        return dateSpan.contains(date);
    }

    @Override
    public String durationText() {
        return dateSpan.durationText();
    }

    @Override
    public final Date getEndDate() {
        return dateSpan.getEndDate();
    }

    @Override
    public final Date getStartDate() {
        return dateSpan.getStartDate();
    }

    @Override
    @Persistence(ignore = true)
    public boolean isOpen() {
        return dateSpan.isOpen();
    }

    @Override
    @Persistence(ignore = true)
    public boolean isValid() {
        return dateSpan.isValid();
    }

    @Override
    public final boolean overlapsWith(DateSpanI other) {
        return dateSpan.overlapsWith(other);
    }

    public final void setEndDate(Date endDate) {
        dateSpan.setEndDate(endDate);
    }

    public final void setStartDate(Date startDate) {
        dateSpan.setStartDate(startDate);
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = super.toJsonTerse();
        result.put(JSON_START_DATE, getStartDate());
        result.put(JSON_END_DATE, getEndDate());
        return result;
    }
}
