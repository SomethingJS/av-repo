/*
 * Copyright (c) 2018.
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

package com.avbot.database.fakes;

import com.avbot.contracts.database.Database;
import com.avbot.database.DatabaseManager;
import com.avbot.database.exceptions.DatabaseException;

import java.sql.SQLException;

public class FakeDatabaseManager extends DatabaseManager {

    public FakeDatabaseManager() {
        super(null);
    }

    @Override
    public Database getConnection() throws SQLException, DatabaseException {
        return new FakeMySQLConnection(null);
    }
}
