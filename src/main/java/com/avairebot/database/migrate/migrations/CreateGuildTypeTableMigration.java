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

package com.avbot.database.migrate.migrations;

import com.avbot.Constants;
import com.avbot.contracts.database.migrations.Migration;
import com.avbot.database.schema.Schema;

import java.sql.SQLException;

public class CreateGuildTypeTableMigration implements Migration {

    private int id = 1;

    @Override
    public String created_at() {
        return "Wed, Sep 20, 2017 5:23 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.GUILD_TYPES_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("name");
            table.Text("limits");
            table.Timestamps();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.GUILD_TYPES_TABLE_NAME);
    }
}
