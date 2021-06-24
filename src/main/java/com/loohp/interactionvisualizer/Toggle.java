package com.loohp.interactionvisualizer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.database.Database;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;

public class Toggle {
	
	private static InteractionVisualizer plugin = InteractionVisualizer.plugin;
	
	public static void toggle(CommandSender sender, Player player, Modules mode) {
		switch (mode) {
		case ITEMSTAND:
			if (Database.toggleItemStand(player)) {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%s", "itemstand".toUpperCase())));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%s", "itemstand".toUpperCase())));
			}
			break;
		case ITEMDROP:
			if (Database.toggleItemDrop(player)) {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%s", "itemdrop".toUpperCase())));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%s", "itemdrop".toUpperCase())));
			}
			break;
		case HOLOGRAM:
			if (Database.toggleHologram(player)) {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOn").replace("%s", "hologram".toUpperCase())));
			} else {
				sender.sendMessage(ChatColorUtils.translateAlternateColorCodes('&', plugin.getConfiguration().getString("Messages.Toggle.ToggleOff").replace("%s", "hologram".toUpperCase())));
			}
			break;
		}
	}
	
	public static boolean toggle(Player player, Modules mode) {
		switch (mode) {
		case ITEMSTAND:
			return Database.toggleItemStand(player);
		case ITEMDROP:
			return Database.toggleItemDrop(player);
		case HOLOGRAM:
			return Database.toggleHologram(player);
		}
		return false;
	}

}
