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

package com.avbot.database.controllers;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.database.collection.Collection;
import com.avbot.database.collection.DataRow;
import com.avbot.database.transformers.PlaylistTransformer;
import com.avbot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlaylistController {

    public static final Cache<Long, Collection> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    private static final Logger log = LoggerFactory.getLogger(PlaylistController.class);

    @CheckReturnValue
    public static Collection fetchPlaylists(av av, Message message) {
        if (!message.getChannelType().isGuild()) {
            return null;
        }

        return (Collection) CacheUtil.getUncheckedUnwrapped(cache, message.getGuild().getIdLong(), () -> {
            try {
                return av.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                    .selectAll().where("guild_id", message.getGuild().getId())
                    .get();
            } catch (Exception e) {
                log.error("Failed to fetch playlists for server " + message.getGuild().getId(), e);

                return Collection.EMPTY_COLLECTION;
            }
        });
    }

    @CheckReturnValue
    public static PlaylistTransformer fetchPlaylistFromName(av av, Message message, String name) {
        Collection playlists = fetchPlaylists(av, message);
        if (playlists == null || playlists.isEmpty()) {
            return null;
        }

        List<DataRow> playlist = playlists.whereLoose("name", name);
        if (playlist.isEmpty()) {
            return null;
        }

        return new PlaylistTransformer(playlist.get(0));
    }

    public static void forgetCache(long guildId) {
        cache.invalidate(guildId);
    }
}
