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

/**
 * Represents a duration with start and end date. A date span may be open, i.e. it may have no start or no end date.
 */
public interface DateSpanI {

    /**
     * Checks if this date span contains the specified date.
     *
     * @param date the date to check
     * @return <tt>true</tt> if this date span contains the specified date, otherwise <tt>false</tt>
     */
    boolean contains(Date date);

    /**
     * Returns a formated text representing this date span.
     *
     * @return a formated text representing this date span
     */
    String durationText();

    /**
     * Returns the end date.
     *
     * @return the end date
     */
    Date getEndDate();

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    Date getStartDate();

    /**
     * Checks if this date span is open.
     *
     * @return <tt>true</tt> if this date span is open, otherwise <tt>false</tt>
     */
    boolean isOpen();

    /**
     * Checks if this date span is valid. A date span is valid if it's either open or if the end date is not before the
     * start date.
     *
     * @return
     */
    boolean isValid();

    /**
     * Checks if this and the other date span overlap.
     *
     * @param other the other date span
     * @return <tt>true</tt> if both date span overlap, otherwise <tt>false</tt>
     */
    boolean overlapsWith(DateSpanI other);
}
