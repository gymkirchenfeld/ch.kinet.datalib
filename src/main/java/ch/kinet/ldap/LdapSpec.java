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

public final class LdapSpec {

    private String domain;
    private char[] password;
    private int port;
    private String serverName;
    private String userName;
    private boolean useSSL;

    public static LdapSpec create() {
        return new LdapSpec();
    }

    public static LdapSpec create(LdapSpec orig) {
        final LdapSpec result = new LdapSpec();
        result.domain = orig.domain;
        result.password = orig.password;
        result.port = orig.port;
        result.serverName = orig.serverName;
        result.userName = orig.userName;
        result.useSSL = orig.useSSL;
        return result;
    }

    public String getDomain() {
        return domain;
    }

    public char[] getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        if (useSSL) {
            return "ldaps://";
        }
        else {
            return "ldap://";
        }
    }

    public String getServerName() {
        return serverName;
    }

    public String getUserName() {
        return userName;
    }

    public boolean getUseSSL() {
        return useSSL;
    }

    public void setDomain(String value) {
        this.domain = value;
    }

    public void setPassword(char[] value) {
        this.password = value;
    }

    public void setPort(int value) {
        this.port = value;
    }

    public void setServerName(String value) {
        this.serverName = value;
    }

    public void setUserName(String value) {
        this.userName = value;
    }

    public void setUseSSL(boolean value) {
        this.useSSL = value;
    }

    public String url() {
        final StringBuilder result = new StringBuilder();
        result.append(getProtocol());
        result.append(getServerName());
        result.append(':');
        result.append(getPort());
        return result.toString();
    }

    private LdapSpec() {
    }
}
