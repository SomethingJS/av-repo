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
import com.avbot.audio.DJGuildLevel;
import com.avbot.audio.GuildMusicManager;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.utilities.NumberUtil;
import com.avbot.utilities.RestActionUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VolumeCommand extends Command {

    public VolumeCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Music Volume Command";
    }

    @Override
    public String getDescription() {
        return "Changes the volume of the music, by default the music will be playing at 100% volume.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Shows the current music volume without changing it",
            "`:command <volume>` - Sets the music volume to the given number"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 80`");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("volume", "vol");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "throttle:user,1,4",
            "hasVoted",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_TRACK_MODIFIER);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildMusicManager musicManager = AudioHandler.getDefaultAudioHandler().getGuildAudioPlayer(context.getGuild());

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        int volume = musicManager.getPlayer().getVolume();

        if (args.length == 0) {
            context.makeSuccess(context.i18n("nowPlaying") + "\n:bar")
                .set("volume", volume)
                .set("bar", getVolumeString(volume, 21))
                .queue();
            return true;
        }

        if (!musicManager.canPreformSpecialAction(this, context, "change volume")) {
            return false;
        }

        if (!AudioHandler.getDefaultAudioHandler().canRunDJAction(av, context.getMessage(), DJGuildLevel.NORMAL)) {
            return sendErrorMessage(context, context.i18n("requireDJRole"));
        }

        if (!NumberUtil.isNumeric(args[0])) {
            return sendErrorMessage(context, context.i18n("invalidVolume"));
        }

        int newVolume = NumberUtil.parseInt(args[0], -1);
        if ((newVolume < 0 || newVolume > 100)) {
            return sendErrorMessage(context, context.i18n("invalidVolume"));
        }

        musicManager.getPlayer().setVolume(newVolume);
        context.makeSuccess(context.i18n("setTo") + "\n:bar")
            .set("volume", newVolume)
            .set("bar", getVolumeString(newVolume, 18))
            .queue(message -> message.delete().queueAfter(2, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }

    private String getVolumeString(int volume, int multiplier) {
        StringBuilder volumeString = new StringBuilder();
        for (int i = 1; i <= multiplier; i++) {
            volumeString.append((i - 1) * (100 / multiplier) < volume ? "\u2592" : "\u2591");
        }
        return volumeString.toString();
    }
}
