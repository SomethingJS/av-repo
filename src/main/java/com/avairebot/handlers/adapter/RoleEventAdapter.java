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

package com.avbot.handlers.adapter;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.contracts.handlers.EventAdapter;
import com.avbot.database.controllers.GuildController;
import com.avbot.database.transformers.GuildTransformer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdateNameEvent;

import java.sql.SQLException;
import java.util.Map;

public class RoleEventAdapter extends EventAdapter {

    /**
     * Instantiates the event adapter and sets the av class instance.
     *
     * @param av The av application class instance.
     */
    public RoleEventAdapter(av av) {
        super(av);
    }

    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(av, event.getGuild());
        if (transformer == null || transformer.getSelfAssignableRoles().isEmpty()) {
            return;
        }

        if (!transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
            return;
        }

        try {
            transformer.getSelfAssignableRoles().put(event.getRole().getId(), event.getRole().getName().toLowerCase());
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", av.gson.toJson(transformer.getSelfAssignableRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onRoleDelete(RoleDeleteEvent event) {
        GuildTransformer transformer = GuildController.fetchGuild(av, event.getGuild());
        if (transformer == null) {
            return;
        }

        handleMuteRole(event, transformer);
        handleAutoroles(event, transformer);
        handleLevelRoles(event, transformer);
        handleMusicDjRole(event, transformer);
        handleSelfAssignableRoles(event, transformer);
    }

    private void handleMuteRole(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getMuteRole() == null || !event.getRole().getId().equals(transformer.getMuteRole())) {
            return;
        }

        try {
            transformer.setMuteRole(null);
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("mute_role", null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAutoroles(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getAutorole() == null || !event.getRole().getId().equals(transformer.getAutorole())) {
            return;
        }

        try {
            transformer.setAutorole(null);
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> statement.set("autorole", null));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLevelRoles(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getLevelRoles().isEmpty() || !transformer.getLevelRoles().containsValue(event.getRole().getId())) {
            return;
        }

        int key = -1;
        for (Map.Entry<Integer, String> entry : transformer.getLevelRoles().entrySet()) {
            if (entry.getValue().equals(event.getRole().getId())) {
                key = entry.getKey();
            }
        }

        try {
            transformer.getLevelRoles().remove(key);
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("level_roles", av.gson.toJson(transformer.getLevelRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleMusicDjRole(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getDjRole() == null || !transformer.getDjRole().equals(event.getRole().getId())) {
            return;
        }

        try {
            transformer.setDjRole(null);
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("dj_role", null);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleSelfAssignableRoles(RoleDeleteEvent event, GuildTransformer transformer) {
        if (transformer.getSelfAssignableRoles().isEmpty() || !transformer.getSelfAssignableRoles().containsKey(event.getRole().getId())) {
            return;
        }

        try {
            transformer.getSelfAssignableRoles().remove(event.getRole().getId());
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", event.getGuild().getId())
                .update(statement -> {
                    statement.set("claimable_roles", av.gson.toJson(transformer.getSelfAssignableRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateRoleData(Guild guild) {
        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .useAsync(true)
                .where("id", guild.getId())
                .update(statement -> {
                    statement.set("roles_data", GuildController.buildRoleData(guild.getRoles()), true);
                });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
