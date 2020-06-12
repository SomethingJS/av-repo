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

package com.avbot.commands.fun;

import com.avbot.av;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.CommandPriority;
import com.avbot.contracts.commands.Command;
import com.avbot.factories.RequestFactory;
import com.avbot.requests.Response;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import okhttp3.ResponseBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class MonikaCommand extends Command {

    public MonikaCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Just Monika";
    }

    @Override
    public String getDescription() {
        return "Just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika...";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("Just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika...");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("Just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika, just Monika...");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("justmonika", "monika");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.HIDDEN;
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        RequestFactory.makeGET("https://i.imgur.com/ZupgGkI.jpg")
            .send((Consumer<Response>) response -> {
                ResponseBody body = response.getResponse().body();

                if (body == null) {
                    return;
                }

                context.getChannel().sendFile(body.byteStream(),
                    "just-monika.jpg",
                    new MessageBuilder().setEmbed(
                        new EmbedBuilder()
                            .setImage("attachment://just-monika.jpg")
                            .setDescription("Just Monika")
                            .setFooter("Just Monika", null)
                            .build()
                    ).build()
                ).queue();
            });

        return true;
    }
}
