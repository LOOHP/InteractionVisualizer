package com.loohp.interactionvisualizer.entityholders;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public interface IVisualizerEntity {

	public void setRotation(float yaw, float pitch);

	public World getWorld();

	public void teleport(Location location);

	public void teleport(World world, double x, double y, double z);

	public void teleport(World world, double x, double y, double z, float yaw, float pitch);

	public void setLocation(Location location);

	public Location getLocation();

	public void setSilent(boolean bool);

	public boolean isSilent();

	public UUID getUniqueId();

	public int getEntityId();

	public void setLocked(boolean bool);

	public boolean isLocked();
	
	public double getHeight();

	public WrappedDataWatcher getWrappedDataWatcher();

}
