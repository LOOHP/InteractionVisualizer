package com.loohp.interactionvisualizer.Manager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
		if (current.stream().anyMatch(each -> Arrays.equals(each, array))) {
			return true;
		}
		return false;
	}
	
	public static void run() {
		int count = 0;
		int delay = 1;
		for (Player player : Bukkit.getOnlinePlayers()) {
			count++;
			if (count > 20) {
				count = 0;
				delay++;
			}
			UUID uuid = player.getUniqueId();
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (Bukkit.getPlayer(uuid) == null) {
					return;
				}
				int chunkX = player.getLocation().getChunk().getX();
				int chunkZ = player.getLocation().getChunk().getZ();
				
				upcomming.add(new int[]{chunkX + 1, chunkZ + 1});
				upcomming.add(new int[]{chunkX + 1, chunkZ});
				upcomming.add(new int[]{chunkX + 1, chunkZ - 1});
				upcomming.add(new int[]{chunkX, chunkZ + 1});
				upcomming.add(new int[]{chunkX, chunkZ});
				upcomming.add(new int[]{chunkX, chunkZ - 1});
				upcomming.add(new int[]{chunkX - 1, chunkZ + 1});
				upcomming.add(new int[]{chunkX - 1, chunkZ});
				upcomming.add(new int[]{chunkX - 1, chunkZ - 1});
			}, delay);
		}
		int next = 2 + delay;
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			current = upcomming;
			upcomming = new HashSet<int[]>();
			int nextupdate = ((20 - (next + 2)) > 0) ? (20 - (next + 2)) : 1;
			Bukkit.getScheduler().runTaskLater(plugin, () -> run(), nextupdate);
		}, next);
	}

}
