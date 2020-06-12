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

package com.avbot.contracts.commands;

import com.avbot.av;
import com.avbot.commands.CommandPriority;

import java.util.Collections;
import java.util.List;

public abstract class SystemCommand extends Command {

    /**
     * Creates the given command instance by calling {@link #SystemCommand(av, boolean)} with allowDM set to true.
     *
     * @param av The av class instance.
     */
    public SystemCommand(av av) {
        this(av, true);
    }

    /**
     * Creates the given command instance with the given
     * av instance and the allowDM settings.
     *
     * @param av  The av class instance.
     * @param allowDM Determines if the command can be used in DMs.
     */
    public SystemCommand(av av, boolean allowDM) {
        super(av, allowDM);
    }

    @Override
    public List<String> getMiddleware() {
        if (!getCommandPriority().equals(CommandPriority.SYSTEM_ROLE)) {
            return Collections.singletonList("isBotAdmin");
        }
        return Collections.singletonList("isBotAdmin:use-role");
    }

    @Override
    public CommandPriority getCommandPriority() {
        return CommandPriority.SYSTEM;
    }
}
