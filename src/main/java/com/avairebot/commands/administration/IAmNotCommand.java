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

package com.avbot.commands.administration;

import com.avbot.av;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandContext;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.factories.MessageFactory;
import com.avbot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IAmNotCommand extends Command {

    public IAmNotCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "I Am Not Command";
    }

    @Override
    public String getDescription() {
        return "Removes the role with the given name from you if it is in the self-assignable list of roles.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command <role>`");
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command DJ`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(IAmCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("iamnot", "iamn");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.ROLE_ASSIGNMENTS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "role");
        }

        String roleName = String.join(" ", args);
        Role role = RoleUtil.getRoleFromMentionsOrName(context.getMessage(), roleName);
        if (role == null) {
            context.makeWarning(context.i18nRaw("administration.common.invalidRole"))
                .set("role", roleName)
                .queue(message -> handleMessage(context, message));
            return false;
        }

        GuildTransformer transformer = context.getGuildTransformer();
        if (transformer == null) {
            return sendErrorMessage(context, "errors.errorOccurredWhileLoading", "server settings");
        }

        if (!transformer.getSelfAssignableRoles().containsValue(role.getName().toLowerCase())) {
            context.makeWarning(context.i18n("notSelfAssignable"))
                .set("role", roleName)
                .queue(message -> handleMessage(context, message));
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(context.getGuild().getSelfMember().getRoles(), role)) {
            context.makeWarning(context.i18n("roleIsHigherInTheHierarchy"))
                .set("role", roleName)
                .queue(message -> handleMessage(context, message));
            return false;
        }

        if (RoleUtil.hasRole(context.getMember(), role)) {
            context.getGuild().getController().removeSingleRoleFromMember(context.getMember(), role).queue();
        }

        context.makeSuccess(context.i18n("message"))
            .set("role", role.getName())
            .queue(message -> handleMessage(context, message));
        return true;
    }

    private void handleMessage(CommandContext context, Message message) {
        MessageFactory.deleteMessage(context.getMessage());
        MessageFactory.deleteMessage(message, 45);
    }
}
