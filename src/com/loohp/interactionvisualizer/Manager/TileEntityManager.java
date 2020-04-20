package com.loohp.interactionvisualizer.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Blocks.FurnaceDisplay;

public class TileEntityManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static Set<Object[]> chunks = new HashSet<Object[]>();
	private static HashMap<String, List<Block>> current = new HashMap<String, List<Block>>();
	private static HashMap<String, List<Block>> upcomming = new HashMap<String, List<Block>>();
	
	public static List<Block> getTileEntites(String type) {
		List<Block> list = current.get(type);
		return list != null ? list : new ArrayList<Block>();
	}
	
	public static void run() {
		upcomming.put("blastfurnace", new ArrayList<Block>());
		upcomming.put("brewingstand", new ArrayList<Block>());
		upcomming.put("furnace", new ArrayList<Block>());
		upcomming.put("smoker", new ArrayList<Block>());
		upcomming.put("beacon", new ArrayList<Block>());
		upcomming.put("jukebox", new ArrayList<Block>());
		chunks.clear();
		int next = 2 + getAllChunks();
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			int nextnext = 2 + loadTileEntities();
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				current = upcomming;
				upcomming = new HashMap<String, List<Block>>();
				int nextupdate = ((20 - (next + nextnext + 4)) > 0) ? (20 - (next + nextnext + 4)) : 1;
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), nextupdate);
			}, nextnext);
		}, next);
	}
	
	private static int getAllChunks() {
		int count = 0;
		int delay = 1;
		for (Player player : Bukkit.getOnlinePlayers()) {
			count++;
			if (count > 10) {
				count = 0;
				delay++;
			}
			UUID uuid = player.getUniqueId();
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (Bukkit.getPlayer(uuid) == null) {
					return;
				}
				Location location = player.getLocation().clone();
				String world = location.getWorld().getUID().toString();
				int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
				int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
				
				chunks.add(new Object[]{world, chunkX + 1, chunkZ + 1});
				chunks.add(new Object[]{world, chunkX + 1, chunkZ});
				chunks.add(new Object[]{world, chunkX + 1, chunkZ - 1});
				chunks.add(new Object[]{world, chunkX, chunkZ + 1});
				chunks.add(new Object[]{world, chunkX, chunkZ});
				chunks.add(new Object[]{world, chunkX, chunkZ - 1});
				chunks.add(new Object[]{world, chunkX - 1, chunkZ + 1});
				chunks.add(new Object[]{world, chunkX - 1, chunkZ});
				chunks.add(new Object[]{world, chunkX - 1, chunkZ - 1});
			}, delay);
		}
		return delay;
	}
	
	private static int loadTileEntities() {
		int count = 0;
		int delay = 1;
		for (Object[] chunkCoords : chunks) {
			count++;
			if (count > 9) {
				count = 0;
				delay++;
			}
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				World world = Bukkit.getWorld(UUID.fromString((String) chunkCoords[0]));
				if (world == null) {
					return;
				}
				int chunkX = (int) chunkCoords[1];
				int chunkZ = (int) chunkCoords[2];
				if (world.isChunkLoaded(chunkX, chunkZ)) {
					Chunk chunk = world.getChunkAt(chunkX, chunkZ);
					for (BlockState state : chunk.getTileEntities()) {
						Block block = state.getBlock();
						Material type = block.getType();
						if (type.toString().toUpperCase().equals("BLAST_FURNACE")) {
							upcomming.get("blastfurnace").add(block);
						} else if (type.toString().toUpperCase().equals("BREWING_STAND")) {
							upcomming.get("brewingstand").add(block);
						} else if (FurnaceDisplay.isFurnace(type)) {
							upcomming.get("furnace").add(block);
						} else if (type.toString().toUpperCase().equals("SMOKER")) {
							upcomming.get("smoker").add(block);
						} else if (type.toString().toUpperCase().equals("BEACON")) {
							upcomming.get("beacon").add(block);
						} else if (type.toString().toUpperCase().equals("JUKEBOX")) {
							upcomming.get("jukebox").add(block);
						}
					}
				}
			}, delay);
		}
		return delay;
	}
	
}
