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

package com.avbot.middleware;

import com.avbot.av;
import com.avbot.contracts.middleware.Middleware;
import com.avbot.factories.MessageFactory;
import com.avbot.language.I18n;
import com.avbot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class IsMusicChannelMiddleware extends Middleware {

    public IsMusicChannelMiddleware(av av) {
        super(av);
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild() || stack.getDatabaseEventHolder().getGuild() == null) {
            return stack.next();
        }

        if (stack.getDatabaseEventHolder().getGuild().getMusicChannelText() == null) {
            return stack.next();
        }

        TextChannel textChannelById = message.getGuild().getTextChannelById(
            stack.getDatabaseEventHolder().getGuild().getMusicChannelText()
        );

        if (textChannelById == null) {
            return stack.next();
        }

        if (message.getChannel().getIdLong() == textChannelById.getIdLong()) {
            return stack.next();
        }

        return runMessageCheck(message, () -> {
            MessageFactory.makeWarning(message, I18n.get(message.getGuild()).getString(
                "music.internal.musicChannel",
                "You can only use music commands in the :channel channel."
            )).set("channel", textChannelById.getAsMention()).queue(
                musicMessage -> musicMessage.delete().queueAfter(30, TimeUnit.SECONDS, null, RestActionUtil.ignore),
                RestActionUtil.ignore
            );

            return false;
        });
    }
}
