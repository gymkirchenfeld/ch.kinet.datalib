/*
 * Copyright (C) 2012 - 2021 by Stefan Rothe
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
package ch.kinet.sql;

import ch.kinet.reflect.Property;
import java.util.HashMap;
import java.util.Map;

class Lookup<T> {

    private final Map<Object, T> lookup;
    private final Property keyPropery;

    Lookup(Property keyProperty) {
        this.lookup = new HashMap<>();
        this.keyPropery = keyProperty;
    }

    void add(T object) {
        this.lookup.put(this.keyPropery.getValue(object), object);
    }

    T get(Object key) {
        return this.lookup.get(key);
    }

    String getKeyPropertyName() {
        return this.keyPropery.getName();
    }
}
