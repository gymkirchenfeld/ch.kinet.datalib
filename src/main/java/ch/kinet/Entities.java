/*
 * Copyright (C) 2022 - 2023 by Sebastian Forster, Stefan Rothe
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Entities<T extends Entity> implements Iterable<T> {

    private final Map<Integer, T> byId = new HashMap<>();
    private final List<T> list = new ArrayList<>();

    public static <T extends Entity> Entities<T> create() {
        return new Entities<>();
    }

    public void add(T item) {
        doAdd(item);
        Collections.sort(list);
    }

    public void addAll(Stream<T> stream) {
        stream.sorted().forEachOrdered(item -> doAdd(item));
    }

    public T byId(int id) {
        return byId.get(id);
    }

    public T first() {
        if (isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    public T get(int index) {
        return list.get(index);
    }

    public int indexOf(T item) {
        return list.indexOf(item);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    public T last() {
        int size = list.size();
        return size == 0 ? null : list.get(size - 1);
    }

    public void remove(T item) {
        list.remove(item);
        byId.remove(item.getId());
    }

    public int size() {
        return list.size();
    }

    public Stream<T> stream() {
        return list.stream();
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    private void doAdd(T item) {
        int id = item.getId();

        if (byId.containsKey(id)) {
            throw new IllegalArgumentException("Trying to add entity with duplicate id " + id + ".");
        }

        list.add(item);
        byId.put(item.getId(), item);
    }
}
