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

package com.loohp.interactionvisualizer;

import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Toggle {

    private static final InteractionVisualizer plugin = InteractionVisualizer.plugin;

    public static boolean toggle(CommandSender sender, Player player, Modules mode, boolean verbose, EntryKey... entries) {
        return toggle(sender, player, mode, verbose, null, entries);
    }

    public static boolean toggle(CommandSender sender, Player player, Modules mode, boolean verbose, String entryGroupName, EntryKey... entries) {
        boolean value = true;
        for (EntryKey entry : entries) {
            if (!InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                return false;
            }
            if (InteractionVisualizerAPI.hasPlayerEnabledModule(player, mode, entry)) {
                value = false;
                break;
            }
        }
        return toggle(sender, player, mode, value, verbose, entryGroupName, entries);
    }

    public static boolean toggle(CommandSender sender, Player player, Modules mode, boolean value, boolean verbose, EntryKey... entries) {
        return toggle(sender, player, mode, value, verbose, null, entries);
    }

    public static boolean toggle(CommandSender sender, Player player, Modules mode, boolean value, boolean verbose, String entryGroupName, EntryKey... entries) {
        for (EntryKey entry : entries) {
            if (!InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                return false;
            }
        }
        for (EntryKey entry : entries) {
            InteractionVisualizer.preferenceManager.setPlayerPreference(player.getUniqueId(), mode, entry, value, false);
            if (verbose && entryGroupName == null) {
                if (value) {
                    sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%m", InteractionVisualizerAPI.getUserFriendlyName(mode)).replace("%e", InteractionVisualizerAPI.getUserFriendlyName(entry))));
                } else {
                    sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%m", InteractionVisualizerAPI.getUserFriendlyName(mode)).replace("%e", InteractionVisualizerAPI.getUserFriendlyName(entry))));
                }
            }
        }
        if (verbose && entryGroupName != null) {
            if (value) {
                sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%m", InteractionVisualizerAPI.getUserFriendlyName(mode)).replace("%e", entryGroupName)));
            } else {
                sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%m", InteractionVisualizerAPI.getUserFriendlyName(mode)).replace("%e", entryGroupName)));
            }
        }
        InteractionVisualizer.preferenceManager.updatePlayer(player, true);
        return true;
    }

}
