/*
 * Copyright (C) 2017 - 2021 by Stefan Rothe, Sebastian Forster, Tom Jampen
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
package ch.kinet.evento;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ViewDefinition {

    private final String context;
    private final Map<String, Filter> fields;
    private final String name;

    public static ViewDefinition create(String context, String name) {
        return new ViewDefinition(context, name);
    }

    private ViewDefinition(String context, String name) {
        this.context = context;
        this.fields = new HashMap<>();
        this.name = name;
    }

    public void addField(String fieldName) {
        fields.put(fieldName, null);
    }

    public String getContext() {
        return context;
    }

    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    public Filter getFilter(String fieldName) {
        return fields.get(fieldName);
    }

    public String getName() {
        return name;
    }

    public void setFilter(String fieldName, Filter filter) {
        fields.put(fieldName, filter);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(context);
        result.append('/');
        result.append(name);
        return result.toString();
    }
}
