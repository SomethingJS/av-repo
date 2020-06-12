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

public class ClearQueueCommand extends Command {

    public ClearQueueCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Clear Music Queue Command";
    }

    @Override
    public String getDescription() {
        return "Clears the music queue of all pending songs";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Clears the music queue");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("clearqueue", "cqueue", "flushqueue");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:user,1,5",
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

        if (!musicManager.canPreformSpecialAction(this, context, "clear queue")) {
            return false;
        }

        if (musicManager.getScheduler().getQueue().isEmpty()) {
            context.makeWarning(context.i18n("emptyQueue"))
                .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

            return false;
        }

        context.makeSuccess(context.i18n("success"))
            .set("queueSize", NumberUtil.formatNicely(
                musicManager.getScheduler().getQueue().size()
            ))
            .queue(message -> message.delete().queueAfter(1, TimeUnit.MINUTES, null, RestActionUtil.ignore));

        musicManager.getScheduler().getQueue().clear();

        return true;
    }
}
