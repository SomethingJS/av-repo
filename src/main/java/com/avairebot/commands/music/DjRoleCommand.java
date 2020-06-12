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

package com.avbot.commands.music;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DjRoleCommand extends Command {

    public DjRoleCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "DJ Role Command";
    }

    @Override
    public String getDescription() {
        return "This command can be used to set the role used for music DJing, anyone with the music DJ role will be able to use commands like force skip, force stop, repeat queue, pause, resume, etc.";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("djrole");
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command` - Displays the current DJ Role for the server.",
            "`:command <role>` - Sets the DJ role to the given role.",
            "`:command reset` - Resets the DJ role back to using the default role."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command` - Displays the current role used as the DJ role.",
            "`:command @Music` - Sets the DJ role to the \"Music\" role.",
            "`:command reset` - Resets the role used as the DJ role."
        );
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:guild,1,4"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MUSIC_SETTINGS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (args.length == 0) {
            return sendCurrentDjRole(context, guildTransformer);
        }

        if (args[0].equalsIgnoreCase("reset")) {
            return resetDjRole(context, guildTransformer);
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(
            context.getMessage(),
            String.join(" ", args)
        );

        if (role == null) {
            return sendErrorMessage(context, context.i18n("invalidRoleMentioned"),
                String.join(" ", args)
            );
        }

        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("dj_role", role.getId());
                });

            guildTransformer.setDjRole(role.getId());
        } catch (SQLException e) {
            av.getLogger().error("Failed to update the DJ role for {}", context.getGuild().getId(), e);

            context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
            return false;
        }

        context.makeSuccess(context.i18n("selectedRoleForMusic"))
            .set("role", role.getAsMention())
            .queue();

        return true;
    }

    private boolean sendCurrentDjRole(CommandMessage context, GuildTransformer guildTransformer) {
        if (guildTransformer.getDjRole() == null) {
            context.makeInfo(context.i18n("noDjRoleSelected"))
                .queue();

            return true;
        }

        Role role = context.getGuild().getRoleById(guildTransformer.getDjRole());
        if (role == null) {
            context.makeInfo(context.i18n("noDjRoleSelected"))
                .queue();

            return true;
        }

        context.makeInfo(context.i18n("currentSelectedRole"))
            .set("role", role.getAsMention())
            .queue();

        return true;
    }

    private boolean resetDjRole(CommandMessage context, GuildTransformer guildTransformer) {
        if (guildTransformer.getDjRole() != null) {
            try {
                av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                    .where("id", context.getGuild().getId())
                    .update(statement -> {
                        statement.set("dj_role", null);
                    });

                guildTransformer.setDjRole(null);
            } catch (SQLException e) {
                av.getLogger().error("Failed to update the DJ role for {}", context.getGuild().getId(), e);

                context.makeError("Failed to save the guild settings: " + e.getMessage()).queue();
                return false;
            }
        }

        context.makeSuccess(context.i18n("resetDjRole")).queue();

        return true;
    }
}
