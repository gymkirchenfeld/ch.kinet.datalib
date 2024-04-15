/*
 * Copyright (C) 2012 - 2024 by Stefan Rothe
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
package ch.kinet.ldap;

import ch.kinet.Util;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

public abstract class LdapObject implements Comparable<LdapObject> {

    // Contains the attributes to be added
    private final Attributes add;
    private final LdapConnection connection;
    private final Name dn;
    // Attributes that always exist and should not be created.
    private final Set<String> existingAttributes;
    // Contains the original attributes read from the directory
    private final Attributes original;
    // Contains the attributes to be removed
    private final Attributes remove;
    // Contains the attributes to be updated
    private final Attributes update;
    private boolean isNew;

    protected LdapObject(LdapConnection connection, Name dn) {
        this.add = new BasicAttributes();
        this.connection = connection;
        this.dn = dn;
        this.existingAttributes = new HashSet<>();
        this.original = new BasicAttributes();
        this.remove = new BasicAttributes();
        this.update = new BasicAttributes();
        this.isNew = true;
    }

    protected LdapObject(LdapConnection connection, SearchResult searchResult,
                         String... existingAttributes) {
        this.add = new BasicAttributes();
        this.connection = connection;
        this.dn = searchResult.dn();
        this.existingAttributes = new HashSet<>(Arrays.asList(existingAttributes));
        this.original = searchResult.attributes();
        this.remove = new BasicAttributes();
        this.update = new BasicAttributes();
        this.isNew = false;
    }

    @Override
    public int compareTo(LdapObject other) {
        if (other == null) {
            return 1;
        }
        else {
            return dn.compareTo(other.dn);
        }
    }

    public final Name dn() {
        return dn;
    }

    public final boolean isModified() {
        return add.size() + remove.size() + update.size() > 0;
    }

    public final boolean isNew() {
        return isNew;
    }

    public final void save() {
        if (!isModified()) {
            return;
        }

        if (isNew) {
            connection.createSubcontext(dn, add);
            isNew = false;
        }
        else {
            connection.addAttributes(dn, add);
            connection.removeAttributes(dn, remove);
            connection.updateAttributes(dn, update);
        }
    }

    public boolean getAsBoolean(String name, boolean defaultValue) {
        try {
            String value = getAsString(name);
            if (value == null) {
                return defaultValue;
            }

            switch (value) {
                case "TRUE":
                    return true;
                case "FALSE":
                    return false;
                default:
                    return defaultValue;
            }
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public int getAsInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(getAsString(name));
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public String getAsString(String name) {
        try {
            Attribute attr = original.get(name);
            if (attr == null) {
                return null;
            }
            else {
                return attr.get().toString();
            }
        }
        catch (NamingException ex) {
            throw new AttributeReadException(dn, name, ex);
        }
    }

    public Stream<String> getAsStrings(String name) {
        List<String> result = new ArrayList<>();
        try {
            Attribute attr = original.get(name);
            if (attr != null) {
                NamingEnumeration it = attr.getAll();
                while (it.hasMore()) {
                    result.add(it.next().toString());
                }
            }
            return result.stream();
        }
        catch (NamingException ex) {
            throw new AttributeReadException(dn, name, ex);
        }
    }

    public final void setAsBoolean(String name, boolean value) {
        setAsString(name, value ? "TRUE" : "FALSE");
    }

    public void setAsEncodedBytes(String name, String value, String charsetName) {
        if (Util.isEmpty(value)) {
            clearAttribute(name);
        }
        else {
            try {
                setAttribute(new BasicAttribute(name, value.getBytes(charsetName)));
            }
            catch (UnsupportedEncodingException ex) {
                throw new LdapException("Unsupported encoding.");
            }
        }
    }

    public final void setAsInt(String name, int value) {
        setAsString(name, String.valueOf(value));
    }

    public final void setAsString(String name, String value) {
        if (Util.isEmpty(value)) {
            clearAttribute(name);
        }
        else {
            setAttribute(new BasicAttribute(name, value));
        }
    }

    public void setAsStrings(String name, Collection<String> value) {
        if (value.isEmpty()) {
            clearAttribute(name);
        }
        else {
            Attribute attr = new BasicAttribute(name);
            for (String item : value) {
                attr.add(item);
            }

            setAttribute(attr);
        }
    }

    @Override
    public String toString() {
        return dn.toString();
    }

    // This method is called if the attribute is empty and should be remove
    // from the directory.
    private void clearAttribute(String name) {
        // We don't add or update the attribute anymore.
        add.remove(name);
        update.remove(name);
        // But we remove it, if it is present
        if (original.get(name) != null) {
            remove.put(new BasicAttribute(name));
        }
    }

    // This method is called if the attribute is not empty and should be set
    // in the directory.
    private void setAttribute(Attribute attribute) {
        // We don't remove the attribute anymore.
        remove.remove(attribute.getID());

        // Check if the attribute is already present in the directory
        if (original.get(attribute.getID()) != null || existingAttributes.contains(attribute.getID())) {
            // If present, replace it.
            update.put(attribute);
            add.remove(attribute.getID());
        }
        else {
            // Otherwise, add it.
            add.put(attribute);
            update.remove(attribute.getID());
        }
    }
}
