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
import java.util.List;

public class EatCommand extends InteractionCommand {

    public EatCommand(av av) {
        super(av);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/cVOqRpf.gif",
            "https://i.imgur.com/Ec7OmbS.gif",
            "https://i.imgur.com/zhwLNTC.gif",
            "https://i.imgur.com/ShhXrms.gif",
            "https://i.imgur.com/7eUwFc0.gif",
            "https://i.imgur.com/sxB3LZt.gif",
            "https://i.imgur.com/y7GdCeq.gif",
            "https://i.imgur.com/pFl0PkY.gif",
            "https://i.imgur.com/ux8rVDC.gif"
        );
    }

    @Override
    public String getName() {
        return "Eat Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("eats", "eat", "eats-with");
    }
}
