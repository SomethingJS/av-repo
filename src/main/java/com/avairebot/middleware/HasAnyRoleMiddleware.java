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

package com.avbot.middleware;

import com.avbot.av;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.middleware.Middleware;
import com.avbot.factories.MessageFactory;
import com.avbot.permissions.Permissions;
import com.avbot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HasAnyRoleMiddleware extends Middleware {

    public HasAnyRoleMiddleware(av av) {
        super(av);
    }

    @Override
    public String buildHelpDescription(@Nonnull CommandMessage context, @Nonnull String[] arguments) {
        if (arguments.length == 1) {
            return String.format("**The `%s` role is required to use this command!**", arguments[0]);
        }

        return String.format("**One of the `%s` roles is required to use this command!**",
            String.join("`, `", arguments)
        );
    }

    @Override
    public boolean handle(@Nonnull Message message, @Nonnull MiddlewareStack stack, String... args) {
        if (!message.getChannelType().isGuild()) {
            return stack.next();
        }

        if (message.getMember().hasPermission(Permissions.ADMINISTRATOR.getPermission())) {
            return stack.next();
        }

        List<Role> roles = message.getMember().getRoles();
        for (String roleName : args) {
            if (hasRole(roles, roleName)) {
                return stack.next();
            }
        }

        return runMessageCheck(message, () -> {
            MessageFactory.makeError(message, "You don't have any of the required roles to execute this command:\n`:role`")
                .set("role", String.join("`, `", args))
                .queue(newMessage -> newMessage.delete().queueAfter(45, TimeUnit.SECONDS, null, RestActionUtil.ignore));

            return false;
        });
    }

    private boolean hasRole(List<Role> roles, String roleName) {
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }

}
