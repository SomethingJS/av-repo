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

package com.avbot.contracts.database.collection;

import com.avbot.database.collection.Collection;
import com.avbot.database.collection.DataRow;

public interface CollectionEach {

    /**
     * This is called by by the {@link Collection#each(CollectionEach)}}
     * method, used to loops through every entity in the Collection and parses the key and
     * {@link com.avbot.database.collection.DataRow} object to the consumer.
     *
     * @param key   The key for the element
     * @param value The data row linked to the key
     */
    void forEach(int key, DataRow value);
}
