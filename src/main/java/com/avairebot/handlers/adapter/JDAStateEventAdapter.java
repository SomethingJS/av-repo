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
import com.avbot.audio.VoiceConnectStatus;
import com.avbot.audio.cache.AudioCache;
import com.avbot.audio.cache.AudioState;
import com.avbot.audio.cache.AudioTrackSerializer;
import com.avbot.cache.CacheType;
import com.avbot.chat.MessageType;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.music.PlayCommand;
import com.avbot.contracts.handlers.EventAdapter;
import com.avbot.database.collection.DataRow;
import com.avbot.database.controllers.GuildController;
import com.avbot.factories.MessageFactory;
import com.avbot.handlers.DatabaseEventHolder;
import com.avbot.language.I18n;
import com.avbot.time.Carbon;
import com.avbot.utilities.RoleUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.reflect.TypeToken;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JDAStateEventAdapter extends EventAdapter {

    public static final Cache<Long, Long> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(3, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(JDAStateEventAdapter.class);

    /**
     * Instantiates the event adapter and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public JDAStateEventAdapter(av av) {
        super(av);
    }

    public void onConnectToShard(JDA jda) {
        handleAutoroleTask(jda);
        handleReconnectMusic(jda);
    }

    private void handleReconnectMusic(JDA jda) {
        log.debug("Connection to shard {} has been established, running reconnect music job to reconnect music to channels that were connected during shutdown",
            jda.getShardInfo().getShardId()
        );

        int connectedChannels = 0;
        for (AudioState state : getAudioStates()) {
            if (state == null) {
                continue;
            }

            Guild guild = jda.getGuildById(state.getGuildId());
            if (guild == null) {
                continue;
            }

            VoiceChannel voiceChannel = guild.getVoiceChannelById(state.getVoiceChannelId());
            if (voiceChannel == null) {
                continue;
            }

            long usersInVoiceChannel = voiceChannel.getMembers().stream()
                .filter(member -> !member.getUser().isBot()).count();

            if (usersInVoiceChannel == 0) {
                continue;
            }

            TextChannel textChannel = guild.getTextChannelById(state.getMessageChannelId());
            if (textChannel == null) {
                continue;
            }

            connectedChannels++;
            textChannel.sendMessage(MessageFactory.createEmbeddedBuilder()
                .setDescription(I18n.getString(guild, "music.internal.resumeMusic"))
                .build()).queue(message -> {

                VoiceConnectStatus voiceConnectStatus = AudioHandler.getDefaultAudioHandler().connectToVoiceChannel(
                    message, voiceChannel, guild.getAudioManager()
                );

                if (!voiceConnectStatus.isSuccess()) {
                    message.editMessage(MessageFactory.createEmbeddedBuilder()
                        .setColor(MessageType.WARNING.getColor())
                        .setDescription(voiceConnectStatus.getErrorMessage())
                        .build()
                    ).queue();
                    return;
                }

                List<AudioCache> audioStateTacks = new ArrayList<>();
                audioStateTacks.add(state.getPlayingTrack());
                audioStateTacks.addAll(state.getQueue());

                GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(guild);
                musicManager.setLastActiveMessage(new CommandMessage(
                    CommandHandler.getCommand(PlayCommand.class),
                    new DatabaseEventHolder(GuildController.fetchGuild(av, guild), null),
                    message, false, new String[0]
                ));

                for (AudioCache audioCache : audioStateTacks) {
                    if (audioCache == null) {
                        continue;
                    }

                    Member member = message.getGuild().getMemberById(audioCache.getRequestedBy());
                    if (member == null) {
                        continue;
                    }

                    AudioTrack track = AudioTrackSerializer.decodeTrack(audioCache.getTrack());
                    if (track == null) {
                        continue;
                    }

                    musicManager.getScheduler().queue(track, member.getUser());
                }
            });

            AudioTrack track = AudioTrackSerializer.decodeTrack(state.getPlayingTrack() != null
                ? state.getPlayingTrack().getTrack()
                : null
            );

            log.debug("{} stopped playing {} with {} songs in the queue",
                guild.getId(), track == null ? "Unknown Track" : track.getInfo().uri, state.getQueue().size()
            );
        }

        log.debug("Shard {} successfully reconnected {} music channels",
            jda.getShardInfo().getShardId(), connectedChannels
        );
    }

    private List<AudioState> getAudioStates() {
        Object rawAudioState = av.getCache().getAdapter(CacheType.FILE).get("audio.state");
        if (rawAudioState == null) {
            return new ArrayList<>();
        }

        return av.gson.fromJson(
            String.valueOf(rawAudioState),
            new TypeToken<List<AudioState>>() {
            }.getType()
        );
    }

    private void handleAutoroleTask(JDA jda) {
        log.debug("Connection to shard {} has been established, running autorole job to sync autoroles missed due to downtime",
            jda.getShardInfo().getShardId()
        );

        if (cache.asMap().isEmpty()) {
            populateAutoroleCache();
        }

        int updatedUsers = 0;
        long thirtyMinutesAgo = Carbon.now().subMinutes(30).getTimestamp();

        for (Guild guild : jda.getGuilds()) {
            if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                continue;
            }

            Long autoroleId = cache.getIfPresent(guild.getIdLong());
            if (autoroleId == null) {
                continue;
            }

            Role autorole = guild.getRoleById(autoroleId);
            if (autorole == null) {
                continue;
            }

            for (Member member : guild.getMembers()) {
                if (member.getJoinDate().toEpochSecond() > thirtyMinutesAgo) {
                    if (!RoleUtil.hasRole(member, autorole)) {
                        updatedUsers++;
                        guild.getController().addSingleRoleToMember(member, autorole)
                            .queue();
                    }
                }
            }
        }

        log.debug("Shard {} successfully synced {} new users autorole",
            jda.getShardInfo().getShardId(), updatedUsers
        );
    }

    private void populateAutoroleCache() {
        log.debug("No cache entries was found, populating the auto role cache");
        try {
            for (DataRow row : av.getDatabase().query(String.format(
                "SELECT `id`, `autorole` FROM `%s` WHERE `autorole` IS NOT NULL;", Constants.GUILD_TABLE_NAME
            ))) {
                cache.put(row.getLong("id"), row.getLong("autorole"));
            }
        } catch (SQLException e) {
            log.error("Failed to populate the autorole cache: {}", e.getMessage(), e);
        }
    }
}
