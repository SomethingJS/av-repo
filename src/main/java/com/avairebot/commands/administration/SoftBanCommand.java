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
import com.avbot.commands.CommandHandler;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "banable-user-command")
public class SoftBanCommand extends BanableCommand {

    public SoftBanCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Soft Ban Command";
    }

    @Override
    public String getDescription() {
        return "Bans the mentioned user from the server with the provided reason without removing any of the messages they have sent, this action will be reported to any channel that has modloging enabled.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <user> [reason]` - Bans the mentioned user with the given reason.",
            "`:command <user id> [reason]` - Bans the user with given ID and for the given reason."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command @Senither Being a potato`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            UnbanCommand.class,
            BanCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("softban", "sban");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:all,general.ban_members",
            "throttle:user,1,4"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MODERATION);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean onCommand(CommandMessage message, String[] args) {
        message.setI18nCommandPrefix(
            CommandHandler.getCommand(BanCommand.class)
        );

        return ban(av, this, message, args, true);
    }
}
