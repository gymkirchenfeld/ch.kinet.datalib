/*
 * Copyright (C) 2020 - 2023 by Stefan Rothe, Sebastian Forster
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

import java.util.Objects;

public final class DateSpan implements DateSpanI {

    private Date startDate;
    private Date endDate;

    public static DateSpan create() {
        return new DateSpan(null, null);
    }

    public static DateSpan create(Date date) {
        return new DateSpan(date, date);
    }

    public static DateSpan create(Date startDate, Date endDate) {
        return new DateSpan(startDate, endDate);
    }

    private DateSpan(Date startDay, Date endDay) {
        this.startDate = startDay;
        this.endDate = endDay;
    }

    public boolean after(Date date) {
        return startDate != null && date != null && startDate.after(date);
    }

    public boolean before(Date date) {
        return endDate != null && date != null && endDate.before(date);
    }

    @Override
    public boolean contains(Date day) {
        return day != null && day.between(startDate, endDate);
    }

    @Override
    public String durationText() {
        StringBuilder result = new StringBuilder();
        result.append(getStartDateText());
        result.append(" – ");
        result.append(getEndDateText());
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
    public boolean isOpen() {
        return startDate == null || endDate == null;
    }

    @Override
    public boolean isValid() {
        return isOpen() || !startDate.after(endDate);
    }

    @Override
    public boolean overlapsWith(DateSpanI other) {
        if (other == null) {
            return false;
        }

        return !(after(other.getEndDate()) || before(other.getStartDate()));
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
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
