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

import com.avbot.av;
import com.avbot.contracts.handlers.EventAdapter;
import com.avbot.database.controllers.GuildController;
import com.avbot.database.transformers.ChannelTransformer;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.factories.MessageFactory;
import com.avbot.permissions.Permissions;
import com.avbot.utilities.StringReplacementUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class MemberEventAdapter extends EventAdapter {

    private static final Logger log = LoggerFactory.getLogger(MemberEventAdapter.class);

    /**
     * Instantiates the event adapter and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public MemberEventAdapter(av av) {
        super(av);
    }

    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(av, event.getGuild());
        if (transformer == null) {
            log.warn("Failed to get a valid guild transformer during member join! User:{}, Guild:{}",
                event.getMember().getUser().getId(), event.getGuild().getId()
            );
            return;
        }

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getWelcome().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                String message = StringReplacementUtil.parse(
                    event.getGuild(), textChannel, event.getUser(),
                    channelTransformer.getWelcome().getMessage() == null ?
                        "Welcome %user% to **%server%!**" :
                        channelTransformer.getWelcome().getMessage()
                );

                String embedColor = channelTransformer.getWelcome().getEmbedColor();
                if (embedColor == null) {
                    textChannel.sendMessage(message).queue();
                    continue;
                }

                textChannel.sendMessage(
                    MessageFactory.createEmbeddedBuilder()
                        .setDescription(message)
                        .setColor(Color.decode(embedColor))
                        .build()
                ).queue();
            }
        }

        // Re-mutes the user if a valid mute role have been setup for the guild
        // and the user is still registered as muted for the server.
        if (transformer.getMuteRole() != null) {
            Role mutedRole = event.getGuild().getRoleById(transformer.getMuteRole());
            if (canGiveRole(event, mutedRole) && av.getMuteManger().isMuted(event.getGuild().getIdLong(), event.getUser().getIdLong())) {
                event.getGuild().getController().addRolesToMember(
                    event.getMember(), mutedRole
                ).queue();
            }
        }

        if (event.getUser().isBot()) {
            return;
        }

        if (transformer.getAutorole() != null) {
            Role role = event.getGuild().getRoleById(transformer.getAutorole());
            if (canGiveRole(event, role)) {
                event.getGuild().getController().addSingleRoleToMember(
                    event.getMember(), role
                ).queue();
            }
        }
    }

    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(av, event.getGuild());
        if (transformer == null) {
            log.warn("Failed to get a valid guild transformer during member leave! User:{}, Guild:{}",
                event.getMember().getUser().getId(), event.getGuild().getId()
            );
            return;
        }

        for (ChannelTransformer channelTransformer : transformer.getChannels()) {
            if (channelTransformer.getGoodbye().isEnabled()) {
                TextChannel textChannel = event.getGuild().getTextChannelById(channelTransformer.getId());
                if (textChannel == null) {
                    continue;
                }

                if (!event.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)) {
                    continue;
                }

                String message = StringReplacementUtil.parse(
                    event.getGuild(), textChannel, event.getUser(),
                    channelTransformer.getGoodbye().getMessage() == null ?
                        "%user% has left **%server%**! :(" :
                        channelTransformer.getGoodbye().getMessage()
                );

                String embedColor = channelTransformer.getGoodbye().getEmbedColor();
                if (embedColor == null) {
                    textChannel.sendMessage(message).queue();
                    continue;
                }

                textChannel.sendMessage(
                    MessageFactory.createEmbeddedBuilder()
                        .setDescription(message)
                        .setColor(Color.decode(embedColor))
                        .build()
                ).queue();
            }
        }
    }

    private boolean canGiveRole(GuildMemberJoinEvent event, Role role) {
        return role != null
            && event.getGuild().getSelfMember().canInteract(role)
            && (event.getGuild().getSelfMember().hasPermission(Permissions.MANAGE_ROLES.getPermission())
            || event.getGuild().getSelfMember().hasPermission(Permissions.ADMINISTRATOR.getPermission()));
    }
}
