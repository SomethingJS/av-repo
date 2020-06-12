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
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.permissions.Permissions;
import com.avbot.utilities.ComparatorUtil;
import com.avbot.utilities.MentionableUtil;
import com.avbot.utilities.NumberUtil;
import com.avbot.utilities.RestActionUtil;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SlowmodeCommand extends Command {

    public SlowmodeCommand(av av) {
        super(av, false);
    }

    @Override
    public String getName() {
        return "Slowmode Command";
    }

    @Override
    public String getDescription() {
        return "Disables the slowmode or enables it with the given limit, users with the **" + Permissions.MESSAGE_MANAGE.getPermission().getName() + "**  or **" + Permissions.MANAGE_CHANNEL.getPermission().getName() + "** permissions are exempt from slowmode limits.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command [channel] <off>` - Disables slowmode for the current channel.",
            "`:command [channel] <seconds>` - Enables slowmode, allowing one message per user every given second."
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command off` - Disables slowmode.",
            "`:command #general off` - Disables slowmode in the general channel.",
            "`:command 5` - Enables slowmode, allowing one message every five seconds.",
            "`:command #slow-chat 30` - Enables slowmode in the slow chat channel, allowing one message every 30 seconds per user."
        );
    }

    @Override
    public List<Class<? extends Command>> getRelations() {
        return Collections.singletonList(PurgeCommand.class);
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("slowmode");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:all,general.manage_channels",
            "throttle:guild,1,5"
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
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        Channel channel = MentionableUtil.getChannel(context.getMessage(), args);
        if (channel == null) {
            return handleSlowmode(context, context.getChannel(), args);
        }

        if (!channel.getType().equals(ChannelType.TEXT)) {
            return sendErrorMessage(context, "invalidTextChannel", channel.getName());
        }

        return handleSlowmode(context, (TextChannel) channel, Arrays.copyOfRange(args, 1, args.length));
    }

    private boolean handleSlowmode(CommandMessage context, TextChannel channel, String[] args) {
        if (args.length == 0) {
            return sendErrorMessage(context, context.i18n("missingArgument"));
        }

        if (ComparatorUtil.isFuzzyFalse(args[0])) {
            if (channel.getSlowmode() != 0) {
                channel.getManager().setSlowmode(0).queue(null, RestActionUtil.ignore);
            }

            context.makeSuccess(context.i18n("disabled"))
                .set("textChannel", channel.getAsMention())
                .queue();

            return true;
        }

        int slowmode = NumberUtil.parseInt(args[0], -1);
        if (slowmode < 1 || slowmode > 120) {
            return sendErrorMessage(context, context.i18n("mustBeValidNumbers"));
        }

        if (channel.getSlowmode() == slowmode) {
            context.makeSuccess(context.i18n("message"))
                .set("textChannel", channel.getAsMention())
                .set("slowmode", slowmode)
                .queue();

            return true;
        }

        channel.getManager().setSlowmode(slowmode).queue(aVoid -> {
            context.makeSuccess(context.i18n("message"))
                .set("textChannel", channel.getAsMention())
                .set("slowmode", slowmode)
                .queue();
        }, error -> {
            context.makeError(context.i18n("failedToUpdate"))
                .set("textChannel", channel.getAsMention())
                .set("message", error.getMessage())
                .queue();
        });

        return true;
    }
}
