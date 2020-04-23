package com.loohp.interactionvisualizer.Managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;

public class PlayerRangeManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static Set<ChunkPosition> current = new HashSet<ChunkPosition>();
	private static Set<ChunkPosition> upcomming = new HashSet<ChunkPosition>();
	
	public static boolean hasPlayerNearby(Location location) {
		World world = location.getWorld();
		int x = (int) Math.floor((double) location.getBlockX() / 16.0);
		int z = (int) Math.floor((double) location.getBlockZ() / 16.0);		
		ChunkPosition chunkpos = new ChunkPosition(world, x, z);
		if (!chunkpos.isLoaded()) {
			return false;
		}
		synchronized (current) {
			if (current.contains(chunkpos)) {
				return true;
			}
		}
		return false;
	}
	
	public static void run() {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (!player.isOnline()) {
						continue;
					}
					Location location = player.getLocation().clone();
					World world = location.getWorld();
					int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
					int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
					
					upcomming.add(new ChunkPosition(world, chunkX + 1, chunkZ + 1));
					upcomming.add(new ChunkPosition(world, chunkX + 1, chunkZ));
					upcomming.add(new ChunkPosition(world, chunkX + 1, chunkZ - 1));
					upcomming.add(new ChunkPosition(world, chunkX, chunkZ + 1));
					upcomming.add(new ChunkPosition(world, chunkX, chunkZ));
					upcomming.add(new ChunkPosition(world, chunkX, chunkZ - 1));
					upcomming.add(new ChunkPosition(world, chunkX - 1, chunkZ + 1));
					upcomming.add(new ChunkPosition(world, chunkX - 1, chunkZ));
					upcomming.add(new ChunkPosition(world, chunkX - 1, chunkZ - 1));
				}
				synchronized (current) {
					current = upcomming;
				}
				upcomming = new HashSet<ChunkPosition>();
			} catch (Exception e) {
				upcomming = new HashSet<ChunkPosition>();
			} finally {
				if (plugin.isEnabled()) {
					Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
				}
			}
		});
	}

}
