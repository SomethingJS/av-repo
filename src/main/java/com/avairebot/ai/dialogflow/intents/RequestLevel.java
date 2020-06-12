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

package com.avbot.ai.dialogflow.intents;

import ai.api.model.AIResponse;
import com.avbot.av;
import com.avbot.commands.CommandContainer;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.utility.RankCommand;
import com.avbot.contracts.ai.Intent;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.factories.MessageFactory;

@SuppressWarnings("unused")
public class RequestLevel extends Intent {

    public RequestLevel(av av) {
        super(av);
    }

    @Override
    public String getAction() {
        return "request.level";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onIntent(CommandMessage context, AIResponse response) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            MessageFactory.makeWarning(context.getMessage(),
                "This server doesn't have the `Levels & Experience` feature enabled so I can't tell you what level you are :("
            ).queue();
            return;
        }

        CommandContainer container = CommandHandler.getCommand(RankCommand.class);
        container.getCommand().onCommand(
            new CommandMessage(container, context.getDatabaseEventHolder(), context.getMessage()), new String[]{"---skip-mentions"}
        );
    }
}
