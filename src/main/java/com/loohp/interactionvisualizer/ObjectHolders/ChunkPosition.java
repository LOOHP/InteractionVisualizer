package com.loohp.interactionvisualizer.objectholders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkPosition {
	
	private UUID world;
	private int x;
	private int z;
	
	public ChunkPosition(World world, int chunkX, int chunkZ) {
		this.world = world.getUID();
		this.x = chunkX;
		this.z = chunkZ;
	}
	
	public ChunkPosition(Location location) {
		this(location.getWorld(), (int) Math.floor((double) location.getBlockX() / 16.0), (int) Math.floor((double) location.getBlockZ() / 16.0));
	}
	
	public World getWorld() {
		return Bukkit.getWorld(world);
	}
	
	public UUID getWorldUID() {
		return world;
	}
	
	public int getChunkX() {
		return x;
	}
	
	public int getChunkZ() {
		return z;
	}
	
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ChunkPosition)) {
			return false;
		}
		if (hashCode() == object.hashCode()) {
			return true;
		}
		return false;
	}
	
	@Override
    public int hashCode() {
		int prime = 17;
        int result = 0;
        result = prime * result + world.hashCode();
        result = prime * result + x;
        result = prime * result + z;
        return result;
	}
	
	public boolean isLoaded() {
		World world = getWorld();
		if (world == null) {
			return false;
		}
		return world.isChunkLoaded(x, z);
	}
	
	public Chunk getChunk() {
		return getWorld().getChunkAt(x, z);
	}

}
