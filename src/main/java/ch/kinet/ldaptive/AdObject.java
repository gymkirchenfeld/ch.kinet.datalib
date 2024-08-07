/*
 * Copyright (C) 2024 by Stefan Rothe
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
package ch.kinet.ldaptive;

import ch.kinet.Util;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

public class AdObject {

    private static final String UNICODE_PWD = "unicodePwd";
    private static final String FALSE = "FALSE";
    private static final String TRUE = "TRUE";

    private final Map<String, LdapAttribute> attributes;
    private final String dn;
    private final Map<String, AttributeModification.Type> modifications = new HashMap<>();

    public static AdObject create(String dn) {
        return new AdObject(dn);
    }

    private AdObject(String dn) {
        attributes = new HashMap<>();
        this.dn = dn;
    }

    AdObject(LdapEntry entry) {
        attributes = entry.getAttributes().stream().collect(
            Collectors.toMap(LdapAttribute::getName, x -> x)
        );
        this.dn = entry.getDn();
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            String value = getString(name);
            if (value == null) {
                return defaultValue;
            }

            switch (value) {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                default:
                    return defaultValue;
            }
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String getDn() {
        return dn;
    }

    public int getInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(getString(name));
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String getString(String name) {
        LdapAttribute attribute = attributes.get(name);
        return attribute == null ? null : attribute.getStringValue();
    }

    public Stream<String> getStrings(String name) {
        LdapAttribute attribute = attributes.get(name);
        return attribute == null ? Stream.empty() : attribute.getStringValues().stream();
    }

    public boolean hasModifications() {
        return !modifications.isEmpty();
    }

    public void setBoolean(String name, boolean value) {
        setString(name, value ? TRUE : FALSE);
    }

    public void setInt(String name, int value) {
        setString(name, String.valueOf(value));
    }

    public void setString(String name, String value) {
        AttributeModification.Type type = getModificationType(name, Util.isEmpty(value));
        if (type == null) {
            return;
        }

        modifications.put(name, type);
        if (type != AttributeModification.Type.DELETE) {
            attributes.put(name, new LdapAttribute(name, value));
        }
    }

    public void setStrings(String name, Stream<String> values) {
        List<String> list = values.filter(value -> !Util.isEmpty(value)).collect(Collectors.toList());
        AttributeModification.Type type = getModificationType(name, list.isEmpty());
        if (type == null) {
            return;
        }

        modifications.put(name, type);
        if (type != AttributeModification.Type.DELETE) {
            LdapAttribute attribute = new LdapAttribute(name);
            attribute.addStringValues(list);
            attributes.put(name, attribute);
        }
    }

    public void setUnicodePassword(String value) {
        try {
            String newQuotedPassword = "\"" + value + "\"";
            byte[] password = newQuotedPassword.getBytes("UTF-16LE");
            LdapAttribute attribute = new LdapAttribute(UNICODE_PWD, password);
            modifications.put(UNICODE_PWD, AttributeModification.Type.REPLACE);
            attributes.put(UNICODE_PWD, attribute);
        }
        catch (UnsupportedEncodingException ex) {
            // ignore
        }
    }

    @Override
    public String toString() {
        return dn;
    }

    LdapAttribute[] attributes() {
        return attributes.values().toArray(size -> new LdapAttribute[size]);
    }

    AttributeModification[] modifications() {
        List<AttributeModification> result = new ArrayList<>();
        for (Map.Entry<String, AttributeModification.Type> entry : modifications.entrySet()) {
            result.add(new AttributeModification(entry.getValue(), attributes.get(entry.getKey())));
        }

        return result.toArray(size -> new AttributeModification[size]);
    }

    private AttributeModification.Type getModificationType(String name, boolean isEmpty) {
        if (attributes.containsKey(name)) {
            return isEmpty ? AttributeModification.Type.DELETE : AttributeModification.Type.REPLACE;
        }
        else {
            return isEmpty ? null : AttributeModification.Type.ADD;
        }
    }
}
