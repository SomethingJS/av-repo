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
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.ai.Intent;
import com.avbot.utilities.StringReplacementUtil;

@SuppressWarnings("unused")
public class SmallTalk extends Intent {

    public SmallTalk(av av) {
        super(av);
    }

    @Override
    public String getAction() {
        return "smalltalk.*";
    }

    @Override
    public void onIntent(CommandMessage context, AIResponse response) {
        String nickname = context.getAuthor().getName();
        if (context.getMessage().getChannelType().isGuild()) {
            nickname = context.getMember().getEffectiveName();
        }

        context.makeInfo(StringReplacementUtil.replaceAll(
            response.getResult().getFulfillment().getSpeech(),
            "%nick%", nickname
        )).queue();
    }
}
