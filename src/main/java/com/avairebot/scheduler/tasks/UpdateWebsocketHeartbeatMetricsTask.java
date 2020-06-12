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

package com.avbot.scheduler.tasks;

import com.avbot.av;
import com.avbot.contracts.scheduler.Task;
import com.avbot.metrics.Metrics;
import net.dv8tion.jda.core.JDA;

public class UpdateWebsocketHeartbeatMetricsTask implements Task {

    @Override
    public void handle(av av) {
        if (!av.areWeReadyYet()) {
            return;
        }

        for (JDA shard : av.getShardManager().getShards()) {
            Metrics.websocketHeartbeat.labels("Shard " + shard.getShardInfo().getShardId())
                .set(shard.getPing());
        }
    }
}
