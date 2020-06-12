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

package com.avbot.handlers;

import com.avbot.av;
import com.avbot.contracts.handlers.EventListener;
import com.avbot.plugin.PluginLoader;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

public class EventEmitter {

    private final av av;

    /**
     * Creates a new event emitter instance using
     * the given av application instance.
     *
     * @param av The av instance that the event
     *               emitter should be created for.
     */
    public EventEmitter(av av) {
        this.av = av;
    }

    /**
     * Pushes the given event to all loaded plugins with at least
     * one event listener, forwarding the custom events through
     * each plugin one by one.
     *
     * @param event The event that should be pushed to all the loaded plugins.
     */
    public void push(Event event) {
        Checks.notNull(event, "event instance");
        for (PluginLoader plugin : av.getPluginManager().getPlugins()) {
            for (ListenerAdapter listener : plugin.getEventListeners()) {
                if (listener != null && listener instanceof EventListener) {
                    ((EventListener) listener).onCustomEvent(event);
                }
            }
        }
    }
}
