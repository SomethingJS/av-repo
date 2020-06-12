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

public class HugCommand extends InteractionCommand {

    public HugCommand(av av) {
        super(av);
    }

    @Override
    public List<String> getInteractionImages() {
        return Arrays.asList(
            "https://i.imgur.com/aBdIEEu.gif",
            "https://i.imgur.com/03grRGj.gif",
            "https://i.imgur.com/EuIBiLi.gif",
            "https://i.imgur.com/8KgVR9j.gif",
            "https://i.imgur.com/ZepPo0t.gif",
            "https://i.imgur.com/iIsFQ3q.gif",
            "https://i.imgur.com/XHhFoR1.gif",
            "https://i.imgur.com/psGdps5.gif",
            "https://i.imgur.com/OPKBDeA.gif",
            "https://i.imgur.com/D0GABc2.gif",
            "https://i.imgur.com/LtVBCX3.gif",
            "https://i.imgur.com/o8JQtVL.gif"
        );
    }

    @Override
    public String getName() {
        return "Hug Command";
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("hug", "hugs");
    }
}
