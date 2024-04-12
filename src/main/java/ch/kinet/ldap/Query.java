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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

public class Query {

    private static final int SEARCH_PAGE_SIZE = 500;
    private final List<String> attributes;
    private final LdapContext context;
    private final Name name;
    private String filter;

    static Query create(LdapContext context, Name name) {
        return new Query(context, name);
    }

    public SearchResult[] execute() throws QueryException {
        final List<SearchResult> result = new ArrayList<>();
        try {
            byte[] cookie = null;
            SearchControls sc = new SearchControls();
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            sc.setReturningAttributes(attributes.toArray(new String[attributes.size()]));

            Control control = new PagedResultsControl(SEARCH_PAGE_SIZE, Control.CRITICAL);
            context.setRequestControls(new Control[]{control});
            do {
                NamingEnumeration results = context.search(this.name.toString(), this.filter, sc);
                while (results != null && results.hasMoreElements()) {
                    result.add(new SearchResult((javax.naming.directory.SearchResult) results.next()));
                }

                Control[] controls = context.getResponseControls();
                if (controls != null) {
                    for (Control ctrl : controls) {
                        if (ctrl instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) ctrl;
                            cookie = prrc.getCookie();
                        }
                    }
                }

                control = new PagedResultsControl(SEARCH_PAGE_SIZE, cookie, Control.CRITICAL);
                context.setRequestControls(new Control[]{control});
            }
            while (cookie != null);
        }
        catch (final NamingException ex) {
            throw new QueryException(this, ex);
        }
        catch (final IOException ex) {
            throw new QueryException(this, ex);
        }

        return result.toArray(new SearchResult[result.size()]);
    }

    public Query(LdapContext context, Name name) {
        attributes = new ArrayList<>();
        this.context = context;
        this.filter = null;
        this.name = name;
    }

    public void addAttributes(String... values) {
        attributes.addAll(Arrays.asList(values));
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("Query(name='");
        result.append(name);
        result.append("', filter='");
        result.append(filter);
        result.append("', attributes='");
        for (String attribute : attributes) {
            result.append(attribute);
            result.append(", ");
        }

        result.append("')");
        return result.toString();
    }
}
