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

package com.avbot.commands.system;

import com.avbot.av;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.SystemCommand;
import com.avbot.plugin.PluginLoader;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends SystemCommand {

    public ReloadCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Reload Configuration Command";
    }

    @Override
    public String getDescription() {
        return "Reloads the main configuration, and all the configs for loaded plugins.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("reload");
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        av.getConfig().reloadConfig();
        av.getConstants().reloadConfig();

        for (PluginLoader loader : av.getPluginManager().getPlugins()) {
            loader.getClassLoader().getPlugin().reloadConfig();
        }

        context.makeSuccess("Configuration has been successfully reloaded!").queue();

        return true;
    }
}
