package com.loohp.interactionvisualizer.Managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;

public class PlayerLocationManager {
	
	public static boolean hasPlayerNearby(Location location) {
		World world = location.getWorld();
		int chunkX = location.getBlockX() >> 4;
		int chunkZ = location.getBlockZ() >> 4;
		ChunkPosition chunkpos = new ChunkPosition(world, chunkX, chunkZ);
		if (!chunkpos.isLoaded()) {
			return false;
		}
		
		Set<ChunkPosition> nearby = new HashSet<>();
		nearby.add(new ChunkPosition(world, chunkX + 1, chunkZ + 1));
		nearby.add(new ChunkPosition(world, chunkX + 1, chunkZ));
		nearby.add(new ChunkPosition(world, chunkX + 1, chunkZ - 1));
		nearby.add(new ChunkPosition(world, chunkX, chunkZ + 1));
		nearby.add(new ChunkPosition(world, chunkX, chunkZ));
		nearby.add(new ChunkPosition(world, chunkX, chunkZ - 1));
		nearby.add(new ChunkPosition(world, chunkX - 1, chunkZ + 1));
		nearby.add(new ChunkPosition(world, chunkX - 1, chunkZ));
		nearby.add(new ChunkPosition(world, chunkX - 1, chunkZ - 1));

		return nearby.contains(chunkpos);
	}
	
	public static Location getPlayerLocation(Player player) {
		return player.getLocation().clone();
	}
	
	public static Location getPlayerEyeLocation(Player player) {
		return player.getEyeLocation().clone();
	}

}
