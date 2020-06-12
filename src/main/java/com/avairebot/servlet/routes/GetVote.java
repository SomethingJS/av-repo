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

package com.avbot.servlet.routes;

import com.avbot.av;
import com.avbot.contracts.metrics.SparkRoute;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class GetVote extends SparkRoute {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String[] ids = request.params("ids").split(",");

        JSONObject root = new JSONObject();
        for (String id : ids) {
            try {
                root.put(id, av.getInstance().getVoteManager().hasVoted(Long.parseLong(id)));
            } catch (NumberFormatException e) {
                root.put(id, false);
            }
        }

        return root;
    }
}
