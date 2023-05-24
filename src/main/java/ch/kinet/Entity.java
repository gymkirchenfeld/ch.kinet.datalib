/*
 * Copyright (C) 2012 - 2023 by Stefan Rothe
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

import ch.kinet.reflect.Persistence;
import ch.kinet.reflect.PropertyInit;

/**
 * Represents an object that is identified by a unique integer.
 */
public class Entity implements Comparable<Entity>, Json {

    public static final String DB_ID = "Id";
    public static final String JSON_ID = "id";
    private final int id;

    protected Entity(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Entity other) {
        return this.doCompare(other);
    }

    @Override
    public final boolean equals(Object object) {
        if (object instanceof Entity) {
            return id == ((Entity) object).id;
        }
        else {
            return super.equals(object);
        }
    }

    @Persistence(key = true, init = PropertyInit.AutoIncrement)
    public final int getId() {
        return id;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public JsonObject toJsonTerse() {
        JsonObject result = JsonObject.create();
        result.put(JSON_ID, id);
        return result;
    }

    @Override
    public JsonObject toJsonVerbose() {
        return toJsonTerse();
    }

    protected int doCompare(Entity other) {
        if (other == null) {
            return 1;
        }

        return id - other.id;
    }
}
