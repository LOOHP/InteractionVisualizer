package com.loohp.interactionvisualizer.Managers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.loohp.interactionvisualizer.InteractionVisualizer;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LightManager {
	
	public static class LightData {
		Location location;
		int LightLevel;
		LightType lightType;
		
		public static LightData of(Location location) {
			return of(location, 0, null);
		}
		
		public static LightData of(Location location, LightType lightType) {
			return of(location, 0, lightType);
		}
		
		public static LightData of(Location location, int lightlevel, LightType lightType) {
			return new LightData(location, lightlevel, lightType);
		}
		
		LightData (Location location, int lightlevel, LightType lightType) {
			this.location = location;
			this.lightType = lightType;
			this.LightLevel = lightlevel;
		}
		
		public Location getLocation() {
			return location;
		}
		
		public LightType getLightType() {
			return lightType;
		}
		
		public int getLightLevel() {
			return LightLevel;
		}
		
		@Override
		public int hashCode() {
			int hashCode = location.hashCode();
			if (lightType != null) {
				switch (lightType) {
				case BLOCK:
					hashCode *= 17;
					break;
				case SKY:
					hashCode *= 23;
					break;
				}
			}
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LightData) {
				return ((LightData) obj).hashCode() == this.hashCode();
			}
			return false;
		}
	}
	
	private static Set<LightData> addqueue = new HashSet<LightData>();
	private static Set<LightData> deletequeue = new HashSet<LightData>();
	
	public static void createLight(Location location, int lightlevel, LightType lightType) {
		addqueue.add(LightData.of(location, lightlevel, lightType));
	}
	
	public static void deleteLight(Location location) {
		addqueue.remove(LightData.of(location, LightType.BLOCK));
		addqueue.remove(LightData.of(location, LightType.SKY));
		deletequeue.add(LightData.of(location));
	}
	
	public static int run() {
		return Bukkit.getScheduler().runTaskTimer(InteractionVisualizer.plugin, () -> {
			boolean changed = false;
			
			Queue<LightData> updateQueue = new LinkedList<LightData>();
			
			Set<LightData> addqueue = LightManager.addqueue;
			Set<LightData> deletequeue = LightManager.deletequeue;
			
			LightManager.addqueue = new HashSet<LightData>();
			LightManager.deletequeue = new HashSet<LightData>();
			
			if (!deletequeue.isEmpty()) {
				changed = true;
			}
			Iterator<LightData> itr0 = deletequeue.iterator();
			while (itr0.hasNext()) {
				Location location = itr0.next().getLocation();
				if (LightAPI.isSupported(location.getWorld(), LightType.SKY)) {
					LightAPI.deleteLight(location, LightType.SKY, false);
				}
				LightAPI.deleteLight(location, LightType.BLOCK, false);
				updateQueue.add(LightData.of(location, 14, LightType.SKY));
				updateQueue.add(LightData.of(location, 14, LightType.BLOCK));
				itr0.remove();
			}
			
			if (!addqueue.isEmpty()) {
				changed = true;
			}
			Iterator<LightData> itr1 = addqueue.iterator();
			while (itr1.hasNext()) {
				LightData lightdata = itr1.next();
				Location location = lightdata.getLocation();
				int lightlevel = lightdata.getLightLevel();
				if (LightAPI.isSupported(location.getWorld(), lightdata.getLightType())) {
					LightAPI.createLight(location, lightdata.getLightType(), lightlevel, false);
					updateQueue.add(lightdata);
				}
				itr1.remove();
			}

			if (changed) {
				HashSet<ChunkInfo> blockinfos = new HashSet<ChunkInfo>();
				HashSet<ChunkInfo> skyinfos = new HashSet<ChunkInfo>();
				while (!updateQueue.isEmpty()) {
					LightData lightdata = updateQueue.poll();
					LightType lightType = lightdata.getLightType();
					switch (lightType) {
					case BLOCK:
						blockinfos.addAll(LightAPI.collectChunks(lightdata.getLocation(), lightType, lightdata.getLightLevel()));
						break;
					case SKY:
						skyinfos.addAll(LightAPI.collectChunks(lightdata.getLocation(), lightType, lightdata.getLightLevel()));
						break;
					}
				}
				for (ChunkInfo info : skyinfos) {
					LightAPI.updateChunk(info, LightType.SKY);
				}
				for (ChunkInfo info : blockinfos) {
					LightAPI.updateChunk(info, LightType.BLOCK);
				}
			}
		}, 0, InteractionVisualizer.lightUpdatePeriod).getTaskId();
	}

}
