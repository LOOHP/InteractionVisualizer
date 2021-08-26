package com.loohp.interactionvisualizer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;

public class Toggle {
	
	private static InteractionVisualizer plugin = InteractionVisualizer.plugin;
	
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
