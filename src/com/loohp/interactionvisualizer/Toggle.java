package com.loohp.interactionvisualizer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.Database.Database;

import net.md_5.bungee.api.ChatColor;

public class Toggle {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	
	public static void toggle(CommandSender sender, Player player, String mode) {
		switch (mode.toLowerCase()) {
		case "itemstand":
			if (Database.toggleItemStand(player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", "itemstand".toUpperCase())));
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", "itemstand".toUpperCase())));
			}
			break;
		case "itemdrop":
			if (Database.toggleItemDrop(player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", "itemdrop".toUpperCase())));
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", "itemdrop".toUpperCase())));
			}
			break;
		case "hologram":
			if (Database.toggleHologram(player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOn").replace("%s", "hologram".toUpperCase())));
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Messages.Toggle.ToggleOff").replace("%s", "hologram".toUpperCase())));
			}
			break;
		}
	}

}
