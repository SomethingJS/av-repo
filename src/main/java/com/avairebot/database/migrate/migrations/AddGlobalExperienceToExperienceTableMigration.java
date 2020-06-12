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

public class AddGlobalExperienceToExperienceTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Dec 29, 2018 10:31 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        if (schema.hasColumn(Constants.PLAYER_EXPERIENCE_TABLE_NAME, "global_experience")) {
            return true;
        }

        if (schema.getDbm().getConnection() instanceof MySQL) {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `global_experience` BIGINT NOT NULL DEFAULT '0' AFTER `experience`",
                Constants.PLAYER_EXPERIENCE_TABLE_NAME
            ));
        } else {
            schema.getDbm().queryUpdate(String.format(
                "ALTER TABLE `%s` ADD `global_experience` BIGINT NOT NULL DEFAULT '0'",
                Constants.PLAYER_EXPERIENCE_TABLE_NAME
            ));
        }

        // Assigns the current value of the "experience" column to the newly
        // create "global_experience" column for all XP records,
        // so everything can stay up to date.
        schema.getDbm().queryUpdate(String.format(
            "UPDATE `%s` SET `global_experience` = `experience`;",
            Constants.PLAYER_EXPERIENCE_TABLE_NAME
        ));

        return true;
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.PLAYER_EXPERIENCE_TABLE_NAME, "global_experience")) {
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `global_experience`;",
            Constants.PLAYER_EXPERIENCE_TABLE_NAME
        ));

        return true;
    }
}
