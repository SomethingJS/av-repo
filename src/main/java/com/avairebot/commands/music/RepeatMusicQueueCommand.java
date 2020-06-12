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
import com.avbot.Constants;
import com.avbot.audio.AudioHandler;
import com.avbot.audio.GuildMusicManager;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.utilities.RestActionUtil;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RepeatMusicQueueCommand extends Command {

    public RepeatMusicQueueCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Repeat Music Command";
    }

    @Override
    public String getDescription() {
        return "Repeats all the songs in the music queue or repeats the current song.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <repeat-state>` - Toggles between repeating all, one, or off.");
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command` - Displays current repeat mode",
            "`:command off` - Turns off repeat",
            "`:command one` - Loops the currently playing song",
            "`:command all` - Loops the entire queue"
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("repeatsongs", "repeatqueue", "loop");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,2,4",
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

        if (args.length == 0) {
            context.makeInfo(context.i18n("notes." + musicManager.getRepeatState().getName()))
                .setTitle(context.i18n("title", context.i18n("states." + musicManager.getRepeatState().getName())))
                .setFooter(context.i18n("footer", generateCommandTrigger(context.getMessage())))
                .queue();

            return true;
        }

        if (!musicManager.isReady() || musicManager.getPlayer().getPlayingTrack() == null) {
            return sendErrorMessage(context, context.i18n("error", generateCommandPrefix(context.getMessage())));
        }

        if (!musicManager.canPreformSpecialAction(this, context, "loop queue")) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "single":
            case "one":
            case "s":
            case "1":
                musicManager.setRepeatState(GuildMusicManager.RepeatState.SINGLE);
                if (musicManager.getScheduler().getAudioTrackContainer() != null) {
                    musicManager.getScheduler().getAudioTrackContainer()
                        .setMetadata(Constants.AUDIO_HAS_SENT_NOW_PLAYING_METADATA, true);
                }
                break;

            case "all":
            case "al":
            case "a":
                musicManager.setRepeatState(GuildMusicManager.RepeatState.ALL);
                break;

            case "off":
            case "of":
            case "o":
                musicManager.setRepeatState(GuildMusicManager.RepeatState.LOOPOFF);
                break;

            default:
                return sendErrorMessage(context, "errors.invalidProperty", "repeat-state", "repeat state");
        }

        context.makeSuccess(context.i18n("message"))
            .set("note", context.i18n("notes." + musicManager.getRepeatState().getName()))
            .set("state", context.i18n("states." + musicManager.getRepeatState().getName()))
            .queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        return true;
    }
}
