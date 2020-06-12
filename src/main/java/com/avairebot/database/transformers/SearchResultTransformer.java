/*
 * Copyright (c) 2019.
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

package com.avbot.database.transformers;

import com.avbot.av;
import com.avbot.audio.TrackRequestContext;
import com.avbot.audio.cache.AudioTrackSerializer;
import com.avbot.audio.searcher.SearchProvider;
import com.avbot.contracts.database.transformers.Transformer;
import com.avbot.database.collection.DataRow;
import com.avbot.exceptions.InvalidStateException;
import com.google.gson.reflect.TypeToken;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;

public class SearchResultTransformer extends Transformer {

    private SearchProvider provider;
    private String query;
    private SerializableAudioPlaylist serializableAudioPlaylist;

    /**
     * Creates a new search result transformer for
     * the given data row result.
     *
     * @param data The data row returned from a database query.
     */
    public SearchResultTransformer(DataRow data) {
        super(data);

        if (hasData()) {
            provider = SearchProvider.fromId(data.getInt("provider", -1));
            query = data.getString("query");
            serializableAudioPlaylist = av.gson.fromJson(
                data.getString("result"), new TypeToken<SerializableAudioPlaylist>() {
                }.getType()
            );

            if (serializableAudioPlaylist == null) {
                throw new InvalidStateException("The serializable audio playlist is null, this should not happen for cached results");
            }
        }
    }

    /**
     * Creates a new search result transformer for the given
     * track request context and audio playlist.
     *
     * @param context  The track request context that should be used
     *                 in the search result transformer.
     * @param playlist The Audio Playlist that should be used in
     *                 the search result transformer.
     */
    public SearchResultTransformer(TrackRequestContext context, AudioPlaylist playlist) {
        super(null);

        this.provider = context.getProvider();
        this.query = context.getQuery();
        this.serializableAudioPlaylist = new SerializableAudioPlaylist(playlist);
    }

    /**
     * Gets the search provider used as a cache key for the search result.
     *
     * @return The search provider used as a cache key for the search result.
     */
    public SearchProvider getProvider() {
        return provider;
    }

    /**
     * Gets the search query used as a cache key for the search result.
     *
     * @return The search query used as a cache key for the search result.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets the serializable audio playlist instance, this will contain all
     * the audio tracks contained in the result as a byte array,
     * as-well-as some information about the playlist like
     * it's name and search status.
     *
     * @return The serializable audio playlist instance.
     */
    public SerializableAudioPlaylist getSerializableAudioPlaylist() {
        return serializableAudioPlaylist;
    }

    /**
     * Gets the audio playlist instance, this will create a completely new audio playlist
     * instance from the {@link #getSerializableAudioPlaylist() serialized playlist}.
     *
     * @return The audio playlist instance stored in the cache.
     */
    public AudioPlaylist getAudioPlaylist() {
        return new BasicAudioPlaylist(
            serializableAudioPlaylist.name,
            AudioTrackSerializer.decodeTracks(serializableAudioPlaylist.tracks),
            AudioTrackSerializer.decodeTrack(serializableAudioPlaylist.selectedTrack),
            serializableAudioPlaylist.isSearchResult
        );
    }

    public static class SerializableAudioPlaylist {

        private String name;
        private boolean isSearchResult;
        private byte[] selectedTrack;
        private byte[][] tracks;

        public SerializableAudioPlaylist(AudioPlaylist playlist) {
            this.name = playlist.getName();
            this.isSearchResult = playlist.isSearchResult();
            this.selectedTrack = AudioTrackSerializer.encodeTrack(playlist.getSelectedTrack());
            this.tracks = AudioTrackSerializer.encodeTracks(playlist.getTracks());
        }

        public String getName() {
            return name;
        }

        public byte[] getSelectedTrack() {
            return selectedTrack;
        }

        public byte[][] getTracks() {
            return tracks;
        }

        public boolean isSearchResult() {
            return isSearchResult;
        }

        @Override
        public String toString() {
            return av.gson.toJson(this);
        }
    }
}
