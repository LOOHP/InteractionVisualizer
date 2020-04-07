package com.loohp.interactionvisualizer.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Database.Database;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

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
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		if (InteractionVisualizer.chunksGoneOver.contains(chunk)) {
			return;
		}
		for (Entity entity : chunk.getEntities()) {
			if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
				LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
				for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
					LightAPI.updateChunk(info, LightType.BLOCK);
				}
				entity.remove();
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(InteractionVisualizer.plugin, () -> InteractionVisualizer.chunksGoneOver.add(chunk));
	}

}
