package com.loohp.interactionvisualizer.Manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class PlayerRangeManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static Set<Object[]> current = new HashSet<Object[]>();
	private static Set<Object[]> upcomming = new HashSet<Object[]>();
	
	public static boolean hasPlayerNearby(Location location) {
		String world = location.getWorld().getUID().toString();
		int x = (int) Math.floor((double) location.getBlockX() / 16.0);
		int z = (int) Math.floor((double) location.getBlockZ() / 16.0);		
		Object[] array = new Object[]{world, x, z};
		synchronized (current) {
			if (current.stream().anyMatch(each -> Arrays.equals(each, array))) {
				if (location.getWorld().isChunkLoaded(x, z)) {
					return true;
				}
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
					String world = location.getWorld().getUID().toString();
					int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
					int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
					
					upcomming.add(new Object[]{world, chunkX + 1, chunkZ + 1});
					upcomming.add(new Object[]{world, chunkX + 1, chunkZ});
					upcomming.add(new Object[]{world, chunkX + 1, chunkZ - 1});
					upcomming.add(new Object[]{world, chunkX, chunkZ + 1});
					upcomming.add(new Object[]{world, chunkX, chunkZ});
					upcomming.add(new Object[]{world, chunkX, chunkZ - 1});
					upcomming.add(new Object[]{world, chunkX - 1, chunkZ + 1});
					upcomming.add(new Object[]{world, chunkX - 1, chunkZ});
					upcomming.add(new Object[]{world, chunkX - 1, chunkZ - 1});
				}
				synchronized (current) {
					current = upcomming;
				}
				upcomming = new HashSet<Object[]>();
			} catch (Exception e) {
				upcomming = new HashSet<Object[]>();
			} finally {
				if (plugin.isEnabled()) {
					Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
				}
			}
		});
	}

}
