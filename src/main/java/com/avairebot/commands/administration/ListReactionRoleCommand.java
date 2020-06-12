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
import com.avbot.chat.SimplePaginator;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.collection.Collection;
import com.avbot.database.controllers.ReactionController;
import com.avbot.language.I18n;
import com.avbot.utilities.NumberUtil;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.core.entities.Emote;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ListReactionRoleCommand extends Command {

    public ListReactionRoleCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "List Reaction Role Command";
    }

    @Override
    public String getDescription() {
        return "List reaction messages with a snippet of the message, along with what roles and emotes are linked to the message.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command [page]` - Lists all the reaction role messages on the server."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            AddReactionRoleCommand.class,
            RemoveReactionRoleCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("lrr");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(
            CommandGroups.ROLE_ASSIGNMENTS
        );
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage context, String[] args) {
        Collection collection = ReactionController.fetchReactions(av, context.getGuild());
        if (collection == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading",
                "reaction roles"
            );
        }

        if (collection.isEmpty()) {
            return sendErrorMessage(context, context.i18n("noReactionRoles",
                CommandHandler.getCommand(AddReactionRoleCommand.class)
                    .getCategory().getPrefix(context.getMessage())
                )
            );
        }

        AtomicInteger index = new AtomicInteger(1);
        List<String> records = new ArrayList<>();
        collection.forEach(row -> {
            String snippet = row.getString("snippet");

            List<String> content = new ArrayList<>();
            if (row.getString("roles", null) != null) {
                HashMap<Long, Long> dbRoles = av.gson.fromJson(
                    row.getString("roles"),
                    new TypeToken<HashMap<Long, Long>>() {
                    }.getType());

                for (Map.Entry<Long, Long> item : dbRoles.entrySet()) {
                    Emote emote = av.getShardManager().getEmoteById(item.getKey());
                    if (emote == null) {
                        continue;
                    }

                    content.add(I18n.format("{0} = <@&{1}>",
                        emote.getAsMention(), item.getValue()
                    ));
                }
            }

            records.add(I18n.format(
                "**[#{0}:](https://discordapp.com/channels/{1}/{2}/{3})** {4}\n **Roles >** {5}",
                index.getAndIncrement(),
                row.getString("guild_id"),
                row.getString("channel_id"),
                row.getString("message_id"),
                snippet.substring(0, Math.min(snippet.length(), 42)).trim() + "...",
                String.join(", ", content)
            ));
        });

        List<String> messages = new ArrayList<>();
        SimplePaginator<String> paginator = new SimplePaginator<>(records, 5);
        if (args.length > 0) {
            paginator.setCurrentPage(NumberUtil.parseInt(args[0], 1));
        }

        paginator.forEach((_index, key, val) -> messages.add(val));
        messages.add("\n" + paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage())));

        context.makeInfo(String.join("\n", messages))
            .setTitle(context.i18n("listReactionRoles",
                collection.size()
            )).queue();

        return false;
    }
}
