package com.loohp.interactionvisualizer.Holder;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public class VisualizerEntity {
	
	int id;
	UUID uuid;
	Location location;
	boolean lock;
	boolean isSilent;
	
	public VisualizerEntity(Location location) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.location = location.clone();
		this.lock = false;
		this.isSilent = false;
	}
	
	public VisualizerEntity(Location location, int id, UUID uuid) {
		this.id = id;
		this.uuid = uuid;
		this.location = location.clone();
		this.lock = false;
		this.isSilent = false;
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

}
