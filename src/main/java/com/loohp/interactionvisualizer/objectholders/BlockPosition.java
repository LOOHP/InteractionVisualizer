package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockPosition {
	
	private World world;
	private int x;
	private int y;
	private int z;
	
	public BlockPosition(World world, int x, int y, int z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public BlockPosition(Location location) {
		this(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public BlockPosition(Block block) {
		this(block.getWorld(), block.getX(), block.getY(), block.getZ());
	}

	public World getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	
	public Block getBlock() {
		return world.getBlockAt(x, y, z);
	}
	
	public Location getLocation() {
		return new Location(world, x, y, z);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((world == null) ? 0 : world.hashCode());
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
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
		BlockPosition other = (BlockPosition) obj;
		if (world == null) {
			if (other.world != null) {
				return false;
			}
		} else if (!world.equals(other.world)) {
			return false;
		}
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		if (z != other.z) {
			return false;
		}
		return true;
	}

}
