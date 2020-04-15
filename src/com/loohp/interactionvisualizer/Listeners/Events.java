package com.loohp.interactionvisualizer.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Database.Database;

public class Events implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
			if (!Database.playerExists(player)) {
				Database.createPlayer(player);
			}
			Database.loadPlayer(player);
		});
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		InteractionVisualizer.itemStand.remove(player);
		InteractionVisualizer.itemDrop.remove(player);
		InteractionVisualizer.holograms.remove(player);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		for (Entity entity : event.getWorld().getNearbyEntities(new Location(event.getWorld(), 0, 0, 0), 2, 2, 2)) {
			if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
				entity.remove();
			}
		}
	}

}
