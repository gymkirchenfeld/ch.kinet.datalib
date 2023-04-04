/*
 * Copyright (C) 2018 - 2023 by Sebastian Forster, Stefan Rothe
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

public final class SetComparison<T> {

    private final Set<T> oldItems;
    private final Set<T> newItems;

    public static <T> SetComparison<T> create(Collection<T> oldItems, Collection<T> newItems) {
        SetComparison<T> result = new SetComparison<>();
        result.addOld(oldItems);
        result.addNew(newItems);
        return result;
    }

    private SetComparison() {
        this.oldItems = new HashSet<>();
        this.newItems = new HashSet<>();
    }

    public void addNew(final Collection<T> items) {
        if (items == null) {
            return;
        }

        newItems.addAll(items);
    }

    public void addOld(final Collection<T> items) {
        if (items == null) {
            return;
        }

        oldItems.addAll(items);
    }

    public Set<T> getAdded() {
        final Set<T> result = new HashSet<>();
        for (final T newItem : newItems) {
            if (!oldItems.contains(newItem)) {
                result.add(newItem);
            }
        }

        return result;
    }

    public Set<T> getRemoved() {
        final Set<T> result = new HashSet<>();
        for (final T oldItem : oldItems) {
            if (!newItems.contains(oldItem)) {
                result.add(oldItem);
            }
        }

        return result;
    }

    public boolean hasChanges() {
        return !(getAdded().isEmpty() && getRemoved().isEmpty());
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
