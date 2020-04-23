package com.loohp.interactionvisualizer.Managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkUpdateQueue;

public class TileEntityManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static ChunkUpdateQueue chunks = new ChunkUpdateQueue();
	private static Queue<BlockState> states = new LinkedList<BlockState>();
	private static HashMap<String, List<Block>> current = new HashMap<String, List<Block>>();
	private static HashMap<String, List<Block>> upcomming = new HashMap<String, List<Block>>();
	
	private static Integer tileEntityChunkPerTick = InteractionVisualizer.tileEntityChunkPerTick;
	private static Integer tileEntityStatePerTick = InteractionVisualizer.tileEntityStatePerTick;
	
	private static Boolean tileEntities = InteractionVisualizer.tileEntities;
	
	public static List<Block> getTileEntites(String type) {
		List<Block> list = current.get(type);
		return list != null ? list : new LinkedList<Block>();
	}
	
	public static void run() {
		if (!tileEntities) {
			return;
		}
		upcomming.put("blastfurnace", new LinkedList<Block>());
		upcomming.put("brewingstand", new LinkedList<Block>());
		upcomming.put("furnace", new LinkedList<Block>());
		upcomming.put("smoker", new LinkedList<Block>());
		upcomming.put("beacon", new LinkedList<Block>());
		upcomming.put("jukebox", new LinkedList<Block>());
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> getAllChunks());
	}
	
	private static void getAllChunks() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Location location = player.getLocation().clone();
			World world = location.getWorld();
			int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
			int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
			
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ - 1));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ - 1));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ - 1));
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> loadBlockStates(), 1);
	}
	
	private static void loadBlockStates() {
		int count = 0;
		while (!chunks.isEmpty()) {
			ChunkPosition chunkpos = chunks.poll();
			if (chunkpos.isLoaded()) {
				count++;
				Chunk chunk = chunkpos.getChunk();
				states.addAll(Arrays.asList(chunk.getTileEntities()));
			}
			if (count >= tileEntityChunkPerTick) {
				break;
			}
		}
		if (chunks.isEmpty()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadTileEntities(), 1);
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadBlockStates(), 1);
		}
	}

	private static void loadTileEntities() {
		int count = 0;
		while (!states.isEmpty()) {
			BlockState state = states.poll();
			count++;
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
			if (count >= tileEntityStatePerTick) {
				break;
			}
		}
		if (states.isEmpty()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				current = upcomming;
				upcomming = new HashMap<String, List<Block>>();
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
			}, 1);
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadTileEntities(), 1);
		}
	}
	
}
