package com.loohp.interactionvisualizer.Managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.NMS.NMS;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity.TileEntityType;

public class TileEntityManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static TileEntityType[] tileEntityTypes = TileEntityType.values();
	private static HashMap<TileEntityType, List<Block>> current = new HashMap<>();
	private static HashMap<TileEntityType, List<Block>> upcomming = new HashMap<>();
	
	public static List<Block> getTileEntites(TileEntityType type) {
		List<Block> list = current.get(type);
		return list != null ? list : new LinkedList<>();
	}
	
	public static void run() {
		for (TileEntityType type : tileEntityTypes) {
			upcomming.put(type, new LinkedList<>());
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> getAllChunks());
	}
	
	private static void getAllChunks() {
		Set<ChunkPosition> chunks = new HashSet<>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			Location location = player.getLocation().clone();
			World world = location.getWorld();
			int chunkX = location.getBlockX() >> 4;
			int chunkZ = location.getBlockZ() >> 4;
			
			for (int z = -InteractionVisualizer.tileEntityCheckingRange; z <= InteractionVisualizer.tileEntityCheckingRange; z++) {
				for (int x = -InteractionVisualizer.tileEntityCheckingRange; x <= InteractionVisualizer.tileEntityCheckingRange; x++) {
					chunks.add(new ChunkPosition(world, chunkX + x, chunkZ + z));
				}
			}
		}
		
		if (plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> loadTileEntityPositions(chunks), 1);
		}
	}
	
	private static void loadTileEntityPositions(Collection<ChunkPosition> chunks) {
		Queue<ChunkPosition> queue = new LinkedList<>(chunks);
		List<TileEntity> tileEntities = new ArrayList<>();
		new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = 0; i < InteractionVisualizer.tileEntityChunkPerTick; i++) {
					ChunkPosition chunk = queue.poll();
					if (chunk == null) {
						this.cancel();
						Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> loadTileEntities(tileEntities), 1);
						return;
					}
					tileEntities.addAll(NMS.getInstance().getTileEntities(chunk, false));
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}

	private static void loadTileEntities(List<TileEntity> tileEntities) {
		for (TileEntity tile : tileEntities) {
			upcomming.get(tile.getType()).add(tile.getBlock());
		}
		if (plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				current = upcomming;
				upcomming = new HashMap<>();
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
			}, 1);
		}
	}
	
}
