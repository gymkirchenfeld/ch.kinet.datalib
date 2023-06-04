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

public interface DateSpan {

    public static boolean isOpen(DateSpan dateSpan) {
        return dateSpan.getEndDate() == null || dateSpan.getStartDate() == null;
    }

    boolean contains(Date date);

    Date getEndDate();

    Date getStartDate();

    boolean overlaps(DateSpan other);
}
