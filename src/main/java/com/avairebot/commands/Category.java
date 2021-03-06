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

package com.avbot.commands;

import com.avbot.av;
import com.avbot.database.controllers.GuildController;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.utilities.CacheUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.core.entities.Message;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class Category {

    public static final Cache<Object, Object> cache = CacheBuilder.newBuilder()
        .recordStats()
        .expireAfterWrite(2500, TimeUnit.MILLISECONDS)
        .build();

    private final av av;
    private final String name;
    private final String prefix;

    private boolean isGlobal = false;

    public Category(av av, String name, String prefix) {
        this.av = av;
        this.name = name;
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefix(@Nonnull Message message) {
        if (isGlobal) {
            return getPrefix();
        }

        if (message.getGuild() == null) {
            return getPrefix();
        }

        return (String) CacheUtil.getUncheckedUnwrapped(cache, asKey(message), () -> {
            GuildTransformer transformer = GuildController.fetchGuild(av, message);

            return transformer == null ? getPrefix() : transformer.getPrefixes().getOrDefault(
                getName().toLowerCase(), getPrefix()
            );
        });
    }

    public boolean hasCommands() {
        return CommandHandler.getCommands().stream().
            filter(container -> container.getCategory().equals(this))
            .count() > 0;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    Category setGlobal(boolean value) {
        isGlobal = value;
        return this;
    }

    public boolean isGlobalOrSystem() {
        return isGlobal || name.equalsIgnoreCase("system");
    }

    private String asKey(Message message) {
        return message.getGuild().getId() + ":" + name;
    }
}
