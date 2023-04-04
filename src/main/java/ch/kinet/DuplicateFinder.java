/*
 * Copyright (C) 2021 by Stefan Rothe
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateFinder<T> {

    public enum CompareMode {
        IgnoreCase, CaseSensitive
    };
    private final CompareMode compareMode;
    private final HashMap<String, Set<T>> map;

    public static final <T> DuplicateFinder<T> create(CompareMode compareMode) {
        return new DuplicateFinder<>(compareMode);
    }

    private DuplicateFinder(CompareMode compareMode) {
        this.compareMode = compareMode;
        map = new HashMap<>();
    }

    public final void add(T object, String value) {
        if (compareMode == CompareMode.IgnoreCase) {
            value = Util.toLower(value);
        }

        if (!map.containsKey(value)) {
            map.put(value, new HashSet<>());
        }
    }

    public final Set<String> getDuplicates() {
        return map.keySet();
    }

    public final List<T> get(String value) {
        if (!map.containsKey(value)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(map.get(value));
    }
}
