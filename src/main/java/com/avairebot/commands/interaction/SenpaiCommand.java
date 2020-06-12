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

package com.avbot.commands.interaction;

import com.avbot.av;
import com.avbot.contracts.commands.InteractionCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SenpaiCommand extends InteractionCommand {

    public SenpaiCommand(av av) {
        super(av);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/7X0TzmR.gif",
            "https://i.imgur.com/j4vdwZh.gif",
            "https://i.imgur.com/e1MXv4L.gif",
            "https://i.imgur.com/bUUJS7A.gif",
            "https://i.imgur.com/qkcRXl6.gif"
        );
    }

    @Override
    public String getName() {
        return "Senpai Command";
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("senpai");
    }
}
