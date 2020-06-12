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

package com.avbot.scheduler.jobs.generic;

import com.avbot.av;
import com.avbot.contracts.scheduler.Job;
import com.avbot.scheduler.tasks.MusicActivityTask;
import com.avbot.scheduler.tasks.SyncJDAMetricsCounterTask;

import java.util.concurrent.TimeUnit;

public class RunEveryThirtySecondsJob extends Job {

    private final MusicActivityTask musicActivityTask = new MusicActivityTask();
    private final SyncJDAMetricsCounterTask syncGuildMetricsCounterTask = new SyncJDAMetricsCounterTask();

    public RunEveryThirtySecondsJob(av av) {
        super(av, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        handleTask(
            musicActivityTask,
            syncGuildMetricsCounterTask
        );
    }
}
