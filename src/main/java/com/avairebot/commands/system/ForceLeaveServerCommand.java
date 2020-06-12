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
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.SystemCommand;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Collections;
import java.util.List;

public class ForceLeaveServerCommand extends SystemCommand {

    public ForceLeaveServerCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Force Leave Server Command";
    }

    @Override
    public String getDescription() {
        return "Force leaves a server with the given ID.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <id>` - Leaves the server with the given ID if the bot is on the server."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 304414699645042690`");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("force-leave");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "You must include the ID of the server you want the bot to leave.");
        }

        try {
            Guild guild = av.getShardManager().getGuildById(args[0]);
            if (guild == null) {
                return sendErrorMessage(context, "The bot is not in any guild with an ID of `{0}`", args[0]);
            }

            String name = guild.getName();
            guild.leave().queue();

            context.makeSuccess("The bot has successfully left the **:name** server with an ID of :id")
                .set("name", name)
                .set("id", args[0])
                .queue();

        } catch (NumberFormatException e) {
            return sendErrorMessage(context, "Invalid format for an ID given, expecting a valid long value!");
        }

        return true;
    }
}
