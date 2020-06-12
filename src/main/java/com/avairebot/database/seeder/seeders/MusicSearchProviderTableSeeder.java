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

package com.avbot.database.seeder.seeders;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.audio.searcher.SearchProvider;
import com.avbot.contracts.database.seeder.Seeder;

import java.sql.SQLException;

public class MusicSearchProviderTableSeeder extends Seeder {

    public MusicSearchProviderTableSeeder(av av) {
        super(av);
    }

    @Override
    public String table() {
        return Constants.MUSIC_SEARCH_PROVIDERS_TABLE_NAME;
    }

    @Override
    public void run() throws SQLException {
        for (SearchProvider provider : SearchProvider.values()) {
            if (!tableHasValue("id", provider.getId())) {
                createQuery().insert(statement -> {
                    statement.set("id", provider.getId());
                    statement.set("name", provider);
                });
            }
        }
    }
}
