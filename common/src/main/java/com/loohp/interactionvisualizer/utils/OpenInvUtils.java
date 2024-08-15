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

import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OpenInvUtils {

    private static final boolean openInvHook = InteractionVisualizer.openinv;

    private static Object openInvInstance = null;
    private static Method getSilentContainerStatusMethod;

    private static Object getOpenInvInstance() {
        if (openInvInstance == null) {
            openInvInstance = Bukkit.getPluginManager().getPlugin("OpenInv");
            try {
                getSilentContainerStatusMethod = openInvInstance.getClass().getMethod("getSilentContainerStatus", OfflinePlayer.class);
            } catch (NoSuchMethodException e) {
                try {
                    getSilentContainerStatusMethod = openInvInstance.getClass().getMethod("getPlayerSilentChestStatus", OfflinePlayer.class);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return openInvInstance;
    }

    public static boolean isSilentChest(Player player) {
        if (!openInvHook) {
            return false;
        }
        try {
            Object openinv = getOpenInvInstance();
            return (boolean) getSilentContainerStatusMethod.invoke(openinv, player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
