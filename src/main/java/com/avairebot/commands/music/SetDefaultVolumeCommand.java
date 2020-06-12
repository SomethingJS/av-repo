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
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandContext;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.shared.DiscordConstants;
import com.avbot.utilities.NumberUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetDefaultVolumeCommand extends Command {

    public SetDefaultVolumeCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Set Default Volume Command";
    }

    @Override
    public String getDescription(@Nullable CommandContext context) {
        String prefix = context != null && context.isGuildMessage()
            ? generateCommandPrefix(context.getMessage())
            : DiscordConstants.DEFAULT_COMMAND_PREFIX;

        return String.format(String.join("\n", Arrays.asList(
            "Sets the default volume that the music should play at when Ava first joins a voice channel.",
            "**Note:** This does not change the volume of music already playing, to change that, use the `%svolume` command instead."
        )), prefix);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("default-volume", "set-volume");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current default volume",
            "`:command <volume>` - Changes the default volume to the given volume."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList(
            "`:command 75` - Sets the default volume to 75"
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "hasDJLevel:normal",
            "throttle:guild,1,4",
            "hasVoted",
            "musicChannel"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "default volume");
        }

        if (args.length == 0) {
            return sendCurrentVolume(context, transformer);
        }

        int vol = NumberUtil.parseInt(args[0], 0);
        if (vol < 1 || vol > 100) {
            return sendErrorMessage(context, context.i18n("mustBeNumber"));
        }

        transformer.setDefaultVolume(vol);

        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> statement.set("default_volume", vol));

            context.makeSuccess(context.i18n("changedVolume"))
                .set("volume", vol)
                .queue();

            return true;
        } catch (SQLException e) {
            av.getLogger().error("Failed to store the default volume in the database due to a SQLException: ", e);
            context.makeError(context.i18n("failedToSave", e.getMessage())).queue();
        }

        return false;
    }

    private boolean sendCurrentVolume(CommandMessage context, GuildTransformer transformer) {
        context.makeSuccess(context.i18n("currentVolume"))
            .set("volume", transformer.getDefaultVolume())
            .queue();

        return false;
    }
}
