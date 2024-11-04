/*
 * Copyright (C) 2012 - 2024 by Stefan Rothe
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class Log {

    public enum Level {
        debug, info, success, warning, error
    }

    private static final String JSON_ID = "id";
    private static final String JSON_DATE = "date";
    private static final String JSON_TIME = "time";
    private static final String JSON_LEVEL = "level";
    private static final String JSON_MESSAGE = "message";

    private final Object lock;
    private final List<Entry> entries;
    private boolean debug;
    private boolean hasError;
    private boolean hasInfo;
    private int nextEntryId;

    public static Log create() {
        return new Log();
    }

    private Log() {
        debug = true;
        entries = new ArrayList<>();
        lock = new Object();
    }

    public void clear() {
        synchronized (lock) {
            entries.clear();
            hasError = false;
            hasInfo = false;
        }
    }

    public void debug(String message, Object... args) {
        if (debug) {
            addEntry(Level.debug, Util.args(message, args));
        }
    }

    public void error(String message, Object... args) {
        addEntry(Level.error, Util.args(message, args));
    }

    public void exception(Throwable throwable) {
        addEntry(Level.error, throwable.toString());
        stackTrace(throwable);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            addEntry(Level.error, "caused by " + cause.toString());
            stackTrace(cause);
            cause = cause.getCause();
        }
    }

    private void stackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < stackTrace.length; ++i) {
            addEntry(Level.error, stackTrace[i].toString());
        }
    }

    public boolean hasError() {
        synchronized (lock) {
            return hasError;
        }
    }

    public boolean hasInfo() {
        synchronized (lock) {
            return hasInfo;
        }
    }

    public void info(String message, Object... args) {
        addEntry(Level.info, Util.args(message, args));
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return entries.isEmpty();
        }
    }

    public Mail createMail(String subject, String to) {
        Mail result = Mail.create();
        result.addTo(to);
        result.setSubject(subject);
        for (Entry entry : toList()) {
            String line = entry.toString();
            result.addLine(line);
        }

        return result;
    }

    public void setDebug(boolean debug) {
        synchronized (lock) {
            this.debug = debug;
        }
    }

    public Stream<Entry> streamEntries() {
        synchronized (lock) {
            return entries.stream();
        }
    }

    public void success(String message, Object... args) {
        addEntry(Level.success, Util.args(message, args));
    }

    public void warning(String message, Object... args) {
        addEntry(Level.warning, Util.args(message, args));
    }

    public List<Log.Entry> toList() {
        synchronized (lock) {
            return new ArrayList<>(entries);
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Entry entry : entries) {
            if (result.length() > 0) {
                result.append("\n");
            }

            result.append(entry.toString());
        }

        return result.toString();
    }

    private void addEntry(Level level, String message) {
        synchronized (lock) {
            ++nextEntryId;
            Entry entry = new Entry(nextEntryId, level, message);
            entries.add(entry);
            switch (level) {
                case info:
                    hasInfo = true;
                    break;
                case warning:
                    hasInfo = true;
                    break;
                case error:
                    hasInfo = true;
                    hasError = true;
                    break;
            }
        }
    }

    public static final class Entry implements Json {

        private final int id;
        private final Level level;
        private final String message;
        private final LocalDateTime timestamp;

        Entry(int id, Level level, String message) {
            this.id = id;
            this.level = level;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Entry) {
                Entry other = (Entry) object;
                return level == other.level && Util.equal(message, other.message);
            }
            else {
                return super.equals(object);
            }
        }

        public String getLevelText() {
            switch (level) {
                case debug:
                    return "Debug";
                case error:
                    return "Fehler";
                case info:
                    return "Info";
                case success:
                    return "Erfolg";
                case warning:
                    return "Warnung";
                default:
                    return null;
            }
        }

        public String getMessage() {
            return message;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + Objects.hashCode(level);
            hash = 67 * hash + Objects.hashCode(message);
            return hash;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(Date.formatTimestamp(timestamp));
            result.append(": ");
            result.append(getLevelText());
            result.append(": ");
            result.append(message);
            return result.toString();
        }

        @Override
        public JsonObject toJsonTerse() {
            JsonObject result = JsonObject.create();
            result.put(JSON_ID, id);
            result.put(JSON_DATE, timestamp.toLocalDate());
            result.put(JSON_TIME, timestamp.toLocalTime());
            result.put(JSON_LEVEL, getLevelText());
            result.put(JSON_MESSAGE, message);
            return result;
        }

        @Override
        public JsonObject toJsonVerbose() {
            return toJsonTerse();
        }
    }
}
