/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactionvisualizer.utils;

import com.loohp.interactionvisualizer.managers.MaterialManager;
import org.bukkit.Material;

import java.util.Set;

public class MaterialUtils {

    public static MaterialMode getMaterialType(Material material) {
        if (MaterialManager.getTools().contains(material)) {
            return MaterialMode.TOOL;
        }
        if (MaterialManager.getStanding().contains(material)) {
            return MaterialMode.STANDING;
        }
        if (MaterialManager.getLowblocks().contains(material)) {
            return MaterialMode.LOWBLOCK;
        }
        if (MaterialManager.getBlockexceptions().contains(material)) {
            return MaterialMode.ITEM;
        }
        if (material.isBlock()) {
            return MaterialMode.BLOCK;
        }
        return MaterialMode.ITEM;
    }

    public static Set<Material> getNonSolidSet() {
        return MaterialManager.getNonSolid();
    }

    public enum MaterialMode {
        TOOL("Tool"),
        STANDING("Standing"),
        LOWBLOCK("LowBlock"),
        ITEM("Item"),
        BLOCK("Block");

        public static MaterialMode getModeFromName(String name) {
            for (MaterialMode mode : MaterialMode.values()) {
                if (mode.toString().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
            return null;
        }
        String name;

        MaterialMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
