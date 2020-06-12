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
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.CacheFingerprint;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.utilities.ComparatorUtil;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "welcome-goodbye-command")
public class GoodbyeCommand extends Command {

    public GoodbyeCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Goodbye Command";
    }

    @Override
    public String getDescription() {
        return "Toggles the goodbye messages on or off for the current channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Toggles the goodbye messages on/off for the current channel");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            GoodbyeMessageCommand.class,
            WelcomeCommand.class,
            WelcomeMessageCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("goodbye", "bye");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:channel,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.JOIN_LEAVE_MESSAGES);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer channelTransformer = guildTransformer.getChannel(context.getChannel().getId());

        if (channelTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "channel settings");
        }

        ComparatorUtil.ComparatorType type = args.length == 0 ?
            ComparatorUtil.ComparatorType.UNKNOWN :
            ComparatorUtil.getFuzzyType(args[0]);

        switch (type) {
            case TRUE:
            case FALSE:
                channelTransformer.getGoodbye().setEnabled(type.getValue());
                break;

            case UNKNOWN:
                channelTransformer.getGoodbye().setEnabled(!channelTransformer.getGoodbye().isEnabled());
        }

        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .andWhere("id", context.getGuild().getId())
                .update(statement -> statement.set("channels", guildTransformer.channelsToJson(), true));

            String note = "";
            if (channelTransformer.getGoodbye().isEnabled()) {
                note = context.i18n("note", CommandHandler.getCommand(GoodbyeMessageCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
                );
            }

            context.makeSuccess(context.i18n("message") + note)
                .set("status", context.i18n(
                    channelTransformer.getGoodbye().isEnabled() ? "status.enabled" : "status.disabled"
                ))
                .queue();
        } catch (SQLException ex) {
            av.getLogger().error("Failed to update the goodbye status", ex);

            context.makeError("Failed to save the guild settings: " + ex.getMessage()).queue();
            return false;
        }

        return true;
    }
}
