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

package com.avbot.middleware.global;

import com.avbot.av;
import com.avbot.commands.Category;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.administration.ToggleCategoryCommand;
import com.avbot.contracts.middleware.Middleware;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.factories.MessageFactory;
import com.avbot.middleware.MiddlewareStack;
import com.avbot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class IsCategoryEnabled extends Middleware {

    private static final HashMap<String, String> disabledCategories;

    static {
        disabledCategories = new HashMap<>();
    }

    public IsCategoryEnabled(av av) {
        super(av);
    }

    public static void enableCategory(Category category) {
        disabledCategories.remove(category.getName());
    }

    public static void disableCategory(Category category, @Nullable String reason) {
        disabledCategories.put(category.getName(), reason);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (disabledCategories.containsKey(stack.getCommandContainer().getCategory().getName())) {
            if (av.getBotAdmins().getUserById(message.getAuthor().getIdLong()).isAdmin()) {
                return stack.next();
            }

            String disabledMessage = disabledCategories.get(stack.getCommandContainer().getCategory().getName());
            if (disabledMessage == null) {
                disabledMessage = "The :category command category is currently disable globally by a bot administrator.";
            }

            String finalDisabledMessage = disabledMessage;
            return runMessageCheck(message, () -> {
                MessageFactory.makeError(message, finalDisabledMessage)
                    .set("category", stack.getCommandContainer().getCategory().getName())
                    .queue(success -> success.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));

                return false;
            });
        }

        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (isCategoryCommands(stack) || stack.getCommandContainer().getCategory().isGlobalOrSystem()) {
            return stack.next();
        }

        GuildTransformer transformer = stack.getDatabaseEventHolder().getGuild();
        if (transformer == null) {
            return stack.next();
        }

        ChannelTransformer channel = transformer.getChannel(message.getChannel().getId());
        if (channel == null) {
            return stack.next();
        }

        if (!channel.isCategoryEnabled(stack.getCommandContainer().getCategory())) {
            if (isHelpCommand(stack) && stack.isMentionableCommand()) {
                MessageFactory.makeError(message, "The help command is disabled in this channel, you can enable it by using the `:category` command.")
                    .set("category", CommandHandler.getCommand(ToggleCategoryCommand.class).getCommand().generateCommandTrigger(message))
                    .queue(success -> success.delete().queueAfter(15, TimeUnit.SECONDS, null, RestActionUtil.ignore));
            }

            return false;
        }

        return stack.next();
    }

    private boolean isCategoryCommands(MiddlewareStack stack) {
        return stack.getCommand().getClass().getTypeName().equals("com.avbot.commands.administration.ToggleCategoryCommand") ||
            stack.getCommand().getClass().getTypeName().equals("com.avbot.commands.administration.CategoriesCommand");
    }

    private boolean isHelpCommand(MiddlewareStack stack) {
        return stack.getCommand().getClass().getTypeName().equals("com.avbot.commands.help.HelpCommand");
    }
}
