/*
 * Copyright (C) 2013 - 2021 by Sebastian Forster, Stefan Rothe
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
package ch.kinet;

import ch.kinet.sql.Connection;
import ch.kinet.sql.DbSpec;
import java.util.HashMap;
import java.util.Map;

public final class DataManager {

    private final Connection connection;
    private final Map<Class, BaseData> dataMap = new HashMap<>();
    private final Object lock = new Object();

    public static DataManager create(DbSpec dbSpec) {
        final Connection connection = new Connection();
        connection.connect(dbSpec);
        return new DataManager(connection);
    }

    private DataManager(Connection connection) {
        this.connection = connection;
    }

    public void destroy() {
        connection.close();
        dataMap.clear();
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseData> T getData(Class<T> clazz) {
        synchronized (lock) {
            if (!dataMap.containsKey(clazz)) {
                createModule(clazz);
            }
        }

        return (T) dataMap.get(clazz);
    }

    Connection getConnection() {
        return connection;
    }

    private <T extends BaseData> void createModule(Class<T> clazz) {
        try {
            final BaseData data = (BaseData) clazz.getDeclaredConstructor().newInstance();
            data.initialize(this);
            dataMap.put(clazz, data);
        }
        catch (Exception ex) {
            throw new DataCreationException(clazz, ex);
        }
    }
}
