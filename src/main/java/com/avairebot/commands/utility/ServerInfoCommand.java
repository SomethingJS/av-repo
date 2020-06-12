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

package com.avbot.commands.utility;

import com.avbot.av;
import com.avbot.chat.PlaceholderMessage;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.time.Carbon;
import com.avbot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerInfoCommand extends Command {

    public ServerInfoCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Server Info Command";
    }

    @Override
    public String getDescription() {
        return "Shows information about the server the command was ran in.";
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(ServerIdCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("serverinfo", "sinfo");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:channel,2,5");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        Guild guild = context.getGuild();
        Carbon time = Carbon.createFromOffsetDateTime(guild.getCreationTime());

        long bots = guild.getMembers().stream().filter(member -> member.getUser().isBot()).count();

        PlaceholderMessage placeholderMessage = context.makeEmbeddedMessage(getRoleColor(guild.getSelfMember().getRoles()),
            new MessageEmbed.Field(context.i18n("fields.id"), guild.getId(), true),
            new MessageEmbed.Field(context.i18n("fields.owner"), guild.getOwner().getUser().getName() + "#" + guild.getOwner().getUser().getDiscriminator(), true),
            new MessageEmbed.Field(context.i18n("fields.textChannels"), NumberUtil.formatNicely(guild.getTextChannels().size()), true),
            new MessageEmbed.Field(context.i18n("fields.voiceChannels"), NumberUtil.formatNicely(guild.getVoiceChannels().size()), true),
            new MessageEmbed.Field(context.i18n("fields.members"), NumberUtil.formatNicely(guild.getMembers().size()), true),
            new MessageEmbed.Field(context.i18n("fields.roles"), NumberUtil.formatNicely(guild.getRoles().size()), true),
            new MessageEmbed.Field(context.i18n("fields.users"), NumberUtil.formatNicely(guild.getMembers().size() - bots), true),
            new MessageEmbed.Field(context.i18n("fields.bots"), NumberUtil.formatNicely(bots), true),
            new MessageEmbed.Field(context.i18n("fields.region"), guild.getRegion().getName(), true),
            new MessageEmbed.Field(context.i18n("fields.emotes"), NumberUtil.formatNicely(guild.getEmotes().size()), true),
            new MessageEmbed.Field(context.i18n("fields.createdAt"), time.format(context.i18n("timeFormat")) + "\n*About " + shortenDiffForHumans(time) + "*", true)
        ).setTitle(guild.getName()).setThumbnail(guild.getIconUrl());

        placeholderMessage.requestedBy(context.getMember()).queue();
        return true;
    }

    private String shortenDiffForHumans(Carbon carbon) {
        String diff = carbon.diffForHumans();
        if (!diff.contains("and")) {
            return diff;
        }
        return diff.split("and")[0] + "ago";
    }

    private Color getRoleColor(List<Role> roles) {
        for (Role role : roles) {
            if (role.getColor() != null) return role.getColor();
        }
        return Color.decode("#E91E63");
    }
}
