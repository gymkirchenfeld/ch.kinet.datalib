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
import java.util.Map;

public final class ICalendar {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private final StringBuilder data;

    public static final ICalendar create(String prodid) {
        return new ICalendar(prodid);
    }

    private ICalendar(String prodid) {
        data = new StringBuilder();
        data.append("BEGIN:VCALENDAR\r\n");
        data.append("VERSION:2.0\r\n");
        data.append("PRODID:");
        data.append(prodid);
        data.append("\r\n");
        data.append("METHOD:PUBLISH\r\n");
    }

    public void addEvent(String uid, String title, String description, DateInterval duration, Map<String, String> hashMap) {
        addEvent(uid, title, description, null, duration.getStartDate(), null, duration.getEndDate(), null, hashMap);
    }

    public void addEvent(String uid, String title, String description,
            LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, Map<String, String> hashMap ) {      
        addEvent(uid, title, description, null, startDate, startTime, endDate, endTime, hashMap);                  
    }

    public void addEvent(String uid, String title, String description, String location,
            LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime, Map<String, String> hashMap ) {
        data.append("BEGIN:VEVENT\r\n");
        data.append("UID:");
        data.append(uid);
        data.append("\r\n");
        data.append("SUMMARY:");
        data.append(title);
        data.append("\r\n");
        if(!Util.isEmpty(description)) {
            data.append("DESCRIPTION:");
            data.append(description);
            data.append("\r\n");
        }
        if(!Util.isEmpty(location)) {
            data.append("LOCATION:");
            data.append(location);
            data.append("\r\n");
        }

        if (hashMap != null) {
            for (Map.Entry<String, String> entry : hashMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                data.append(key);
                data.append(":");
                data.append(value);
                data.append("\r\n");
            }
        }

        data.append("CLASS:PUBLIC\r\n");
        if (startTime != null) {
            data.append("DTSTART:");
            data.append(LocalDateTime.of(startDate, startTime).format(TIMESTAMP_FORMAT));
        }
        else {
            data.append("DTSTART;VALUE=DATE:");
            data.append(startDate.format(DATE_FORMAT));
        }

        data.append("\r\n");


        if (startTime != null) {
            if (endTime == null) {
                endTime = startTime;
            }
            data.append("DTEND:");
            data.append(LocalDateTime.of(endDate, endTime).format(TIMESTAMP_FORMAT));
        }
        else {
            data.append("DTEND;VALUE=DATE:");
            data.append(endDate.plusDays(1).format(DATE_FORMAT));
        }

        data.append("\r\n");
        data.append("DTSTAMP:");
        data.append(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        data.append("\r\n");
        data.append("END:VEVENT\r\n");
    }

    @Override
    public String toString() {
        return data.toString() + "END:VCALENDAR\r\n";
    }
}
