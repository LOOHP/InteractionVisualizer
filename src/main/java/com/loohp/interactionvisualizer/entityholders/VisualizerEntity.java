package com.loohp.interactionvisualizer.entityholders;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public abstract class VisualizerEntity implements IVisualizerEntity {
	
	private static final Random RANDOM = new Random();
	public static final int ID_OFFSET = 1000000;
	public static final int ID_BOUND = Integer.MAX_VALUE - ID_OFFSET;

	protected int id;
	protected UUID uuid;
	protected Location location;
	protected boolean lock;
	protected boolean isSilent;

	public VisualizerEntity(Location location) {
		this.id = RANDOM.nextInt(ID_BOUND) + ID_OFFSET;
		this.uuid = UUID.randomUUID();
		this.location = location.clone();
		this.lock = false;
		this.isSilent = false;
	}

	public int cacheCode() {
		int prime = 17;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((lock) ? 1531 : 4021);
		result = prime * result + ((isSilent) ? 3301 : 4507);
		return result;
	}

	@Override
	public void setRotation(float yaw, float pitch) {
		if (lock) {
			return;
		}
		teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
	}

	@Override
	public World getWorld() {
		return location.getWorld();
	}

	@Override
	public void teleport(Location location) {
		this.location = location.clone();
	}

	@Override
	public void teleport(World world, double x, double y, double z) {
		this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
	}

	@Override
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		this.location = new Location(world, x, y, z, yaw, pitch);
	}

	@Override
	public void setLocation(Location location) {
		this.location = location.clone();
	}

	@Override
	public Location getLocation() {
		return location.clone();
	}

	@Override
	public void setSilent(boolean bool) {
		this.isSilent = bool;
	}

	@Override
	public boolean isSilent() {
		return isSilent;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public int getEntityId() {
		return id;
	}

	@Override
	public void setLocked(boolean bool) {
		this.lock = bool;
	}

	@Override
	public boolean isLocked() {
		return lock;
	}
	
	@Override
	public abstract double getHeight();

	@Override
	public abstract WrappedDataWatcher getWrappedDataWatcher();

}
