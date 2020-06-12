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

package com.avbot.commands.fun;

import com.avbot.av;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.factories.RequestFactory;
import com.avbot.requests.Response;
import com.avbot.requests.service.RandomBirdService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class RandomBirdCommand extends Command {

    public RandomBirdCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Random Bird Command";
    }

    @Override
    public String getDescription() {
        return "I will scour the internet to find a random bird picture for you.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Gets a random picture of a bird and sends it in the channel.");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(RandomDogCommand.class, RandomCatCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("randombird", "bird", "birb", "randombirb");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList("throttle:user,2,5");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("https://some-random-api.ml/img/birb").send((Consumer<Response>) response -> {
            RandomBirdService service = (RandomBirdService) response.toService(RandomBirdService.class);

            context.makeEmbeddedMessage()
                .setImage(service.getLink())
                .queue();
        });
        return true;
    }
}
