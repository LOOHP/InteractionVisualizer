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
	private static Set<int[]> current = new HashSet<int[]>();
	private static Set<int[]> upcomming = new HashSet<int[]>();
	
	public static boolean hasPlayerNearby(Location location) {
		int x = (int) Math.floor((double) location.getBlockX() / 16.0);
		int z = (int) Math.floor((double) location.getBlockZ() / 16.0);
		int[] array = new int[]{x, z};
		synchronized (current) {
			if (current.stream().anyMatch(each -> Arrays.equals(each, array))) {
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
					int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
					int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
					
					upcomming.add(new int[]{chunkX + 1, chunkZ + 1});
					upcomming.add(new int[]{chunkX + 1, chunkZ});
					upcomming.add(new int[]{chunkX + 1, chunkZ - 1});
					upcomming.add(new int[]{chunkX, chunkZ + 1});
					upcomming.add(new int[]{chunkX, chunkZ});
					upcomming.add(new int[]{chunkX, chunkZ - 1});
					upcomming.add(new int[]{chunkX - 1, chunkZ + 1});
					upcomming.add(new int[]{chunkX - 1, chunkZ});
					upcomming.add(new int[]{chunkX - 1, chunkZ - 1});
				}
				synchronized (current) {
					current = upcomming;
				}
				upcomming = new HashSet<int[]>();
			} catch (Exception e) {
				upcomming = new HashSet<int[]>();
			} finally {
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> run(), 1);
			}
		});
	}

}
