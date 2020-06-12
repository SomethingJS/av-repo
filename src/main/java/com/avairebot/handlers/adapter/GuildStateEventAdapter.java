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
import com.avbot.Constants;
import com.avbot.audio.AudioHandler;
import com.avbot.audio.GuildMusicManager;
import com.avbot.audio.LavalinkManager;
import com.avbot.chat.ConsoleColor;
import com.avbot.contracts.handlers.EventAdapter;
import com.avbot.metrics.Metrics;
import com.avbot.scheduler.ScheduleHandler;
import com.avbot.scheduler.tasks.MusicActivityTask;
import com.avbot.utilities.NumberUtil;
import com.avbot.utilities.RestActionUtil;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateRegionEvent;

import java.awt.*;
import java.sql.SQLException;
import java.time.Instant;

public class GuildStateEventAdapter extends EventAdapter {

    /**
     * Instantiates the event adapter and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public GuildStateEventAdapter(av av) {
        super(av);
    }

    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("name", event.getGuild().getName(), true));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onGuildUpdateRegion(GuildUpdateRegionEvent event) {
        Metrics.geoTracker.labels(event.getOldRegion().getName()).dec();
        Metrics.geoTracker.labels(event.getNewRegion().getName()).inc();
    }

    public void onGuildJoin(GuildJoinEvent event) {
        av.getLogger().info(ConsoleColor.format(
            "%greenJoined guild with an ID of " + event.getGuild().getId() + " called: " + event.getGuild().getName() + "%reset"
        ));

        if (!av.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.inc();
        Metrics.geoTracker.labels(event.getGuild().getRegion().getName()).inc();

        TextChannel channel = av.getShardManager().getTextChannelById(
            av.getConstants().getActivityLogChannelId()
        );

        if (channel == null) {
            return;
        }

        User owner = event.getGuild().getOwner().getUser();

        double guildMembers = event.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot()).count();
        double guildBots = event.getGuild().getMembers().stream().filter(member -> member.getUser().isBot()).count();
        double percentage = (guildBots / (guildBots + guildMembers)) * 100;

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#66BB6A"))
                .setTimestamp(Instant.now())
                .setFooter(String.format("%s Users, and %s Bots, %s Bots",
                    NumberUtil.formatNicely(guildMembers),
                    NumberUtil.formatNicely(guildBots),
                    NumberUtil.formatNicelyWithDecimals(percentage) + "%"
                ), null)
                .addField("Added", String.format("%s (ID: %s)",
                    event.getGuild().getName(), event.getGuild().getId()
                ), false)
                .addField("Owner", String.format("%s#%s (ID: %s)",
                    owner.getName(), owner.getDiscriminator(), owner.getId()
                ), false)
                .build()
        ).queue(null, RestActionUtil.ignore);
    }

    public void onGuildLeave(GuildLeaveEvent event) {
        handleSendGuildLeaveWebhook(event.getGuild());
        handleAudioConnectionOnGuildLeave(event.getGuild());
    }

    private void handleSendGuildLeaveWebhook(Guild guild) {
        av.getLogger().info(ConsoleColor.format(
            "%redLeft guild with an ID of " + guild.getId() + " called: " + guild.getName() + "%reset"
        ));

        if (!av.areWeReadyYet()) {
            return;
        }

        Metrics.guilds.dec();
        Metrics.geoTracker.labels(guild.getRegion().getName()).dec();

        TextChannel channel = av.getShardManager().getTextChannelById(
            av.getConstants().getActivityLogChannelId()
        );

        if (channel == null) {
            return;
        }

        channel.sendMessage(
            new EmbedBuilder()
                .setColor(Color.decode("#EF5350"))
                .setTimestamp(Instant.now())
                .addField("Removed", String.format("%s (ID: %s)",
                    guild.getName(), guild.getId()
                ), false)
                .build()
        ).queue(null, RestActionUtil.ignore);
    }

    private void handleAudioConnectionOnGuildLeave(Guild guild) {
        long guildId = guild.getIdLong();

        ScheduleHandler.getScheduler().submit(() -> {
            GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler()
                .musicManagers.remove(guildId);

            if (musicManager == null) {
                return;
            }

            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().getQueue().clear();

            MusicActivityTask.missingListener.remove(guildId);
            MusicActivityTask.playerPaused.remove(guildId);
            MusicActivityTask.emptyQueue.remove(guildId);

            musicManager.getScheduler().nextTrack(false);

            if (LavalinkManager.LavalinkManagerHolder.lavalink.isEnabled()) {
                JdaLink link = LavalinkManager.LavalinkManagerHolder.lavalink.getLavalink()
                    .getExistingLink(String.valueOf(guildId));


                if (link != null && !LavalinkManager.LavalinkManagerHolder.lavalink.isLinkBeingDestroyed(link)) {
                    link.destroy();
                }
            }
        });
    }
}
