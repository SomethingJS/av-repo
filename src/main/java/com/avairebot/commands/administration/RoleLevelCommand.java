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

package com.avbot.commands.administration;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.utility.RankCommand;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.utilities.ComparatorUtil;
import com.avbot.utilities.MentionableUtil;
import com.avbot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoleLevelCommand extends Command {

    private static final Logger log = LoggerFactory.getLogger(RoleLevelCommand.class);

    public RoleLevelCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Role Level Command";
    }

    @Override
    public String getDescription() {
        return "Toggles XP rewards on or off for the mentioned roles, if no arguments is given the role that currently has their XP rewards disabled will be displayed instead, the command can be used to prevent rewarding users with certain roles experience.\n**Note** this command requires that the leveling system is enabled for the server.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role> [status]` - Toggles the level feature on/off.",
            "`:command` - Lists roles with their XP status disabled."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command @Muted off` - Disables people with the @Muted role from gaining XP.",
            "`:command @Member` - Toggles the XP gains on/off for the Member role.",
            "`:command` - Lists all the roles that currently has their XP status disabled."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            ChannelLevelCommand.class,
            LevelCommand.class,
            LevelHierarchyCommand.class,
            LevelModifierCommand.class,
            LevelAlertsCommand.class,
            RankCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("rolelevel", "rlvl");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.manage_server",
            "throttle:user,1,5"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.LEVEL_AND_EXPERIENCE);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        GuildTransformer guildTransformer = context.getGuildTransformer();
        if (guildTransformer == null || !guildTransformer.isLevels()) {
            //noinspection ConstantConditions
            return sendErrorMessage(
                context,
                "errors.requireLevelFeatureToBeEnabled",
                CommandHandler.getCommand(LevelCommand.class)
                    .getCommand().generateCommandTrigger(context.getMessage())
            );
        }

        if (args.length == 0 || NumberUtil.parseInt(args[0], -1) > 0) {
            return sendDisabledRoles(context, guildTransformer);
        }

        Role role = MentionableUtil.getRole(context.getMessage(), args);
        if (role == null) {
            return sendErrorMessage(context, context.i18n("invalidRole", args[0]));
        }

        if (args.length > 1) {
            return handleToggleRole(context, role, ComparatorUtil.getFuzzyType(args[1]));
        }
        return handleToggleRole(context, role, ComparatorUtil.ComparatorType.UNKNOWN);
    }

    private boolean sendDisabledRoles(CommandMessage context, GuildTransformer transformer) {
        if (transformer.getLevelExemptRoles().isEmpty()) {
            return sendErrorMessage(context, context.i18n("noRolesWithRewardsDisabled",
                generateCommandTrigger(context.getMessage())
            ));
        }

        List<String> roles = new ArrayList<>();
        for (Long roleId : transformer.getLevelExemptRoles()) {
            Role role = context.getGuild().getRoleById(roleId);
            if (role != null) {
                roles.add(role.getAsMention());
            }
        }

        context.makeInfo(context.i18n("listRoles"))
            .set("roles", String.join(", ", roles))
            .setTitle(context.i18n("listRolesTitle",
                transformer.getLevelExemptRoles().size()
            ))
            .queue();

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private boolean handleToggleRole(CommandMessage context, Role role, ComparatorUtil.ComparatorType value) {
        GuildTransformer guildTransformer = context.getGuildTransformer();

        switch (value) {
            case TRUE:
                guildTransformer.getLevelExemptRoles().remove(role.getIdLong());
                break;

            case FALSE:
                guildTransformer.getLevelExemptRoles().add(role.getIdLong());
                break;

            case UNKNOWN:
                if (guildTransformer.getLevelExemptRoles().contains(role.getIdLong())) {
                    guildTransformer.getLevelExemptRoles().remove(role.getIdLong());
                } else {
                    guildTransformer.getLevelExemptRoles().add(role.getIdLong());
                }
                break;
        }

        boolean isEnabled = !guildTransformer.getLevelExemptRoles().contains(role.getIdLong());

        try {
            av.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
                .where("id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("level_exempt_roles", av.gson.toJson(
                        guildTransformer.getLevelExemptRoles()
                    ), true);
                });

            context.makeSuccess(context.i18n("success"))
                .set("role", role.getAsMention())
                .set("status", context.i18n(isEnabled ? "status.enabled" : "status.disabled"))
                .queue();

            return true;
        } catch (SQLException e) {
            log.error("Failed to save the level exempt roles to the database for guild {}, error: {}",
                context.getGuild().getId(), e.getMessage(), e
            );

            context.makeError("Failed to save the changes to the database, please try again. If the issue persists, please contact one of my developers.").queue();

            return false;
        }
    }
}
