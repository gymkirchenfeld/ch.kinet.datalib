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

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapConnection {

    private static final String TRUST_STORE = "javax.net.ssl.trustStore";
    private String domainName;
    private Name rootName;
    private LdapContext context = null;

    protected LdapConnection() {
    }

    public void close() {
        if (context != null) {
            try {
                context.close();
            }
            catch (NamingException ex) {
                // ignore
            }
        }
    }

    public void connect(LdapSpec spec) throws LdapException {
        String keystore = System.getProperty("java.home") + "/lib/security/cacerts";
        System.setProperty(TRUST_STORE, keystore);
        domainName = spec.getDomain();
        rootName = Name.fromURL(spec.getDomain());
        String principal = userPrincipalName(spec.getUserName());

        Hashtable<Object, Object> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        if (spec.getUseSSL()) {
            environment.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        environment.put(Context.PROVIDER_URL, spec.url());
        environment.put(Context.SECURITY_AUTHENTICATION, "simple");
        environment.put(Context.SECURITY_PRINCIPAL, principal);

        String password = spec.getPassword().toString();
        if (password == null || password.isEmpty()) {
            throw new AuthenticationFailedException(spec);
        }

        environment.put(Context.SECURITY_CREDENTIALS, spec.getPassword());
        environment.put(Context.REFERRAL, "follow");
        environment.put("java.naming.ldap.attributes.binary", "objectSid");
        try {
            context = new InitialLdapContext(environment, null);
        }
        catch (AuthenticationException ex) {
            throw new AuthenticationFailedException(spec, ex);
        }
        catch (Exception ex) {
            throw new ConnectionFailedException(spec, ex);
        }
    }

    public Query createQuery(Name baseContext) {
        return new Query(context, baseContext);
    }

    public void remove(Name dn) throws LdapException {
        try {
            context.unbind(dn.toString());
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Removing", dn);
        }
        catch (NamingException ex) {
            throw new OperationFailedException("Removing", dn, ex);
        }
    }

    public void rename(Name dn, Name newDn) throws LdapException {
        try {
            context.rename(dn.toString(), newDn.toString());
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Renaming", dn);
        }
        catch (NamingException ex) {
            throw new OperationFailedException("Renaming", dn, ex);
        }
    }

    public Name rootName() {
        return rootName;
    }

    void addAttributes(Name dn, Attributes attributes) throws LdapException {
        try {
            context.modifyAttributes(dn.toString(), DirContext.ADD_ATTRIBUTE, attributes);
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Adding attributes", dn);
        }
        catch (final NamingException ex) {
            throw new OperationFailedException("Adding attributes", dn, ex);
        }
    }

    void createSubcontext(Name dn, Attributes attributes) throws LdapException {
        try {
            context.createSubcontext(dn.toString(), attributes);
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Creation", dn);
        }
        catch (NamingException ex) {
            throw new OperationFailedException("Creation", dn, ex);
        }
    }

    void removeAttributes(Name dn, Attributes attributes) throws LdapException {
        try {
            context.modifyAttributes(dn.toString(), DirContext.REMOVE_ATTRIBUTE, attributes);
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Removing attributes", dn);
        }
        catch (NamingException ex) {
            throw new OperationFailedException("Removing attributes", dn, ex);
        }
    }

    void updateAttributes(Name dn, Attributes attributes) throws LdapException {
        try {
            context.modifyAttributes(dn.toString(), DirContext.REPLACE_ATTRIBUTE, attributes);
        }
        catch (javax.naming.NoPermissionException ex) {
            throw new NoPermissionException("Updating attributes", dn);
        }
        catch (NamingException ex) {
            throw new OperationFailedException("Updating attributes", dn, ex);
        }
    }

    private String userPrincipalName(String userName) {
        return userName + "@" + domainName;
    }
}
