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
package ch.kinet.ad;

import ch.kinet.ldap.LdapConnection;
import ch.kinet.ldap.LdapException;
import ch.kinet.ldap.LdapObject;
import ch.kinet.ldap.Name;
import ch.kinet.ldap.SearchResult;
import java.util.SortedSet;
import java.util.TreeSet;

public final class AdGroup extends LdapObject {

    private static final String ADMIN_DESCRIPTION = "adminDescription";
    private static final String DESCRIPTION = "description";
    private static final String GROUP = "group";
    private static final String GROUP_TYPE = "groupType";
    private static final String MAIL = "mail";
    private static final String MEMBER = "member";
    private static final String MS_EXCH_REQUIRE_AUTH_TO_SEND_TO = "msExchRequireAuthToSendTo";
    private static final String OBJECT_CLASS = "objectClass";
    private static final String SAM_ACCOUNT_NAME = "samAccountName";
    private static final int ACCOUNT_GROUP = 0x2;
    private static final int SECURITY_GROUP = 0x80000000;
    public static final String[] ATTRIBUTES = {
        ADMIN_DESCRIPTION, DESCRIPTION, MAIL, MEMBER, MS_EXCH_REQUIRE_AUTH_TO_SEND_TO, SAM_ACCOUNT_NAME
    };
    private final SortedSet<String> members;

    AdGroup(LdapConnection connection, Name dn) throws LdapException {
        super(connection, dn);
        members = new TreeSet<>();
        setAsString(OBJECT_CLASS, GROUP);
        setAsInt(GROUP_TYPE, ACCOUNT_GROUP + SECURITY_GROUP);
    }

    AdGroup(LdapConnection connection, SearchResult searchResult, SearchResult[] members)
        throws LdapException {
        super(connection, searchResult);
        this.members = new TreeSet<>();
        for (int i = 0; i < members.length; ++i) {
            this.members.add(members[i].dn().toString());
        }
    }

    public void add(AdUser user) {
        if (members.add(user.dn().toString())) {
            setAsStrings(MEMBER, members);
        }
    }

    public boolean contains(AdUser user) {
        return members.contains(user.dn().toString());
    }

    public String getDescription() throws LdapException {
        return getAsString(DESCRIPTION);
    }

    public int getId() throws LdapException {
        return getAsInt(ADMIN_DESCRIPTION, -1);
    }

    public String getMail() throws LdapException {
        return getAsString(MAIL);
    }

    public String getSamAccountName() throws LdapException {
        return getAsString(SAM_ACCOUNT_NAME);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public boolean isInternal() throws LdapException {
        return getAsBoolean(MS_EXCH_REQUIRE_AUTH_TO_SEND_TO, false);
    }

    public String name() {
        return dn().commonName();
    }

    public void remove(AdUser user) {
        if (members.remove(user.dn().toString())) {
            setAsStrings(MEMBER, members);
        }
    }

    public void setId(int id) {
        setAsInt(ADMIN_DESCRIPTION, id);
    }

    public void setDescription(String description) {
        setAsString(DESCRIPTION, description);
    }

    public void setInternal(boolean value) {
        setAsBoolean(MS_EXCH_REQUIRE_AUTH_TO_SEND_TO, value);
    }

    public void setMail(String mail) {
        setAsString(MAIL, mail);
    }

    public void setSamAccountName(String value) {
        setAsString(SAM_ACCOUNT_NAME, value);
    }

    @Override
    public String toString() {
        return dn().commonName();
    }
}
