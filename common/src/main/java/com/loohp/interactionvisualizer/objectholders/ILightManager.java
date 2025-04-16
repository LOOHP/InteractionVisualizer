/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.Location;

public interface ILightManager {

    ILightManager DUMMY_INSTANCE = new ILightManager() {

        @Override
        public int run() {
            return -1;
        }

        @Override
        public void deleteLight(Location location) {
            //do nothing
        }

        @Override
        public void createLight(Location location, int lightlevel, LightType lightType) {
            //do nothing
        }
    };

    void createLight(Location location, int lightlevel, LightType lightType);

    void deleteLight(Location location);

    int run();

}
