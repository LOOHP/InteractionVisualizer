package com.loohp.interactionvisualizer.Managers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolders.VisualizerEntity;
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
		for (int z = -InteractionVisualizer.tileEntityCheckingRange; z <= InteractionVisualizer.tileEntityCheckingRange; z++) {
			for (int x = -InteractionVisualizer.tileEntityCheckingRange; x <= InteractionVisualizer.tileEntityCheckingRange; x++) {
				nearby.add(new ChunkPosition(world, chunkX + x, chunkZ + z));
			}
		}

		for (Player player : world.getPlayers()) {
			Location playerLocation = player.getLocation();
			ChunkPosition playerChunk = new ChunkPosition(world, playerLocation.getBlockX() >> 4, playerLocation.getBlockZ() >> 4);
			if (nearby.contains(playerChunk)) {
				return true;
			}
		}
		return false;
	}
	
	public static Location getPlayerLocation(Player player) {
		return player.getLocation();
	}
	
	public static Location getPlayerEyeLocation(Player player) {
		return player.getEyeLocation();
	}
	
	public static Collection<Player> filterOutOfRange(Collection<Player> players, VisualizerEntity entity) {
		return filterOutOfRange(players, entity.getLocation());
	}
	
	public static Collection<Player> filterOutOfRange(Collection<Player> players, Entity entity) {
		return filterOutOfRange(players, entity.getLocation());
	}
	
	public static Collection<Player> filterOutOfRange(Collection<Player> players, Location location) {
		Collection<Player> playersInRange = new HashSet<>();
		int range = InteractionVisualizer.playerTrackingRange.getOrDefault(location.getWorld(), 64);
		range *= range;
		for (Player player : players) {
			Location playerLocation = PlayerLocationManager.getPlayerLocation(player);
			if (playerLocation.getWorld().equals(location.getWorld()) && (playerLocation.distanceSquared(location) <= range)) {
				playersInRange.add(player);
			}
		}
		return playersInRange;
	}

}
