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
import com.avbot.commands.Category;
import com.avbot.commands.CategoryHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.CommandPriority;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CategoriesCommand extends Command {

    public CategoriesCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Categories Command";
    }

    @Override
    public String getDescription() {
        return "Shows status of all command categories in the current or mentioned channel, both for globally and per-channel.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command [channel]` - Displays the status of the command categories in the mentioned channel, or the current channel if no channel was mentioned.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command #general`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(ToggleCategoryCommand.class, ChangePrefixCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("categories", "cats");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.LOW;
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.COMMAND_CUSTOMIZATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        TextChannel channel = context.getChannel();
        if (!context.getMentionedChannels().isEmpty()) {
            channel = context.getMentionedChannels().get(0);
        }

        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        ChannelTransformer transformer = guildTransformer.getChannel(channel.getId());

        List<String> items = new ArrayList<>();
        for (Category category : CategoryHandler.getValues()) {
            if (category.isGlobalOrSystem()) continue;

            if (!transformer.isCategoryEnabledGlobally(category)) {
                items.add(Constants.EMOTE_DND + category.getName());
                continue;
            }

            if (!transformer.isCategoryEnabled(category)) {
                items.add(Constants.EMOTE_AWAY + category.getName());
                continue;
            }

            items.add(Constants.EMOTE_ONLINE + category.getName());
        }

        context.makeInfo(context.i18n("status") + "\n\n" + String.join("\n", items))
            .setTitle(context.i18n("title", channel.getName()))
            .set("emoteEnabled", Constants.EMOTE_ONLINE)
            .set("emoteDisabledInChannel", Constants.EMOTE_AWAY)
            .set("emoteDisabledGlobally", Constants.EMOTE_DND)
            .queue();

        return true;
    }
}
