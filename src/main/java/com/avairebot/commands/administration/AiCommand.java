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

package com.avbot.commands.administration;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AiCommand extends Command {

    public AiCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "AI Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the AI(Artificial Intelligence) on/off for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Toggles the AI on/off for the current channel.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("ai");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,2,5"
        );
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());

        if (channelTransformer == null) {
            if (!guildTransformer.createChannelTransformer(context.getChannel())) {
                context.makeError(context.i18nRaw("errors.errorOccurredWhileLoading", "channel transformer")).queue();
                return false;
            }
            channelTransformer = guildTransformer.getChannel(context.getChannel().getId());
        }

        channelTransformer.getAI().setEnabled(!channelTransformer.getAI().isEnabled());

        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            context.makeSuccess(context.i18n("message"))
                .set("status", context.i18n(channelTransformer.getAI().isEnabled() ? "status.enabled" : "status.disabled"))
                .queue();
        } catch (SQLException ex) {
            av.getLogger().error(ex.getMessage(), ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
