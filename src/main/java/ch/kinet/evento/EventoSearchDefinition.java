/*
 * Copyright (C) 2017 - 2023 by Stefan Rothe, Sebastian Forster, Tom Jampen
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

import ch.kinet.Json;
import ch.kinet.JsonArray;
import ch.kinet.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EventoSearchDefinition implements Json {

    private static final String JSON_DATA_CLASS_ID = "DataClassId";
    private static final String JSON_ID = "Id";
    private static final String JSON_ID_OBJECT = "IdObject";
    private static final String JSON_DESIGNATION = "Designation";
    private static final String FIELD_ID = "FieldId";
    private static final String OPERATOR = "Operator";
    private static final String JSON_SEARCH_FIELDS = "SearchFields";
    private static final String SEARCH_TEXT = "SearchText";
    private final String context;
    private final int id;
    private final String idObject;
    private final String name;
    private final List<Field> fieldList;
    private final Map<String, Field> fieldMap;

    EventoSearchDefinition(JsonObject json) {
        this.context = json.getString(JSON_DATA_CLASS_ID);
        this.name = json.getString(JSON_DESIGNATION);
        this.id = json.getInt(JSON_ID, 0);
        this.idObject = json.getString(JSON_ID_OBJECT);
        this.fieldMap = new HashMap<>();
        this.fieldList = new ArrayList<>();
        JsonArray jsonFields = json.getArray(JSON_SEARCH_FIELDS);
        for (int i = 0; i < jsonFields.length(); ++i) {
            final Field column = new Field(jsonFields.getObject(i));
            this.fieldList.add(column);
            this.fieldMap.put(column.getName(), column);
        }
    }

    public Set<String> getFieldNames() {
        return fieldMap.keySet();
    }

    public void applyFilter(String fieldName, Filter filter) {
        if (!fieldMap.containsKey(fieldName)) {
            throw new IllegalArgumentException("Unknown field name " + fieldName);
        }

        switch (filter.getOperator()) {
            case Single:
                fieldMap.get(fieldName).setSearchText(filter.getTexts()[0]);
                break;
            case Or:
                applyCombinedFilter(fieldName, filter.getTexts(), "OR");
                break;
            case And:
                applyCombinedFilter(fieldName, filter.getTexts(), "AND");
                break;
        }
    }

    public String getContext() {
        return context;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Evento-Suchanfrage ");
        result.append(context);
        result.append("/");
        result.append(name);
        result.append(" (ID ");
        result.append(id);
        result.append("): [");
        boolean first = true;
        for (Field field : fieldList) {
            if (first) {
                first = false;
            }
            else {
                result.append(", ");
            }

            result.append(field);
        }

        result.append("]");
        return result.toString();
    }

    @Override
    public JsonObject toJsonTerse() {
        final JsonObject result = JsonObject.create();
        result.put(JSON_DATA_CLASS_ID, context);
        result.put(JSON_DESIGNATION, name);
        result.put(JSON_ID, id);
        result.put(JSON_ID_OBJECT, idObject);
        result.put(JSON_SEARCH_FIELDS, JsonArray.createTerse(fieldList));
        return result;
    }

    @Override
    public JsonObject toJsonVerbose() {
        return toJsonTerse();
    }

    private void applyCombinedFilter(String fieldName, String[] searchTexts, String operator) {
        if (searchTexts == null || searchTexts.length == 0) {
            return;
        }

        Field field = fieldMap.get(fieldName);
        field.setSearchText(searchTexts[0]);
        for (int i = 1; i < searchTexts.length; ++i) {
            Field col = new Field(JsonObject.create(field.json));
            col.setSearchText(searchTexts[i]);
            col.setOperator(operator);
            fieldList.add(col);
        }
    }

    public static final class Field implements Json {

        private final JsonObject json;

        private Field(JsonObject json) {
            this.json = json;
        }

        public String getName() {
            return json.getString(FIELD_ID);
        }

        public String getSearchText() {
            return json.getString(SEARCH_TEXT);
        }

        public void setOperator(String operator) {
            json.put(OPERATOR, operator);
        }

        public void setSearchText(String searchText) {
            json.put(SEARCH_TEXT, searchText);
        }

        @Override
        public JsonObject toJsonTerse() {
            return json;
        }

        @Override
        public JsonObject toJsonVerbose() {
            return toJsonTerse();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
