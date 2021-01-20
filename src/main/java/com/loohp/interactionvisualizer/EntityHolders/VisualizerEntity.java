package com.loohp.interactionvisualizer.EntityHolders;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public abstract class VisualizerEntity {

	private int id;
	private UUID uuid;
	private Location location;
	protected boolean lock;
	private boolean isSilent;

	public VisualizerEntity(Location location) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
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

	public void setRotation(float yaw, float pitch) {
		if (lock) {
			return;
		}
		teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
	}

	public World getWorld() {
		return location.getWorld();
	}

	public void teleport(Location location) {
		this.location = location.clone();
	}

	public void teleport(World world, double x, double y, double z) {
		this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
	}

	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		this.location = new Location(world, x, y, z, yaw, pitch);
	}

	public void setLocation(Location location) {
		this.location = location.clone();
	}

	public Location getLocation() {
		return location.clone();
	}

	public void setSilent(boolean bool) {
		this.isSilent = bool;
	}

	public boolean isSilent() {
		return isSilent;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public int getEntityId() {
		return id;
	}

	public void setLocked(boolean bool) {
		this.lock = bool;
	}

	public boolean isLocked() {
		return lock;
	}
	
	public abstract double getHeight();

	public abstract WrappedDataWatcher getWrappedDataWatcher();

}
