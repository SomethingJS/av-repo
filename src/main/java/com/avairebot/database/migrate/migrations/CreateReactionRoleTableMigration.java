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

public class CreateReactionRoleTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Nov 10, 2018 5:10 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        return schema.createIfNotExists(Constants.REACTION_ROLES_TABLE_NAME, table -> {
            table.Long("guild_id").unsigned();
            table.Long("channel_id").unsigned();
            table.Long("message_id").unsigned();
            table.String("snippet");
            table.Text("roles");
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        return schema.dropIfExists(Constants.REACTION_ROLES_TABLE_NAME);
    }
}
