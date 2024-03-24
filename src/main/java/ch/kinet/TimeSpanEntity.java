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

public class TimeSpanEntity extends DateSpanEntity implements TimeSpanI {

    public static final String DB_END_TIME = "EndTime";
    public static final String DB_START_TIME = "StartTime";
    public static final String JSON_END_TIME = "endTime";
    public static final String JSON_START_TIME = "startTime";
    private Time endTime;
    private Time startTime;

    protected TimeSpanEntity(int id) {
        super(id);
    }

    @Override
    public final Time getEndTime() {
        return endTime;
    }

    @Override
    public final Time getStartTime() {
        return startTime;
    }

    public final boolean overlapsWith(TimeSpanI other) {
        return timeSpan().overlapsWith(other);
    }

    public final void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public final void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public final TimeSpan timeSpan() {
        return TimeSpan.create(getStartDate(), startTime, getEndDate(), endTime);
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = super.toJsonTerse();
        result.put(JSON_START_TIME, startTime);
        result.put(JSON_END_TIME, endTime);
        return result;
    }
}
