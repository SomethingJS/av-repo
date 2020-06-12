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

package com.avbot.scheduler.jobs;

import com.avbot.AppInfo;
import com.avbot.av;
import com.avbot.contracts.scheduler.Job;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.SelfUser;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SyncStatsWithBeaconJob extends Job {

    private static final MediaType json = MediaType.parse("application/json; charset=utf-8");
    private static final Logger log = LoggerFactory.getLogger(SyncStatsWithBeaconJob.class);

    private final OkHttpClient client = new OkHttpClient();

    public SyncStatsWithBeaconJob(av av) {
        super(av, 5, 180, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        SelfUser selfUser = av.getSelfUser();
        if (selfUser == null) {
            return;
        }

        Request.Builder request = new Request.Builder()
            .addHeader("User-Agent", "av v" + AppInfo.getAppInfo().version)
            .url("https://beacon.avbot.com/v1/bot/" + selfUser.getId())
            .post(RequestBody.create(json, buildPayload(selfUser)));

        Response response = null;
        try {
            response = client.newCall(request.build()).execute();
        } catch (IOException e) {
            log.error("Failed sending sync with beacon request: " + e.getMessage());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private String buildPayload(SelfUser selfUser) {
        JSONObject main = new JSONObject();

        JSONObject bot = new JSONObject();
        bot.put("name", selfUser.getName());
        bot.put("avatar", selfUser.getAvatarId());
        main.put("bot", bot);

        JSONArray shards = new JSONArray();
        for (JDA shard : av.getShardManager().getShards()) {
            JSONObject shardObj = new JSONObject();

            shardObj.put("id", shard.getShardInfo().getShardId());
            shardObj.put("latency", shard.getPing());
            shardObj.put("users", shard.getUsers().size());
            shardObj.put("channels", getTotalChannels(shard));
            shardObj.put("guilds", shard.getGuilds().size());

            shards.put(shardObj);
        }
        main.put("shards", shards);

        return main.toString();
    }

    private int getTotalChannels(JDA jda) {
        return jda.getTextChannels().size()
            + jda.getVoiceChannels().size();
    }
}
