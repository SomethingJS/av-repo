/*
 * Copyright (c) 2019.
 *
 * This file is part of av.
 *
 * av is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * av is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with av.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avbot.contracts.database.seeder;

import com.avbot.av;
import com.avbot.contracts.reflection.Reflectionable;
import com.avbot.database.query.QueryBuilder;

import java.sql.SQLException;

public abstract class Seeder extends Reflectionable {

    /**
     * Creates a new seeder class instance.
     *
     * @param av The main {@link av av} application instance.
     */
    public Seeder(av av) {
        super(av);
    }

    /**
     * Gets the channel that the {@link #createQuery() query builder}
     * should be created for.
     *
     * @return The table used by the seeder.
     */
    public abstract String table();

    /**
     * Attempts to seeder the database, this is automatically executed from the
     * {@link com.avbot.database.seeder.SeederManager#run()} method.
     *
     * @return the result of the schematic instance call
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public abstract void run() throws SQLException;

    /**
     * Creates a new query builder for the table defined in
     * the {@link #table() table method}.
     *
     * @return A new query builder instance set to the table defined for the seeder.
     */
    protected QueryBuilder createQuery() {
        return av.getDatabase().newQueryBuilder(table());
    }

    /**
     * Checks if the table defined for the seeder has a record
     * that exists with the given column and value.
     *
     * @param column The column that should be checked
     * @param value  The value that should exist for the given column.
     * @return {@code True} if the colum exists, {@code False} otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    protected boolean tableHasValue(String column, Object value) throws SQLException {
        return !createQuery().where(column, value).get().isEmpty();
    }
}
