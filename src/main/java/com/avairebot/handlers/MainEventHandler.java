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

package com.avbot.handlers;

import com.avbot.av;
import com.avbot.contracts.handlers.EventHandler;
import com.avbot.database.controllers.PlayerController;
import com.avbot.handlers.adapter.*;
import com.avbot.metrics.Metrics;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.ResumedEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent;
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.emote.EmoteRemovedEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;
import net.dv8tion.jda.core.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateDiscriminatorEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent;

import java.util.Collections;

public class MainEventHandler extends EventHandler {

    private final RoleEventAdapter roleEvent;
    private final MemberEventAdapter memberEvent;
    private final ChannelEventAdapter channelEvent;
    private final MessageEventAdapter messageEvent;
    private final GuildStateEventAdapter guildStateEvent;
    private final JDAStateEventAdapter jdaStateEventAdapter;
    private final ChangelogEventAdapter changelogEventAdapter;
    private final ReactionEmoteEventAdapter reactionEmoteEventAdapter;

    /**
     * Instantiates the event handler and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public MainEventHandler(av av) {
        super(av);

        this.roleEvent = new RoleEventAdapter(av);
        this.memberEvent = new MemberEventAdapter(av);
        this.channelEvent = new ChannelEventAdapter(av);
        this.messageEvent = new MessageEventAdapter(av);
        this.guildStateEvent = new GuildStateEventAdapter(av);
        this.jdaStateEventAdapter = new JDAStateEventAdapter(av);
        this.changelogEventAdapter = new ChangelogEventAdapter(av);
        this.reactionEmoteEventAdapter = new ReactionEmoteEventAdapter(av);
    }

    @Override
    public void onGenericEvent(Event event) {
        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }

    @Override
    public void onReady(ReadyEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
    }

    @Override
    public void onResume(ResumedEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        jdaStateEventAdapter.onConnectToShard(event.getJDA());
    }

    @Override
    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        guildStateEvent.onGuildUpdateRegion(event);
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        guildStateEvent.onGuildUpdateName(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        guildStateEvent.onGuildJoin(event);
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        guildStateEvent.onGuildLeave(event);
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        channelEvent.onVoiceChannelDelete(event);
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        channelEvent.updateChannelData(event.getGuild());
        channelEvent.onTextChannelDelete(event);
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onTextChannelUpdateName(TextChannelUpdateNameEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onTextChannelUpdatePosition(TextChannelUpdatePositionEvent event) {
        channelEvent.updateChannelData(event.getGuild());
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!av.getSettings().isMusicOnlyMode()) {
            memberEvent.onGuildMemberJoin(event);
        }
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (!av.getSettings().isMusicOnlyMode()) {
            memberEvent.onGuildMemberLeave(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageReceived(event);
        }

        messageEvent.onMessageReceived(event);
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageDelete(event);
        }

        messageEvent.onMessageDelete(event.getChannel(), Collections.singletonList(event.getMessageId()));
    }

    @Override
    public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
        messageEvent.onMessageDelete(event.getChannel(), event.getMessageIds());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (changelogEventAdapter.isChangelogMessage(event.getChannel())) {
            changelogEventAdapter.onMessageUpdate(event);
        }

        messageEvent.onMessageUpdate(event);
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        roleEvent.updateRoleData(event.getGuild());
        roleEvent.onRoleUpdateName(event);
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        roleEvent.updateRoleData(event.getGuild());
        roleEvent.onRoleDelete(event);
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onRoleUpdatePosition(RoleUpdatePositionEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        roleEvent.updateRoleData(event.getGuild());
    }

    @Override
    public void onUserUpdateDiscriminator(UserUpdateDiscriminatorEvent event) {
        if (!av.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onUserUpdateAvatar(UserUpdateAvatarEvent event) {
        if (!av.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        if (!av.getSettings().isMusicOnlyMode()) {
            PlayerController.updateUserData(event.getUser());
        }
    }

    @Override
    public void onEmoteRemoved(EmoteRemovedEvent event) {
        reactionEmoteEventAdapter.onEmoteRemoved(event);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (isValidMessageReactionEvent(event)) {
            reactionEmoteEventAdapter.onMessageReactionAdd(event);
        }
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (isValidMessageReactionEvent(event)) {
            reactionEmoteEventAdapter.onMessageReactionRemove(event);
        }
    }

    private boolean isValidMessageReactionEvent(GenericMessageReactionEvent event) {
        return !event.getUser().isBot()
            && event.getGuild() != null
            && event.getReactionEmote().getEmote() != null;
    }
}
