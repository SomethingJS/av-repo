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

import com.avbot.av;
import com.avbot.cache.CacheType;
import com.avbot.contracts.scheduler.Job;
import com.avbot.contracts.scheduler.Task;
import com.avbot.factories.RequestFactory;
import com.avbot.requests.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FetchMemeTypesJob extends Job {

    private final String cacheToken = "meme.types";
    private final String apiEndpoint = "https://memegen.link/api/templates/";

    public FetchMemeTypesJob(av av) {
        super(av, 7, 7, TimeUnit.DAYS);

        if (!av.getCache().getAdapter(CacheType.FILE).has(cacheToken)) {
            run();
        }
    }

    @Override
    public void run() {
        handleTask((Task) av -> {
            RequestFactory.makeGET(apiEndpoint)
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Content-Type", "application/json")
                .send((Consumer<Response>) response -> {
                    HashMap<String, String> memes = (HashMap<String, String>) response.toService(HashMap.class);
                    HashMap<String, HashMap<String, String>> cache = new HashMap<>();

                    for (Map.Entry<String, String> entry : memes.entrySet()) {
                        HashMap<String, String> meme = new HashMap<>();
                        meme.put("name", entry.getKey());
                        meme.put("url", entry.getValue());

                        cache.put(entry.getValue().substring(apiEndpoint.length(), entry.getValue().length()), meme);

                    }

                    av.getCache().getAdapter(CacheType.FILE).forever(cacheToken, cache);
                });
        });
    }
}
