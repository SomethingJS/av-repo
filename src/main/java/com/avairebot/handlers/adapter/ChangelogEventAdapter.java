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

package com.avbot.handlers.adapter;

import com.avbot.av;
import com.avbot.changelog.ChangelogHandler;
import com.avbot.changelog.ChangelogMessage;
import com.avbot.contracts.handlers.EventAdapter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;

public class ChangelogEventAdapter extends EventAdapter {

    public ChangelogEventAdapter(av av) {
        super(av);
    }

    public void onMessageDelete(GuildMessageDeleteEvent event) {
        ChangelogHandler.getMessagesMap().remove(event.getMessageIdLong());
    }

    public void onMessageUpdate(MessageUpdateEvent event) {
        createChangelogMessage(event.getMessageIdLong(), event.getMessage());
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        createChangelogMessage(event.getMessageIdLong(), event.getMessage());
    }

    public boolean isChangelogMessage(MessageChannel channel) {
        return channel.getIdLong() == av.getConstants().getChangelogChannelId();
    }

    private void createChangelogMessage(long messageId, Message message) {
        ChangelogHandler.getMessagesMap()
            .put(messageId, new ChangelogMessage(message));
    }
}
