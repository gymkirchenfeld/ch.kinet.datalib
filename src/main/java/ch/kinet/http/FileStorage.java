/*
 * Copyright (C) 2023 by Sebastian Forster, Stefan Rothe
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
package ch.kinet.http;

import ch.kinet.Data;
import ch.kinet.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FileStorage {

    private final Map<String, FileInfo> storage = new HashMap<>();
    private final Object lock = new Object();

    public static FileStorage create() {
        FileStorage result = new FileStorage();
        new Thread(new GarbageCollector(result)).start();
        return result;
    }

    private FileStorage() {
    }

    public Data getFile(String uuid) {
        synchronized (lock) {
            FileInfo fileInfo = storage.get(uuid);
            return fileInfo == null ? Data.empty() : fileInfo.file;
        }
    }

    public String addTemporaryFile(Data file) {
        synchronized (lock) {
            String key = UUID.randomUUID().toString();
            FileInfo fileInfo = new FileInfo(file, Timestamp.now().addMinutes(1));
            storage.put(key, fileInfo);
            return key;
        }
    }

    public void collectGarbage() {
        synchronized (lock) {
            Set<String> removable = new HashSet<>();
            Timestamp now = Timestamp.now();
            removable.clear();
            storage.keySet().forEach(key -> {
                if (now.after(storage.get(key).deleteAfter)) {
                    removable.add(key);
                }
            });

            removable.forEach(key -> {
                storage.remove(key);
            });
        }
    }

    private static final class FileInfo {

        private final Data file;
        private final Timestamp deleteAfter;

        FileInfo(Data file, Timestamp deleteAfter) {
            this.file = file;
            this.deleteAfter = deleteAfter;
        }
    }

    private static final class GarbageCollector implements Runnable {

        private final FileStorage storage;

        private GarbageCollector(FileStorage storage) {
            this.storage = storage;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(60 * 1000);
                }
                catch (InterruptedException ex) {
                    // ignore
                }

                storage.collectGarbage();
            }
        }
    }
}
