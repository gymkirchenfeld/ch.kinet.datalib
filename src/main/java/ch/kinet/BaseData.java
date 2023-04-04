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

/**
 * Base class for data objects.
 */
public abstract class BaseData {

    private Connection connection;
    private boolean initializing = false;

    /**
     * Initializes the data object.
     */
    protected BaseData() {
    }

    /**
     * Returns the database connection.
     *
     * @return the database connection
     */
    protected final Connection getConnection() {
        return connection;
    }

    protected abstract void doInitDependencies(DataManager dataManager);

    protected abstract void doInitLookups(Connection connection);

    protected abstract void doInitData();

    final void initialize(DataManager dataManager) {
        if (initializing) {
            throw new IllegalStateException("Already initializing");
        }

        initializing = true;
        connection = dataManager.getConnection();
        doInitDependencies(dataManager);
        doInitLookups(connection);
        doInitData();
    }
}
