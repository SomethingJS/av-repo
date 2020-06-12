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
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.fun.RandomDogCommand;
import com.avbot.contracts.ai.Intent;

@SuppressWarnings("unused")
public class RequestDog extends Intent {

    public RequestDog(av av) {
        super(av);
    }

    @Override
    public String getAction() {
        return "request.dog";
    }

    @Override
    @SuppressWarnings({"SingleStatementInBlock", "ConstantConditions"})
    public void onIntent(CommandMessage context, AIResponse response) {
        CommandHandler.getCommand(RandomDogCommand.class)
            .getCommand().onCommand(new CommandMessage(context), new String[0]);
    }
}
