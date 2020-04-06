package com.loohp.interactionvisualizer.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Database.Database;

public class Events implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();	
		if (!Database.playerExists(player)) {
			Database.createPlayer(player);
		}
		Database.loadPlayer(player);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		InteractionVisualizer.itemStand.remove(player);
		InteractionVisualizer.itemDrop.remove(player);
		InteractionVisualizer.holograms.remove(player);
	}

}
