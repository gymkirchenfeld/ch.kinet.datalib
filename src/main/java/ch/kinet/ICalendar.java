/*
 * Copyright (C) 2021 - 2023 by Sebastian Forster, Stefan Rothe
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ICalendar {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private final StringBuilder data;

    public static final ICalendar create(String prodid) {
        return new ICalendar(prodid);
    }

    private ICalendar(String prodid) {
        data = new StringBuilder();
        data.append("BEGIN:VCALENDAR\n");
        data.append("VERSION:2.0\n");
        data.append("PRODID:");
        data.append(prodid);
        data.append("\n");
        data.append("METHOD:PUBLISH\n");
    }

    public void addEvent(String uid, String title, DateInterval duration) {
        addEvent(uid, title, duration.getStartDate(), null, duration.getEndDate(), null);
    }

    public void addEvent(String uid, String title, LocalDate startDay, LocalTime startTime, LocalDate endDay, LocalTime endTime) {
        data.append("BEGIN:VEVENT\n");
        data.append("UID:");
        data.append(uid);
        data.append("\n");
        data.append("SUMMARY:");
        data.append(title);
        data.append("\n");
        data.append("CLASS:PUBLIC\n");
        if (startTime != null) {
            data.append("DTSTART:");
            data.append(LocalDateTime.of(startDay, startTime).format(TIMESTAMP_FORMAT));
        }
        else {
            data.append("DTSTART;VALUE=DATE:");
            data.append(startDay.format(DATE_FORMAT));
        }

        data.append("\n");

        if (startTime != null) {
            if (endTime == null) {
                endTime = startTime;
            }
            data.append("DTEND:");
            data.append(LocalDateTime.of(endDay, endTime).format(TIMESTAMP_FORMAT));
        }
        else {
            data.append("DTEND;VALUE=DATE:");
            data.append(endDay.format(DATE_FORMAT));
        }

        data.append("\n");
        data.append("DTSTAMP:");
        data.append(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        data.append("\n");
        data.append("END:VEVENT\n");
    }

    @Override
    public String toString() {
        return data.toString() + "END:VCALENDAR\n";
    }
}
