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

package com.avbot.middleware.global;

import com.avbot.av;
import com.avbot.contracts.middleware.Middleware;
import com.avbot.metrics.Metrics;
import com.avbot.middleware.MiddlewareStack;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;

public class IncrementMetricsForCommand extends Middleware {

    public IncrementMetricsForCommand(av av) {
        super(av);
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        Metrics.commandsReceived.labels(stack.getCommand().getClass().getSimpleName()).inc();

        return stack.next();
    }
}
