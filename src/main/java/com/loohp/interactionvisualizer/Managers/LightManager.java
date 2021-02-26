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
		
		private Location location;
		private int LightLevel;
		private LightType lightType;
		
		public static LightData of(Location location) {
			return of(location, 0, null);
		}
		
		public static LightData of(Location location, LightType lightType) {
			return of(location, 0, lightType);
		}
		
		public static LightData of(Location location, int lightlevel, LightType lightType) {
			return new LightData(location, lightlevel, lightType);
		}
		
		private LightData (Location location, int lightlevel, LightType lightType) {
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
		
		public boolean hasLightType() {
			return lightType != null;
		}
		
		public int getLightLevel() {
			return LightLevel;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + LightLevel;
			result = prime * result + ((lightType == null) ? 0 : lightType.hashCode());
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			LightData other = (LightData) obj;
			if (LightLevel != other.LightLevel) {
				return false;
			}
			if (lightType != other.lightType) {
				return false;
			}
			if (location == null) {
				if (other.location != null) {
					return false;
				}
			} else if (!location.equals(other.location)) {
				return false;
			}
			return true;
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
			
			Queue<LightData> updateQueue = new LinkedList<>();
			
			Set<LightData> addqueue = LightManager.addqueue;
			Set<LightData> deletequeue = LightManager.deletequeue;
			
			LightManager.addqueue = new HashSet<>();
			LightManager.deletequeue = new HashSet<>();
			
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
				HashSet<ChunkInfo> blockinfos = new HashSet<>();
				HashSet<ChunkInfo> skyinfos = new HashSet<>();
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
