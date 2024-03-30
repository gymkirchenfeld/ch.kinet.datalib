/*
 * Copyright (C) 2017 - 2022 by Stefan Rothe, Sebastian Forster, Tom Jampen
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

public final class EventoConfig {

    private String apiBase;
    private String caCertStorePath;
    private char[] caCertStorePassword;
    private String clientId;
    private String instance;
    private char[] keyPassword;
    private boolean keyStoreJKS;
    private String keyStorePath;
    private char[] keyStorePassword;
    private String oauthBase;
    private String secret;
    private String server;
    private String userName;
    private String userPassword;

    public static EventoConfig create() {
        return new EventoConfig();
    }

    private EventoConfig() {
    }

    public String getApiBase() {
        return apiBase;
    }

    public char[] getCaCertStorePassword() {
        return caCertStorePassword;
    }

    public String getCaCertStorePath() {
        return caCertStorePath;
    }

    public String getClientId() {
        return clientId;
    }

    public String getInstance() {
        return instance;
    }

    public char[] getKeyPassword() {
        return keyPassword;
    }

    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getOauthBase() {
        return oauthBase;
    }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public boolean isKeyStoreJKS() {
        return keyStoreJKS;
    }

    public void setApiBase(String apiEndpoint) {
        this.apiBase = apiEndpoint;
    }

    public void setCaCertStorePassword(String caCertStorePassword) {
        if (caCertStorePassword != null) {
            this.caCertStorePassword = caCertStorePassword.toCharArray();
        }
    }

    public void setCaCertStorePath(String caCertStorePath) {
        this.caCertStorePath = caCertStorePath;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setKeyPassword(String keyPassword) {
        if (keyPassword != null) {
            this.keyPassword = keyPassword.toCharArray();
        }
    }

    public void setKeyStoreJKS(boolean keyStoreJKS) {
        this.keyStoreJKS = keyStoreJKS;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        if (keyStorePassword != null) {
            this.keyStorePassword = keyStorePassword.toCharArray();
        }
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public void setOauthBase(String oauthEndpoint) {
        this.oauthBase = oauthEndpoint;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
