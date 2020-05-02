package com.loohp.interactionvisualizer.Managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;

import com.loohp.interactionvisualizer.InteractionVisualizer;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightManager {
	
	public static ConcurrentHashMap<Location, Integer> blocklights = new ConcurrentHashMap<Location, Integer>();
	public static ConcurrentHashMap<Location, Integer> skylights = new ConcurrentHashMap<Location, Integer>();
	public static ConcurrentLinkedQueue<Location> deletequeue = new ConcurrentLinkedQueue<Location>();
	
	public static void createLight(Location location, int lightlevel, LightType lightType) {
		switch (lightType) {
		case BLOCK:
			blocklights.put(location, lightlevel);
			break;
		case SKY:
			skylights.put(location, lightlevel);
			break;
		}
	}
	
	public static void deleteLight(Location location) {
		blocklights.remove(location);
		skylights.remove(location);
		deletequeue.add(location);
	}
	
	public static int run() {
		return Bukkit.getScheduler().runTaskTimer(InteractionVisualizer.plugin, () -> {
			boolean changed = false;
			HashMap<Location, Integer> locations = new HashMap<Location, Integer>();
			if (!deletequeue.isEmpty()) {
				changed = true;
			}
			while (!deletequeue.isEmpty()) {
				Location location = deletequeue.poll();
				if (location != null) {
					if (location.getWorld().getEnvironment().equals(Environment.NORMAL)) {
						LightAPI.deleteLight(location, LightType.SKY, false);
					}
					LightAPI.deleteLight(location, LightType.BLOCK, false);
					locations.put(location, 14);
				}
			}
			if (!skylights.isEmpty()) {
				changed = true;
			}
			for (Entry<Location, Integer> entry : skylights.entrySet()) {
				Location location = entry.getKey();
				if (location.getWorld().getEnvironment().equals(Environment.NORMAL)) {
					int lightlevel = entry.getValue();
					LightAPI.createLight(location, LightType.SKY, lightlevel, false);
					locations.put(location, lightlevel);
				}
			}
			if (!blocklights.isEmpty()) {
				changed = true;
			}
			for (Entry<Location, Integer> entry : blocklights.entrySet()) {
				Location location = entry.getKey();
				int lightlevel = entry.getValue();
				LightAPI.createLight(location, LightType.BLOCK, lightlevel, false);
				locations.put(location, lightlevel);
			}
			if (changed) {
				HashSet<ChunkInfo> blockinfos = new HashSet<ChunkInfo>();
				HashSet<ChunkInfo> skyinfos = new HashSet<ChunkInfo>();
				for (Entry<Location, Integer> entry : locations.entrySet()) {
					skyinfos.addAll(LightAPI.collectChunks(entry.getKey(), LightType.SKY, entry.getValue()));
					blockinfos.addAll(LightAPI.collectChunks(entry.getKey(), LightType.BLOCK, entry.getValue()));
				}
				for (ChunkInfo info : skyinfos) {
					LightAPI.updateChunk(info, LightType.SKY);
				}
				for (ChunkInfo info : blockinfos) {
					LightAPI.updateChunk(info, LightType.BLOCK);
				}
			}
			skylights.clear();
			blocklights.clear();
		}, 0, 10).getTaskId();
	}

}
