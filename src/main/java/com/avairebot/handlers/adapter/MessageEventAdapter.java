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

package com.avbot.handlers.adapter;

import com.avbot.AppInfo;
import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandContainer;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.help.HelpCommand;
import com.avbot.contracts.handlers.EventAdapter;
import com.avbot.database.collection.Collection;
import com.avbot.database.collection.DataRow;
import com.avbot.database.controllers.GuildController;
import com.avbot.database.controllers.PlayerController;
import com.avbot.database.controllers.ReactionController;
import com.avbot.database.query.QueryBuilder;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.factories.MessageFactory;
import com.avbot.handlers.DatabaseEventHolder;
import com.avbot.language.I18n;
import com.avbot.middleware.MiddlewareStack;
import com.avbot.shared.DiscordConstants;
import com.avbot.utilities.ArrayUtil;
import com.avbot.utilities.RestActionUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MessageEventAdapter extends EventAdapter {

    public static final Set<Long> hasReceivedInfoMessageInTheLastMinute = new HashSet<>();

    private static final ExecutorService commandService = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder()
            .setNameFormat("av-command-thread-%d")
            .build()
    );

    private static final Logger log = LoggerFactory.getLogger(MessageEventAdapter.class);
    private static final Pattern userRegEX = Pattern.compile("<@(!|)+[0-9]{16,}+>", Pattern.CASE_INSENSITIVE);
    private static final String mentionMessage = String.join("\n", Arrays.asList(
        "Hi there! I'm **%s**, a multipurpose Discord bot built for fun by %s!",
        "You can see what commands I have by using the `%s` command.",
        "",
        "I am currently running **av v%s**",
        "",
        "You can find all of my source code on github:",
        "https://github.com/av/av",
        "",
        "If you like me please vote for av to help me grow:",
        "https://discordbots.org/bot/av/vote"
    ));

    /**
     * Instantiates the event adapter and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public MessageEventAdapter(av av) {
        super(av);
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        if (!isValidMessage(event.getAuthor())) {
            return;
        }

        if (event.getChannelType().isGuild() && !event.getTextChannel().canTalk()) {
            return;
        }

        if (av.getBlacklist().isBlacklisted(event.getMessage())) {
            return;
        }

        loadDatabasePropertiesIntoMemory(event).thenAccept(databaseEventHolder -> {
            if (databaseEventHolder.getGuild() != null && databaseEventHolder.getPlayer() != null) {
                av.getLevelManager().rewardPlayer(event, databaseEventHolder.getGuild(), databaseEventHolder.getPlayer());
            }

            CommandContainer container = CommandHandler.getCommand(av, event.getMessage(), event.getMessage().getContentRaw());
            if (container != null && canExecuteCommand(event, container)) {
                invokeMiddlewareStack(new MiddlewareStack(event.getMessage(), container, databaseEventHolder));
                return;
            }

            if (isMentionableAction(event)) {
                container = CommandHandler.getLazyCommand(ArrayUtil.toArguments(event.getMessage().getContentRaw())[1]);
                if (container != null && canExecuteCommand(event, container)) {
                    invokeMiddlewareStack(new MiddlewareStack(event.getMessage(), container, databaseEventHolder, true));
                    return;
                }

                if (av.getIntelligenceManager().isEnabled()) {
                    if (isAIEnabledForChannel(event, databaseEventHolder.getGuild())) {
                        av.getIntelligenceManager().handleRequest(
                            event.getMessage(), databaseEventHolder
                        );
                    }
                    return;
                }
            }

            if (isSingleBotMention(event.getMessage().getContentRaw().trim())) {
                sendTagInformationMessage(event);
                return;
            }

            if (!event.getChannelType().isGuild()) {
                sendInformationMessage(event);
            }
        });
    }

    private boolean isValidMessage(User author) {
        return !author.isBot() || author.getIdLong() == DiscordConstants.SENITHER_BOT_ID;
    }

    private void invokeMiddlewareStack(MiddlewareStack stack) {
        commandService.submit(stack::next);
    }

    private boolean canExecuteCommand(MessageReceivedEvent event, CommandContainer container) {
        if (!container.getCommand().isAllowedInDM() && !event.getChannelType().isGuild()) {
            MessageFactory.makeWarning(event.getMessage(), ":warning: You can not use this command in direct messages!").queue();
            return false;
        }
        return true;
    }

    private boolean isMentionableAction(MessageReceivedEvent event) {
        if (!event.getMessage().isMentioned(av.getSelfUser())) {
            return false;
        }

        String[] args = event.getMessage().getContentRaw().split(" ");
        return args.length >= 2 &&
            userRegEX.matcher(args[0]).matches() &&
            event.getMessage().getMentionedUsers().get(0).getId().equals(av.getSelfUser().getId());

    }

    private boolean isSingleBotMention(String rawContent) {
        return rawContent.equals("<@" + av.getSelfUser().getId() + ">") ||
            rawContent.equals("<@!" + av.getSelfUser().getId() + ">");
    }

    private boolean isAIEnabledForChannel(MessageReceivedEvent event, GuildTransformer transformer) {
        if (transformer == null) {
            return true;
        }

        ChannelTransformer channel = transformer.getChannel(event.getChannel().getId());
        return channel == null || channel.getAI().isEnabled();
    }

    private void sendTagInformationMessage(MessageReceivedEvent event) {
        String author = "**Senither#0001**";
        if (event.getMessage().getChannelType().isGuild() && event.getGuild().getMemberById(88739639380172800L) != null) {
            author = "<@88739639380172800>";
        }

        MessageFactory.makeEmbeddedMessage(event.getMessage().getChannel(), Color.decode("#E91E63"), String.format(mentionMessage,
            av.getSelfUser().getName(),
            author,
            CommandHandler.getLazyCommand("help").getCommand().generateCommandTrigger(event.getMessage()),
            AppInfo.getAppInfo().version
        ))
            .setFooter("This message will be automatically deleted in one minute.")
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));
    }

    @SuppressWarnings("ConstantConditions")
    private void sendInformationMessage(MessageReceivedEvent event) {
        log.info("Private message received from user(ID: {}) that does not match any commands!",
            event.getAuthor().getId()
        );

        if (hasReceivedInfoMessageInTheLastMinute.contains(event.getAuthor().getIdLong())) {
            return;
        }

        hasReceivedInfoMessageInTheLastMinute.add(event.getAuthor().getIdLong());

        try {
            ArrayList<String> strings = new ArrayList<>();
            strings.addAll(Arrays.asList(
                "To invite me to your server, use this link:",
                "*:oauth*",
                "",
                "You can use `{0}help` to see a list of all the categories of commands.",
                "You can use `{0}help category` to see a list of commands for that category.",
                "For specific command help, use `{0}help command` (for example `{0}help {1}{2}`,\n`{0}help {2}` also works)"
            ));

            if (av.getIntelligenceManager().isEnabled()) {
                strings.add("\nYou can tag me in a message with <@:botId> to send me a message that I should process using my AI.");
            }

            strings.add("\n**Full list of commands**\n*https://avbot.com/commands*");
            strings.add("\nav Support Server:\n*https://avbot.com/support*");

            CommandContainer commandContainer = CommandHandler.getCommands().stream()
                .filter(container -> !container.getCategory().isGlobalOrSystem())
                .findAny()
                .get();

            MessageFactory.makeEmbeddedMessage(event.getMessage(), Color.decode("#E91E63"), I18n.format(
                String.join("\n", strings),
                CommandHandler.getCommand(HelpCommand.class).getCategory().getPrefix(event.getMessage()),
                commandContainer.getCategory().getPrefix(event.getMessage()),
                commandContainer.getTriggers().iterator().next()
            ))
                .set("oauth", av.getConfig().getString("discord.oauth"))
                .set("botId", av.getSelfUser().getId())
                .queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private CompletableFuture<DatabaseEventHolder> loadDatabasePropertiesIntoMemory(final MessageReceivedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            if (!event.getChannelType().isGuild()) {
                return new DatabaseEventHolder(null, null);
            }

            GuildTransformer guild = GuildController.fetchGuild(av, event.getMessage());

            if (guild == null || !guild.isLevels() || event.getAuthor().isBot()) {
                return new DatabaseEventHolder(guild, null);
            }
            return new DatabaseEventHolder(guild, PlayerController.fetchPlayer(av, event.getMessage()));
        });
    }

    public void onMessageDelete(TextChannel channel, List<String> messageIds) {
        Collection reactions = ReactionController.fetchReactions(av, channel.getGuild());
        if (reactions == null || reactions.isEmpty()) {
            return;
        }

        List<String> removedReactionMessageIds = new ArrayList<>();
        for (DataRow row : reactions) {
            for (String messageId : messageIds) {
                if (Objects.equals(row.getString("message_id"), messageId)) {
                    removedReactionMessageIds.add(messageId);
                }
            }
        }

        if (removedReactionMessageIds.isEmpty()) {
            return;
        }

        QueryBuilder builder = av.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME);
        for (String messageId : removedReactionMessageIds) {
            builder.orWhere("message_id", messageId);
        }

        try {
            builder.delete();

            ReactionController.forgetCache(
                channel.getGuild().getIdLong()
            );
        } catch (SQLException e) {
            log.error("Failed to delete {} reaction messages for the guild with an ID of {}",
                removedReactionMessageIds.size(), channel.getGuild().getId(), e
            );
        }
    }

    public void onMessageUpdate(MessageUpdateEvent event) {
        Collection reactions = ReactionController.fetchReactions(av, event.getGuild());
        if (reactions == null) {
            return;
        }

        if (reactions.where("message_id", event.getMessage().getId()).isEmpty()) {
            return;
        }

        try {
            String messageContent = event.getMessage().getContentStripped();
            if (messageContent.trim().length() == 0 && !event.getMessage().getEmbeds().isEmpty()) {
                messageContent = event.getMessage().getEmbeds().get(0).getDescription();
            }

            String finalMessageContent = messageContent;
            av.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
                .where("guild_id", event.getGuild().getId())
                .where("message_id", event.getMessage().getId())
                .update(statement -> {
                    statement.set("snippet", finalMessageContent.substring(
                        0, Math.min(finalMessageContent.length(), 64)
                    ), true);
                });

            ReactionController.forgetCache(event.getGuild().getIdLong());
        } catch (SQLException e) {
            log.error("Failed to update the reaction role message with a message ID of {}, error: {}",
                event.getMessage().getId(), e.getMessage(), e
            );
        }
    }
}
