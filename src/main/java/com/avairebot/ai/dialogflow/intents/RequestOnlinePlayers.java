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
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Member;

@SuppressWarnings("unused")
public class RequestOnlinePlayers extends Intent {

    public RequestOnlinePlayers(av av) {
        super(av);
    }

    @Override
    public String getAction() {
        return "request.online-players";
    }

    @Override
    public void onIntent(CommandMessage context, AIResponse response) {
        if (!context.getMessage().getChannelType().isGuild()) {
            context.makeWarning("Right now it's just me and you online ;)").queue();
            return;
        }

        int online = 0;
        for (Member member : context.getGuild().getMembers()) {
            if (!member.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
                online++;
            }
        }

        context.makeInfo("There are **:online** people online out of **:total** people on the server.")
            .set("online", online)
            .set("total", context.getGuild().getMembers().size())
            .queue();
    }
}
