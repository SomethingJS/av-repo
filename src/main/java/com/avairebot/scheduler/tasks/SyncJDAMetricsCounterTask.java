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
import net.dv8tion.jda.core.Region;
import net.dv8tion.jda.core.entities.Guild;

import java.lang.management.ManagementFactory;

public class SyncJDAMetricsCounterTask implements Task {

    @Override
    public void handle(av av) {
        Metrics.uptime.labels("dynamic").set(ManagementFactory.getRuntimeMXBean().getUptime());

        Metrics.memoryTotal.set(Runtime.getRuntime().totalMemory());
        Metrics.memoryUsed.set(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

        if (!av.areWeReadyYet() || !hasLoadedGuilds(av)) {
            return;
        }

        Metrics.guilds.set(av.getShardEntityCounter().getGuilds());
        Metrics.users.set(av.getShardEntityCounter().getUsers());
        Metrics.channels.labels("text").set(av.getShardEntityCounter().getTextChannels());
        Metrics.channels.labels("voice").set(av.getShardEntityCounter().getVoiceChannels());

        for (Region region : Region.values()) {
            Metrics.geoTracker.labels(region.getName()).set(0);
        }

        for (JDA shard : av.getShardManager().getShards()) {
            for (Guild guild : shard.getGuilds()) {
                Metrics.geoTracker.labels(guild.getRegion().getName()).inc();
            }
        }
    }

    private boolean hasLoadedGuilds(av av) {
        if (av.getSettings().getShardCount() != -1
            && av.getShardManager().getShards().size() != av.getSettings().getShardCount()) {
            return false;
        }

        for (JDA shard : av.getShardManager().getShards()) {
            if (shard.getGuildCache().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
