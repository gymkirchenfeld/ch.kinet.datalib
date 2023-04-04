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
package ch.kinet.sql;

/**
 * This class encapsulates the information needed to connect to a database.
 */
public class DbSpec {

    public enum Dbms {

        SqlServer, Postgresql
    }
    private final Dbms dbms;
    private String database;
    private String dbServer;
    private char[] password;
    private int port;
    private String userName;
    private boolean sslEnabled;

    public static DbSpec create(Dbms dbms) {
        return new DbSpec(dbms);
    }

    public String getDatabase() {
        return database;
    }

    public Dbms getDbms() {
        return dbms;
    }

    public String getDbServer() {
        return dbServer;
    }

    public char[] getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public boolean getSslEnabled() {
        return sslEnabled;
    }

    public String getUserName() {
        return userName;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    private DbSpec(final Dbms dbms) {
        this.dbms = dbms;
        switch (this.dbms) {
            case Postgresql:
                port = 5432;
                break;
            case SqlServer:
                port = 1433;
                break;
        }
    }
}
