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

package com.avbot.commands.music;

import com.avbot.av;
import com.avbot.audio.AudioHandler;
import com.avbot.audio.AudioTrackContainer;
import com.avbot.audio.GuildMusicManager;
import com.avbot.audio.TrackScheduler;
import com.avbot.chat.PlaceholderMessage;
import com.avbot.chat.SimplePaginator;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.language.I18n;
import com.avbot.utilities.NumberUtil;
import com.avbot.utilities.RestActionUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SongCommand extends Command {

    public SongCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Music Song Command";
    }

    @Override
    public String getDescription() {
        return "Returns the song that is playing right now and some attached information. This includes who requested it, how much of the song is left and the volume the song is playing at plus the rest of the songs currently in queue.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows info about the song currently playing and the queue.",
            "`:command [page]` - Shows the songs in the given page in the queue."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("song", "songs", "queue", "np");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:none",
            "throttle:channel,2,4",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_QUEUE);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        if (args.length > 0 && NumberUtil.isNumeric(args[0])) {
            if (musicManager.getScheduler().getQueue().isEmpty()) {
                return sendSongWithSixSongs(context, musicManager);
            }

            SimplePaginator<AudioTrackContainer> paginator = new SimplePaginator<>(
                musicManager.getScheduler().getQueue().iterator(), 10, NumberUtil.parseInt(args[0])
            );

            List<String> messages = new ArrayList<>();
            paginator.forEach((index, key, track) -> {
                messages.add(context.i18n("formats.line",
                    NumberUtil.parseInt(key.toString()) + 1,
                    track.getAudioTrack().getInfo().title,
                    track.getAudioTrack().getInfo().uri
                ));
            });

            context.makeSuccess(String.format("%s\n\n%s",
                String.join("\n", messages),
                paginator.generateFooter(context.getGuild(), generateCommandTrigger(context.getMessage()))
            )).setTitle(context.i18n("songsInQueue"))
                .queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES, null, RestActionUtil.ignore));

            return true;
        }

        return sendSongWithSixSongs(context, musicManager);
    }

    private boolean sendSongWithSixSongs(CommandMessage context, GuildMusicManager musicManager) {
        PlaceholderMessage queueMessage = context.makeSuccess(
            buildTrackDescription(context, musicManager.getPlayer(), musicManager.getScheduler())
        )
            .setTitle(musicManager.getPlayer().isPaused() ? context.i18n("paused") : context.i18n("playing"))
            .addField(
                context.i18n("songsInQueue") + " - " + buildQueueLength(musicManager),
                buildSongsInQueue(context, musicManager.getScheduler()),
                false
            );

        if (!musicManager.getScheduler().getQueue().isEmpty()) {
            queueMessage.setFooter(context.i18n("moreSongs", generateCommandTrigger(context.getMessage())));
        }

        queueMessage.queue(message -> message.delete().queueAfter(3, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }

    private String buildTrackDescription(CommandMessage context, IPlayer player, TrackScheduler scheduler) {
        String message = context.i18n("formats.song");

        if (player.getPlayingTrack().getInfo().isStream) {
            message = context.i18n("formats.stream");
        }

        String songTitle = player.getPlayingTrack().getInfo().title;
        if (songTitle == null || songTitle.equalsIgnoreCase("Unknown Title")) {
            songTitle = player.getPlayingTrack().getInfo().uri;
        }

        AudioTrackContainer trackContainer = scheduler.getAudioTrackContainer();
        if (trackContainer == null) {
            return "Track is still loading... Please wait one moment.";
        }

        return I18n.format(message,
            songTitle,
            player.getPlayingTrack().getInfo().uri,
            player.getVolume() + "%",
            trackContainer.getFormattedTotalTimeLeft(player),
            trackContainer.getRequester().getId()
        );
    }

    private String buildSongsInQueue(CommandMessage context, TrackScheduler scheduler) {
        if (scheduler.getQueue().isEmpty()) {
            return context.i18n("noSongs");
        }

        int number = 1;
        String songs = "";

        Iterator<AudioTrackContainer> iterator = scheduler.getQueue().iterator();
        while (iterator.hasNext() && number <= 6) {
            AudioTrackContainer next = iterator.next();

            songs += context.i18n("formats.line",
                number++,
                next.getAudioTrack().getInfo().title,
                next.getAudioTrack().getInfo().uri
            ) + "\n";
        }

        if (scheduler.getQueue().size() > 6) {
            songs += context.i18n("andXMoreSongs",
                NumberUtil.formatNicely(scheduler.getQueue().size() - 6),
                scheduler.getQueue().size() == 7 ? "" : 's'
            );
        }

        return songs;
    }

    private String buildQueueLength(GuildMusicManager manager) {
        long length = 0L;
        for (AudioTrackContainer container : manager.getScheduler().getQueue()) {
            if (container.getAudioTrack().getInfo().isStream) {
                continue;
            }
            length += container.getAudioTrack().getDuration() / 1000L;
        }

        AudioTrack playingTrack = manager.getPlayer().getPlayingTrack();
        if (playingTrack != null && !playingTrack.getInfo().isStream) {
            length += (playingTrack.getDuration() - playingTrack.getPosition()) / 1000L;
        }

        int seconds = (int) (length % 60L);
        int minutes = (int) ((length % 3600L) / 60L);
        int hours = (int) (length / 3600L);

        if (hours != 0) {
            return hours + " hours, " + minutes + " minutes, and " + seconds + " seconds";
        } else if (minutes != 0) {
            return minutes + " minutes, and " + seconds + " seconds";
        }
        return seconds + " seconds";
    }
}
