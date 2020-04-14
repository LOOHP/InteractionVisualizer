package com.loohp.interactionvisualizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightManager {
	
	public static HashMap<Location, Integer> lights = new HashMap<Location, Integer>();
	public static Queue<Location> deletequeue = new LinkedList<Location>();
	
	public static void createLight(Location location, int lightlevel) {
		lights.put(location, lightlevel);
	}
	
	public static void deleteLight(Location location) {
		lights.remove(location);
		deletequeue.add(location);
	}
	
	public static int run() {
		return new BukkitRunnable() {
			public void run() {
				Set<Location> locations = new HashSet<Location>();
				while (!deletequeue.isEmpty()) {
					Location location = deletequeue.poll();
					LightAPI.deleteLight(location, LightType.BLOCK, false);
					locations.add(location);
				}
				for (Entry<Location, Integer> entry : lights.entrySet()) {
					Location location = entry.getKey();
					int lightlevel = entry.getValue();
					LightAPI.createLight(location, LightType.BLOCK, lightlevel, false);
					locations.add(location);
				}
				Queue<ChunkInfo> infos = new LinkedList<ChunkInfo>();
				for (Location location : locations) {
					infos.addAll(LightAPI.collectChunks(location, LightType.BLOCK, 15));					
				}
				while (!infos.isEmpty()) {
					ChunkInfo info = infos.poll();
					LightAPI.updateChunk(info, LightType.BLOCK);

				}
			}
		}.runTaskTimer(InteractionVisualizer.plugin, 0, 10).getTaskId();
	}

}
