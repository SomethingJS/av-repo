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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GithubChangesJob extends Job {

    private final String cacheToken = "github.commits";

    public GithubChangesJob(av av) {
        super(av, 90, 90, TimeUnit.MINUTES);

        if (!av.getCache().getAdapter(CacheType.FILE).has(cacheToken)) {
            run();
        }
    }

    @Override
    public void run() {
        handleTask((Task) av -> {
            RequestFactory.makeGET("https://api.github.com/repos/av/av/commits")
                .send((Consumer<Response>) response -> {
                    List service = (List) response.toService(List.class);

                    av.getCache().getAdapter(CacheType.FILE).forever(cacheToken, service);
                });
        });
    }
}
