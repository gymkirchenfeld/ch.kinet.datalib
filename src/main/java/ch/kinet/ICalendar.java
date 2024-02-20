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

public final class ICalendar {

    public static final ICalendar create(String prodid) {
        return new ICalendar(prodid);
    }

    private StringBuilder data;

    private ICalendar(String prodid) {
        data = new StringBuilder();
        data.append("BEGIN:VCALENDAR\n");
        data.append("VERSION:2.0\n");
        data.append("PRODID:");
        data.append(prodid);
        data.append("\n");
        data.append("METHOD:PUBLISH\n");
    }

    public void addEvent(String uid, String title, DateSpanI duration) {
        addEvent(uid, title, duration.getStartDate(), null, duration.getEndDate(), null);
    }

    public void addEvent(String uid, String title, Date startDay, Date endDay) {
        addEvent(uid, title, startDay, null, endDay, null);
    }

    public void addEvent(String uid, String title, Date startDay, Time startTime, Date endDay, Time endTime) {
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
            data.append(startDay.formatICalendar());
            data.append("T");
            data.append(startTime.formatICalendar());
        }
        else {
            data.append("DTSTART;VALUE=DATE:");
            data.append(startDay.formatICalendar());
        }

        data.append("\n");

        if (startTime != null) {
            if (endTime == null) {
                endTime = startTime;
            }
            data.append("DTEND:");
            data.append(endDay.formatICalendar());
            data.append("T");
            data.append(endTime.formatICalendar());
        }
        else {
            data.append("DTEND;VALUE=DATE:");
            data.append(endDay.formatICalendar());
        }

        data.append("\n");
        data.append("DTSTAMP:");
        data.append(Timestamp.now().formatICalendar());
        data.append("\n");
        data.append("END:VEVENT\n");
    }

    @Override
    public String toString() {
        return data.toString() + "END:VCALENDAR\n";
    }
}
