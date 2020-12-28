package com.loohp.interactionvisualizer.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
			Database.loadPlayer(player, true);
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
	
	/*
	@EventHandler
	public void onLoad(ChunkLoadEvent event) {
		if (event.getChunk().getX() == 62 && event.getChunk().getZ() == 62) {
			Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()).get(0).sendMessage("Load " + event.getChunk().getX() + " " + event.getChunk().getZ());
		}
	}

	@EventHandler
	public void onUnload(ChunkUnloadEvent event) {
		if (event.getChunk().getX() == 62 && event.getChunk().getZ() == 61) {
			Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()).get(0).sendMessage("Unload " + event.getChunk().getX() + " " + event.getChunk().getZ());
		}
	}

	@EventHandler
	public void onlaodchunk(ChunkLoadEvent event) {
		Bukkit.getOnlinePlayers().stream().collect(Collectors.toList()).get(0).sendMessage("" + event.getChunk().getX() + " " + event.getChunk().getZ());
	}
	*/

}
