package com.loohp.interactionvisualizer.Managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;

public class PlayerLocationManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static Set<ChunkPosition> current = new HashSet<ChunkPosition>();
	private static Set<ChunkPosition> upcomming = new HashSet<ChunkPosition>();
	private static HashMap<Player, Location> currentlocations = new HashMap<Player, Location>();
	private static HashMap<Player, Location> upcomminglocations = new HashMap<Player, Location>();
	
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
	
	public static Location getPlayerLocation(Player player) {
		Location location = currentlocations.get(player);
		return location != null ? location.clone() : new Location(InteractionVisualizer.defaultworld, -99999999, -99999999, -99999999);
	}
	
	public static void updateLocation() {
		Bukkit.getScheduler().runTask(plugin, () -> {		
			int count = 0;
			int size = Bukkit.getOnlinePlayers().size();
			int maxper = (int) Math.ceil((double) size / (double) 5);
			if (maxper > 10) {
				maxper = 10;
			}
			int delay = 1;
			for (Player eachPlayer : Bukkit.getOnlinePlayers()) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				UUID uuid = eachPlayer.getUniqueId();
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					Player player = Bukkit.getPlayer(uuid);
					if (player == null) {
						return;
					}					
					upcomminglocations.put(player, player.getLocation().clone());
				}, delay);
			}
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				currentlocations = upcomminglocations;
				upcomminglocations = new HashMap<Player, Location>();
				Bukkit.getScheduler().runTaskLater(plugin, () -> updateLocation(), 1);
			}, delay + 1);
		});
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
