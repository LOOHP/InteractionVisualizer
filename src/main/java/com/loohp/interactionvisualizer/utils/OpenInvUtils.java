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

import com.lishid.openinv.OpenInv;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OpenInvUtils {

    private static final boolean openinvhook = InteractionVisualizer.openinv;

    private static OpenInv openInvInstance = null;

    private static OpenInv getOpenInvInstance() {
        if (openInvInstance == null) {
            openInvInstance = (OpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
        }
        return openInvInstance;
    }

    public static boolean isSlientChest(Player player) {
        if (!openinvhook) {
            return false;
        }
        OpenInv openinv = getOpenInvInstance();
        boolean isSilent = openinv.getPlayerSilentChestStatus(player);
        return isSilent;
    }

}
