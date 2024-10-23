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

    private static final int NONE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int SUCCESS = 3;
    private static final int WARNING = 4;
    private static final int ERROR = 5;

    private final Object lock;
    private final List<Entry> entries;
    private final List<Entry> oldEntries;
    private boolean debug;
    private int maxLevel;
    private int nextEntryId;

    public static Log create() {
        return new Log();
    }

    private Log() {
        debug = true;
        entries = new ArrayList<>();
        lock = new Object();
        maxLevel = NONE;
        oldEntries = new ArrayList<>();
    }

    public void clear() {
        synchronized (lock) {
            maxLevel = NONE;
            entries.clear();
        }
    }

    public void debug(String message, Object... args) {
        if (debug) {
            addEntry(DEBUG, Util.args(message, args));
        }
    }

    public void error(String message, Object... args) {
        addEntry(ERROR, Util.args(message, args));
    }

    public void exception(Throwable throwable) {
        addEntry(ERROR, throwable.toString());
        stackTrace(throwable);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            addEntry(ERROR, "caused by " + cause.toString());
            stackTrace(cause);
            cause = cause.getCause();
        }
    }

    private void stackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < stackTrace.length; ++i) {
            addEntry(ERROR, stackTrace[i].toString());
        }
    }

    public boolean hasChanges() {
        synchronized (lock) {
            return !entries.equals(oldEntries);
        }
    }

    public boolean hasError() {
        synchronized (lock) {
            return maxLevel >= ERROR;
        }
    }

    public boolean hasInfo() {
        synchronized (lock) {
            return maxLevel >= INFO;
        }
    }

    public void info(String message, Object... args) {
        addEntry(INFO, Util.args(message, args));
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

    public void startSession() {
        synchronized (lock) {
            oldEntries.clear();
            oldEntries.addAll(entries);
            entries.clear();
            maxLevel = NONE;
        }
    }

    public Stream<Entry> streamEntries() {
        synchronized (lock) {
            return entries.stream();
        }
    }

    public void success(String message, Object... args) {
        addEntry(SUCCESS, Util.args(message, args));
    }

    public void warning(String message, Object... args) {
        addEntry(WARNING, Util.args(message, args));
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

    private void addEntry(int level, String message) {
        synchronized (lock) {
            ++nextEntryId;
            Entry entry = new Entry(nextEntryId, level, message);
            entries.add(entry);
            if (level > maxLevel) {
                maxLevel = level;
            }
        }
    }

    public static final class Entry implements Json {

        private final int id;
        private final int level;
        private final String message;
        private final LocalDateTime timestamp;

        Entry(int id, int level, String message) {
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

        public String getLevelCss() {
            switch (level) {
                case DEBUG:
                    return null;
                case ERROR:
                    return "danger";
                case INFO:
                    return "info";
                case SUCCESS:
                    return "success";
                case WARNING:
                    return "warning";
                default:
                    return null;
            }
        }

        public String getLevelIcon() {
            switch (level) {
                case DEBUG:
                    return "fa-bug";
                case ERROR:
                    return "fa-ban";
                case INFO:
                    return "fa-info";
                case SUCCESS:
                    return "fa-check";
                case WARNING:
                    return "fa-exclamation";
                default:
                    return null;
            }
        }

        public String getLevelText() {
            switch (level) {
                case DEBUG:
                    return "Debug";
                case ERROR:
                    return "Fehler";
                case INFO:
                    return "Info";
                case SUCCESS:
                    return "Erfolg";
                case WARNING:
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
            result.put("id", id);
            result.put("date", timestamp.toLocalDate());
            result.put("time", timestamp.toLocalTime());
            result.put("level", getLevelText());
            result.put("message", message);
            return result;
        }

        @Override
        public JsonObject toJsonVerbose() {
            return toJsonTerse();
        }
    }
}
