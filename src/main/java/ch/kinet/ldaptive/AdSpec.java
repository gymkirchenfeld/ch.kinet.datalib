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
package ch.kinet.ldaptive;

public final class AdSpec {

    private String password;
    private String serverName;
    private String userName;
    private boolean useSSL;

    public static AdSpec create() {
        return new AdSpec();
    }

    private AdSpec() {
    }

    public String getPassword() {
        return password;
    }

    public String getProtocol() {
        return useSSL ? "ldaps://" : "ldap://";
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

    public void setPassword(String value) {
        this.password = value;
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
        StringBuilder result = new StringBuilder();
        result.append(getProtocol());
        result.append(serverName);
        return result.toString();
    }
}
