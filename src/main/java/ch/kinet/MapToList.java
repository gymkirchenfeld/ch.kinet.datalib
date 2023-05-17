/*
 * Copyright (C) 2014 - 2016 by Stefan Rothe
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapToList<K, V> {

    private final Comparator<V> comparator;
    private final Map<K, List<V>> map;

    public static <K, V> MapToList<K, V> create() {
        return new MapToList<>(null);
    }

    public static <K, V> MapToList<K, V> create(final Comparator<V> comparator) {
        return new MapToList<>(comparator);
    }

    private MapToList(final Comparator<V> comparator) {
        this.comparator = comparator;
        map = new HashMap<>();
    }

    public void add(final K key, final V value) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<V>());
        }

        final List<V> list = map.get(key);
        if (!list.contains(value)) {
            list.add(value);
        }

        if (comparator != null) {
            Collections.sort(list, comparator);
        }
    }

    public void clear() {
        this.map.clear();
    }

    @SuppressWarnings("unchecked")
    public List<V> get(final K key) {
        if (map.containsKey(key)) {
            return Collections.unmodifiableList(map.get(key));
        }
        else {
            // Unchecked conversion:
            return Collections.EMPTY_LIST;
        }
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public void remove(final K key, final V value) {
        if (map.containsKey(key)) {
            final List<V> list = map.get(key);
            list.remove(value);
        }
    }
}
