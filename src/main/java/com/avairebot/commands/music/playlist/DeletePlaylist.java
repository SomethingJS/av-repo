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

package com.avbot.commands.music.playlist;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.music.PlaylistCommand;
import com.avbot.contracts.commands.playlist.PlaylistSubCommand;
import com.avbot.database.controllers.PlaylistController;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.database.transformers.PlaylistTransformer;

import java.sql.SQLException;

public class DeletePlaylist extends PlaylistSubCommand {

    public DeletePlaylist(av av, PlaylistCommand command) {
        super(av, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        try {
            av.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("guild_id", context.getGuild().getId())
                .andWhere("id", playlist.getId())
                .delete();

            PlaylistController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess(context.i18n("playlistDeleted"))
                .set("name", playlist.getName())
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            context.makeError("Error: " + e.getMessage()).queue();
        }

        return false;
    }
}
