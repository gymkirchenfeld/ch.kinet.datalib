/*
 * Copyright (C) 2018 - 2024 by Sebastian Forster, Stefan Rothe
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SetComparison<T> {

    private final Set<T> oldItems;
    private final Set<T> newItems;

    public static <T> SetComparison<T> create(Stream<T> oldItems, Stream<T> newItems) {
        return new SetComparison<>(oldItems.collect(Collectors.toSet()), newItems.collect(Collectors.toSet()));
    }

    public static <T> SetComparison<T> create(Collection<T> oldItems, Collection<T> newItems) {
        return new SetComparison<>(new HashSet<>(oldItems), new HashSet<>(newItems));
    }

    private SetComparison(Set<T> oldItems, Set<T> newItems) {
        this.oldItems = oldItems;
        this.newItems = newItems;
    }

    public Set<T> getAdded() {
        Set<T> result = new HashSet<>();
        for (T newItem : newItems) {
            if (!oldItems.contains(newItem)) {
                result.add(newItem);
            }
        }

        return result;
    }

    public Set<T> getRemoved() {
        Set<T> result = new HashSet<>();
        for (T oldItem : oldItems) {
            if (!newItems.contains(oldItem)) {
                result.add(oldItem);
            }
        }

        return result;
    }

    public boolean hasChanges() {
        return !(getAdded().isEmpty() && getRemoved().isEmpty());
    }

    public long newCount() {
        return newItems.size();
    }

    public long oldCount() {
        return oldItems.size();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("old={");
        result.append(Util.concat(oldItems, ";"));
        result.append("} new={");
        result.append(Util.concat(newItems, ";"));
        result.append("}");
        return result.toString();
    }
}
