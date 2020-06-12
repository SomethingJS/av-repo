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

package com.avbot.commands.system;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.CommandPriority;
import com.avbot.contracts.commands.SystemCommand;
import com.avbot.database.collection.Collection;
import com.avbot.database.controllers.GuildController;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.database.transformers.GuildTypeTransformer;
import com.avbot.utilities.NumberUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetGuildTypeCommand extends SystemCommand {

    public SetGuildTypeCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Set Guild Type Command";
    }

    @Override
    public String getDescription() {
        return "Sets the Guild Type of the server the command was ran in, if no arguments was given the current Guild Type will be displayed instead.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current guild type.",
            "`:command list` - List available guild types.",
            "`:command <type id>` - Changes the guild type to the given type."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command 2` - Sets the guild type to VIP+",
            "`:command list` - Lists the available guild types."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("set-type", "settype");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM_ROLE;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context,
                "Something went wrong while trying to get the guild transformer object, check the console for more information."
            );
        }

        if (args.length == 0) {
            GuildTypeTransformer type = guildTransformer.getType();

            context.makeInfo("The server type for **:guildname** is set to **:type**")
                .set("guildname", context.getGuild().getName())
                .set("type", type.getName())
                .queue();

            return true;
        }

        if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("types")) {
            try {
                List<String> types = new ArrayList<>();
                types.add("**0:** Default");

                av.getDatabase()
                    .newQueryBuilder(Constants.GUILD_TYPES_TABLE_NAME)
                    .orderBy("id", "asc")
                    .get()
                    .forEach(row -> {
                        types.add(String.format(
                            "**%s:** %s",
                            row.get("id"),
                            row.get("name")
                        ));
                    });

                context.makeInfo(String.join("\n", types))
                    .setTitle("Guild Types")
                    .setFooter(String.format(
                        "Change guild type with: %s <type id>",
                        generateCommandTrigger(context.getMessage())
                    ))
                    .queue();

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        if (!NumberUtil.isNumeric(args[0])) {
            return sendErrorMessage(context, "Invalid guild type given, the guild type must be a number.");
        }

        int typeId = NumberUtil.parseInt(args[0], 0);
        String typeName = "Default";

        if (typeId > 0) {
            try {
                Collection collection = av.getDatabase()
                    .newQueryBuilder(Constants.GUILD_TYPES_TABLE_NAME)
                    .select("name")
                    .where("id", typeId)
                    .get();

                if (collection.isEmpty()) {
                    return sendErrorMessage(context, "Invalid guild type given, `{0}` is not a valid guild type ID.", "" + typeId);
                }

                typeName = collection.first().getString("name");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            av.getDatabase()
                .newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("type", typeId));

            GuildController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess("The guild type for **:guildname** has been changed to **:type**")
                .set("guildname", context.getGuild().getName())
                .set("type", typeName)
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }
}
