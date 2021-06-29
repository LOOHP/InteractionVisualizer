package com.loohp.interactionvisualizer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;

public class Toggle {
	
	private static InteractionVisualizer plugin = InteractionVisualizer.plugin;
	
	public static boolean toggle(CommandSender sender, Player player, Modules mode, boolean value, EntryKey... entries) {
		for (EntryKey entry : entries) {
			if (!InteractionVisualizer.preferenceManager.getRegisteredEntries().contains(entry)) {
				return false;
			}
		}
		for (EntryKey entry : entries) {
			InteractionVisualizer.preferenceManager.setPlayerPreference(player.getUniqueId(), mode, entry, value, false);
			if (value) {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%m", mode.toString().toLowerCase()).replace("%e", entry.toSimpleString().toLowerCase())));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%m", mode.toString().toLowerCase()).replace("%e", entry.toSimpleString().toLowerCase())));
			}
		}
		InteractionVisualizer.preferenceManager.updatePlayer(player, true);
		return true;
	}

}
