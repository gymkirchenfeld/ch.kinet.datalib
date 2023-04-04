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
package ch.kinet.ldap;

import java.util.Arrays;

public class Name implements Comparable<Name> {

    public static Name NULL = new Name(new String[0], new String[0]);
    private static final String DC = "DC";
    private static final String CN = "CN";
    private final String[] prefixes;
    private final String[] postfixes;

    public static Name fromURL(String url) {
        if (url == null) {
            throw new NullPointerException("url");
        }

        final String[] domainParts = url.split("\\.");
        final String[] prefixes = new String[domainParts.length];
        final String[] postfixes = new String[domainParts.length];
        for (int i = 0; i < domainParts.length; ++i) {
            prefixes[i] = DC;
            postfixes[i] = domainParts[i];
        }

        return new Name(prefixes, postfixes);
    }

    public static Name parse(String name) {
        final String[] parts = name.split(",");
        final String[] prefixes = new String[parts.length];
        final String[] postfixes = new String[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            String[] subParts = parts[i].split("=");
            prefixes[i] = subParts[0].toUpperCase();
            postfixes[i] = subParts[1];
        }

        return new Name(prefixes, postfixes);
    }

    private Name(String[] prefixes, String[] postfixes) {
        this.prefixes = prefixes;
        this.postfixes = postfixes;
    }

    public String commonName() {
        if (!isEmpty() && CN.equals(prefixes[0])) {
            return postfixes[0];
        }
        else {
            return null;
        }
    }

    @Override
    public int compareTo(Name other) {
        return this.toString().compareTo(other.toString());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Name) {
            Name other = (Name) object;
            return Arrays.equals(prefixes, other.prefixes) &&
                Arrays.equals(postfixes, other.postfixes);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(prefixes);
        hash = 97 * hash + Arrays.hashCode(postfixes);
        return hash;
    }

    public boolean isEmpty() {
        return prefixes.length == 0;
    }

    public String namePart(int index) {
        return postfixes[index];
    }

    public Name subContext(String name) {
        final String[] parts = name.split("=");
        return subContext(parts[0], parts[1]);
    }

    public Name subContext(String prefix, String postfix) {
        final int length = prefixes.length;
        final String[] newPrefixes = new String[length + 1];
        final String[] newPostfixes = new String[length + 1];
        newPrefixes[0] = prefix.toUpperCase();
        newPostfixes[0] = postfix;
        System.arraycopy(prefixes, 0, newPrefixes, 1, length);
        System.arraycopy(postfixes, 0, newPostfixes, 1, length);
        return new Name(newPrefixes, newPostfixes);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < prefixes.length; ++i) {
            if (i > 0) {
                result.append(',');
            }

            result.append(prefixes[i]);
            result.append('=');
            result.append(postfixes[i]);
        }

        return result.toString();
    }
}
