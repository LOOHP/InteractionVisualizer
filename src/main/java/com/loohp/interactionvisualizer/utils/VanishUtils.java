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

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VanishUtils {

    public static Boolean vanishenabled = InteractionVisualizer.vanish;
    public static Boolean cmienabled = InteractionVisualizer.cmi;
    public static Boolean ess3enabled = InteractionVisualizer.ess3;

    public static boolean isVanished(Player player) {
        if (vanishenabled) {
            if (VanishAPI.isInvisible(player)) {
                return true;
            }
        }
        if (cmienabled) {
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            if (user.isVanished()) {
                return true;
            }
        }
        if (ess3enabled) {
            Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            User user = ess3.getUser(player);
            return user.isVanished();
        }
        return false;
    }

}
