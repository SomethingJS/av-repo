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

package com.avbot.commands.system.plugin;

import com.avbot.av;
import com.avbot.cache.CacheType;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.system.PluginCommand;
import com.avbot.contracts.commands.plugin.PluginSubCommand;
import com.avbot.contracts.plugin.Plugin;
import com.avbot.plugin.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UninstallPlugin extends PluginSubCommand {

    private static final Logger log = LoggerFactory.getLogger(UninstallPlugin.class);

    /**
     * Creates a new plugin sub command instance.
     *
     * @param av  The main av application instance.
     * @param command The parent plugin command instance.
     */
    public UninstallPlugin(av av, PluginCommand command) {
        super(av, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return command.sendErrorMessage(context, "You must include the name of the plugin you'd like to uninstall!");
        }

        Plugin plugin = av.getPluginManager().getPluginByName(args[0]);
        if (plugin == null) {
            return command.sendErrorMessage(context, "Couldn't find any plugin called `{0}`, are you sure it exists?", args[0]);
        }

        if (!plugin.isInstalled()) {
            return command.sendErrorMessage(context, "The `{0}` plugin is not installed, you can't uninstall plugins that aren't installed.", plugin.getName());
        }

        PluginLoader loader = getPluginLoaderFromPlugin(plugin);
        //noinspection ConstantConditions
        if (!loader.getClassLoader().getFile().delete()) {
            List<String> pluginsToDelete = new ArrayList<>();

            Object deletePlugins = av.getCache().getAdapter(CacheType.FILE).get("deleted-plugins");
            if (deletePlugins instanceof List) {
                pluginsToDelete.addAll((List<String>) deletePlugins);
            }

            pluginsToDelete.add(loader.getClassLoader().getFile().toString());
            av.getCache().getAdapter(CacheType.FILE)
                .put("deleted-plugins", pluginsToDelete, 10800);
        }

        av.getPluginManager().unloadPlugin(plugin);

        try {
            deletePluginIndex(plugin);
        } catch (NullPointerException | SQLException e) {
            log.error("Failed to delete plugin index from the database, error: {}", e.getMessage(), e);
        }

        context.makeSuccess("The **:name** plugin have successfully been uninstalled, however the bot may need to restart before all the features associated with the :name plugin is removed.")
            .set("name", plugin.getName())
            .queue();

        return true;
    }

    /**
     * Gets the plugin loader for the given plugin instance.
     *
     * @param plugin The plugin that the loader should be fetched for.
     * @return The matching plugin loader for the given plugin,
     *         or {@code NULL} if there were no match.
     */
    private PluginLoader getPluginLoaderFromPlugin(Plugin plugin) {
        for (PluginLoader pluginLoader : av.getPluginManager().getPlugins()) {
            if (pluginLoader.getName().equalsIgnoreCase(plugin.getName())) {
                return pluginLoader;
            }
        }
        return null;
    }
}
