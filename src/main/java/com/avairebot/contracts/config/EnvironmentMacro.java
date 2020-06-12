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

package com.avbot.contracts.config;

import com.avbot.config.Configuration;
import com.avbot.config.EnvironmentOverride;

import javax.annotation.Nonnull;

public interface EnvironmentMacro {

    /**
     * Handles the environment macro by formatting and setting the
     * given environment value to the given configuration.
     *
     * @param environmentValue The value of the environment variable that invoked the macro.
     * @param configuration    The configuration the {@link EnvironmentOverride#override(Configuration) override(Configuration)}
     *                         method was called for.
     */
    void handle(@Nonnull String environmentValue, @Nonnull Configuration configuration);
}
