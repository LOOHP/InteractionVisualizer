package com.loohp.interactionvisualizer.Debug;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class Debug implements Listener {
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
			event.getPlayer().sendMessage(ChatColor.BLUE + "InteractionVisualizer " + InteractionVisualizer.plugin.getDescription().getVersion() + " is running!");
		}
	}

}
