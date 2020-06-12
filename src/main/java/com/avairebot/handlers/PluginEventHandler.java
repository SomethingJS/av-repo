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
import com.avbot.contracts.handlers.EventHandler;
import com.avbot.plugin.PluginLoader;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PluginEventHandler extends EventHandler {

    /**
     * Instantiates the event handler and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public PluginEventHandler(av av) {
        super(av);
    }

    @Override
    public void onGenericEvent(Event event) {
        for (PluginLoader plugin : av.getPluginManager().getPlugins()) {
            for (ListenerAdapter listener : plugin.getEventListeners()) {
                listener.onEvent(event);
            }
        }
    }
}
