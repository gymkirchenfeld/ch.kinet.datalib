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

public interface TimeSpanI {

    /**
     * Returns the end date.
     *
     * @return the end date
     */
    Date getEndDate();

    /**
     * Returns the end time.
     *
     * @return the end time
     */
    Time getEndTime();

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    Date getStartDate();

    /**
     * Returns the start time.
     *
     * @return the start time
     */
    Time getStartTime();
}
