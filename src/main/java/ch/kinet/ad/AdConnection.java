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
package ch.kinet.ad;

import ch.kinet.Util;
import ch.kinet.ldap.LdapConnection;
import ch.kinet.ldap.Name;
import ch.kinet.ldap.Query;
import ch.kinet.ldap.SearchResult;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AdConnection extends LdapConnection {

    private static final String GROUP_FILTER = "(objectClass=group)";
    private static final String MEMBER_FILTER = "(&(objectClass=user)(memberOf={0}))";
    private static final String USER_FILTER = "(objectClass=user)";

    public static AdConnection create() {
        return new AdConnection();
    }

    private AdConnection() {
    }

    public AdUser findUser(Name context, String accountName) {
        Query query = createQuery(context);
        query.setFilter("(&(" + USER_FILTER + ")(samAccountName=" + accountName + "))");
        query.addAttributes(AdUser.ATTRIBUTES);
        List<SearchResult> searchResults = query.execute().collect(Collectors.toList());
        return searchResults.size() == 1 ? new AdUser(this, searchResults.get(0)) : null;
    }

    public AdGroup createGroup(Name dn) {
        return new AdGroup(this, dn);
    }

    public AdUser createUser(Name dn) {
        return new AdUser(this, dn);
    }

    public Stream<AdGroup> loadGroups(Name context) {
        Query query = createQuery(context);
        query.setFilter(GROUP_FILTER);
        query.addAttributes(AdGroup.ATTRIBUTES);
        return query.execute().map(item -> {
            // Workaround: AD does not reliably deliver all members in the member attribute of a group. If the
            // group has more than 1500 members, none are returned.
            // As a workaround, we execute a query returning all members of a group for every group.
            Query memberQuery = createQuery(rootName());
            memberQuery.setFilter(Util.args(MEMBER_FILTER, item.dn()));
            return new AdGroup(this, item, memberQuery.execute());
        });
    }

    public Stream<AdUser> loadUsers(Name context) {
        Query query = createQuery(context);
        query.setFilter(USER_FILTER);
        query.addAttributes(AdUser.ATTRIBUTES);
        return query.execute().map(item -> new AdUser(this, item));
    }
}
