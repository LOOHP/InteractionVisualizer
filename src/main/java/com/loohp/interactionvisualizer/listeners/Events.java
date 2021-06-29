package com.loohp.interactionvisualizer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class Events implements Listener {
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		int defaultRange = Bukkit.spigot().getConfig().getInt("world-settings.default.entity-tracking-range.players", 64);
		int range = Bukkit.spigot().getConfig().getInt("world-settings." + world.getName() + ".entity-tracking-range.players", defaultRange);
		InteractionVisualizer.playerTrackingRange.put(world, range);
		for (Entity entity : world.getNearbyEntities(new Location(event.getWorld(), 0, 0, 0), 2, 2, 2)) {
			if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
				entity.remove();
			}
		}
	}

}
