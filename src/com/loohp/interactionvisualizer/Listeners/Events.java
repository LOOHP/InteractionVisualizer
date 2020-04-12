package com.loohp.interactionvisualizer.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Database.Database;

public class Events implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		new BukkitRunnable() {
			public void run() {
				if (!Database.playerExists(player)) {
					Database.createPlayer(player);
				}
				Database.loadPlayer(player);
			}
		}.runTaskAsynchronously(InteractionVisualizer.plugin);
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		InteractionVisualizer.itemStand.remove(player);
		InteractionVisualizer.itemDrop.remove(player);
		InteractionVisualizer.holograms.remove(player);
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		InteractionVisualizer.chunkupdatequeue.add(event.getChunk());
	}

}
