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
import com.avbot.database.connections.MySQL;
import com.avbot.database.schema.Schema;

import java.sql.SQLException;

public class AddLevelExemptChannelsToGuildsTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Wed, Dec 12, 2018 7:13 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.GUILD_TABLE_NAME, "level_exempt_channels")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `level_exempt_channels` TEXT NULL DEFAULT NULL AFTER `level_roles`;",
                Constants.GUILD_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `level_exempt_channels` TEXT;",
                Constants.GUILD_TABLE_NAME
            ));
        }

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "level_exempt_channels")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `level_exempt_channels`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
