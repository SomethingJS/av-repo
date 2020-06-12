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
import com.avbot.scheduler.tasks.ApplicationShutdownTask;
import com.avbot.scheduler.tasks.DrainReactionRoleQueueTask;
import com.avbot.scheduler.tasks.DrainVoteQueueTask;
import com.avbot.scheduler.tasks.DrainWeatherQueueTask;

import java.util.concurrent.TimeUnit;

public class RunEverySecondJob extends Job {

    private final DrainVoteQueueTask emptyVoteQueueTask = new DrainVoteQueueTask();
    private final ApplicationShutdownTask shutdownTask = new ApplicationShutdownTask();
    private final DrainWeatherQueueTask drainWeatherQueueTask = new DrainWeatherQueueTask();
    private final DrainReactionRoleQueueTask reactionRoleQueueTask = new DrainReactionRoleQueueTask();

    public RunEverySecondJob(av av) {
        super(av, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        handleTask(emptyVoteQueueTask, shutdownTask, drainWeatherQueueTask, reactionRoleQueueTask);
    }
}
