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
import com.avbot.contracts.commands.CacheFingerprint;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.modlog.Modlog;
import com.avbot.modlog.ModlogAction;
import com.avbot.modlog.ModlogType;
import com.avbot.utilities.NumberUtil;
import com.avbot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Guild;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CacheFingerprint(name = "banable-user-command")
public class UnbanCommand extends Command {

    public UnbanCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Unban Command";
    }

    @Override
    public String getDescription() {
        return "Unbans the user with the given ID from the server if they are banned, if a modlog channel is setup, the unban will be logged to the channel as well.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList(
            "`:command <user id> [reason]` - Unbans the user with given ID and for the given reason."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Collections.singletonList("`:command 88739639380172800 Wasn't actually a twat`");
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Arrays.asList(
            SoftBanCommand.class,
            BanCommand.class
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("unban");
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
    public boolean onCommand(CommandMessage context, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, "errors.missingArgument", "user id");
        }

        if (!NumberUtil.isNumeric(args[0]) || args[0].length() < 17) {
            return sendErrorMessage(context, context.i18n("invalidUserIdGiven"));
        }

        try {
            context.getGuild().getBanById(Long.parseLong(args[0])).queue(ban -> {
                handleBan(context, ban, Arrays.copyOfRange(args, 1, args.length));
            }, error -> {
                context.makeWarning(context.i18n("noUserIsBanned"))
                    .set("id", args[0])
                    .queue();
            });
        } catch (NumberFormatException e) {
            return sendErrorMessage(context, context.i18n("invalidUserIdGiven"));
        }

        return true;
    }

    private void handleBan(CommandMessage context, Guild.Ban ban, String[] args) {
        String reason = args.length == 0
            ? "No reason was given."
            : String.join(" ", args);

        context.getGuild().getController().unban(ban.getUser()).queue(aVoid -> {
            ModlogAction modlogAction = new ModlogAction(
                ModlogType.UNBAN, context.getAuthor(), ban.getUser(), reason
            );

            Modlog.log(av, context, modlogAction);

            context.makeSuccess(context.i18n("success"))
                .set("target", ban.getUser().getName() + "#" + ban.getUser().getDiscriminator())
                .set("reason", reason)
                .queue(ignoreMessage -> context.delete().queue(null, RestActionUtil.ignore));
        }, throwable -> context.makeWarning(context.i18n("failedToUnban"))
            .set("target", ban.getUser().getName() + "#" + ban.getUser().getDiscriminator())
            .set("error", throwable.getMessage())
            .queue());
    }
}
