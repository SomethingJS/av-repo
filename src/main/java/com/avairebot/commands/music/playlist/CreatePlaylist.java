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
import com.avbot.database.collection.Collection;
import com.avbot.database.collection.DataRow;
import com.avbot.database.connections.SQLite;
import com.avbot.database.controllers.PlaylistController;
import com.avbot.database.query.ChangeableStatement;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.utilities.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreatePlaylist extends PlaylistSubCommand {

    private static final Logger log = LoggerFactory.getLogger(CreatePlaylist.class);

    public CreatePlaylist(av av, PlaylistCommand command) {
        super(av, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, Collection playlists) {
        String name = args[0].trim().split(" ")[0];
        List<DataRow> playlistItems = playlists.whereLoose("name", name);
        if (!playlistItems.isEmpty()) {
            context.makeWarning(context.i18n("alreadyExists"))
                .set("playlist", name).queue();
            return false;
        }

        if (NumberUtil.isNumeric(name)) {
            context.makeWarning(context.i18n("onlyNumbersInName"))
                .queue();
            return false;
        }

        int playlistLimit = guild.getType().getLimits().getPlaylist().getPlaylists();
        if (playlists.size() >= playlistLimit) {
            context.makeWarning(context.i18n("noMorePlaylistSlots")).queue();
            return false;
        }

        try {
            storeInDatabase(context, name);

            context.makeSuccess(context.i18n("playlistCreated"))
                .set("playlist", name)
                .set("command", command.generateCommandTrigger(context.getMessage()))
                .queue();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            context.makeError("Error: " + e.getMessage()).queue();
        }

        return false;
    }

    private void storeInDatabase(CommandMessage context, String name) throws SQLException {
        av.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
            .insert(statement -> {
                addIncrementingIdWhenUsingSQLite(statement);

                statement.set("guild_id", context.getGuild().getId());
                statement.set("name", name, true);
                statement.set("amount", 0);
                statement.set("songs", av.gson.toJson(new ArrayList<>()));
            });

        PlaylistController.forgetCache(context.getGuild().getIdLong());
    }

    private void addIncrementingIdWhenUsingSQLite(ChangeableStatement statement) {
        try {
            if (!(av.getDatabase().getConnection() instanceof SQLite)) {
                return;
            }

            DataRow row = av.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .orderBy("created_at", "desc")
                .take(1)
                .get()
                .first();

            statement.set("id", row == null ? 1 : row.getLong("id") + 1);
        } catch (SQLException e) {
            log.error("Failed to generate the playlist ID, error: {}", e.getMessage(), e);
        }
    }
}
