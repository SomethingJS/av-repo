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
import com.avbot.contracts.commands.ApplicationShutdownCommand;
import com.avbot.shared.ExitCodes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UpdateCommand extends ApplicationShutdownCommand {

    public UpdateCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Update Command";
    }

    @Override
    public String getDescription() {
        return "Schedule a time the bot should be automatically-updated, the bot will shutdown, update itself, and start back up again.\nThis requires [av/watchdog](https://github.com/av/watchdog) to work, without it the bot will just shutdown.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command now` - Updates the bot now.",
            "`:command cancel` - Cancels the update process.",
            "`:command <time>` - Schedules a time the bot should be updated."
        );
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("update");
    }

    @Override
    public int exitCode() {
        return ExitCodes.EXIT_CODE_UPDATE;
    }

    @Override
    public String shutdownNow() {
        return "Shutting down processes and updating the application... See you soon :wave:";
    }

    @Override
    public String scheduleShutdown() {
        return "The bot has been scheduled to be updated in :fromNow.\n**Date:** :date";
    }

    @Override
    public String scheduleCancel() {
        return "The update process has been canceled.";
    }
}
