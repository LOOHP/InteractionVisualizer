package com.loohp.interactionvisualizer.Entity;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Utils.EntityCreator;

public class Item {
	
	int id;
	UUID uuid;
	Location location;
	ItemStack item;
	boolean hasGravity;
	boolean isGlowing;
	int pickupDelay;
	String customName;
	boolean custonNameVisible;
	Vector velocity;
	boolean lock;
	
	public Item(Location location) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.location = location;
		this.item = new ItemStack(Material.STONE);
		this.hasGravity = false;
		this.pickupDelay = 0;
		this.customName = "";
		this.custonNameVisible = false;
		this.isGlowing = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
		this.lock = false;
	}
	
	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getCustomName() {
		return customName;
	}
	
	public void setGlowing(boolean bool) {
		this.isGlowing = bool;
	}	
	public boolean isGlowing() {
		return isGlowing;
	}
	
	public void setCustomNameVisible(boolean bool) {
		this.custonNameVisible = bool;
	}	
	public boolean isCustomNameVisible() {
		return custonNameVisible;
	}
	
	public EntityType getType() {
		return EntityType.DROPPED_ITEM;
	}
	
	public void setRotation(float yaw, float pitch) {
		teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
	}
	
	public World getWorld() {
		return location.getWorld();
	}
	
	public void teleport(Location location) {
		setLocation(location);
	}
	
	public void teleport(World world, double x, double y, double z) {
		setLocation(new Location(world, x, y, z, location.getYaw(), location.getPitch()));
	}
	
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		setLocation(new Location(world, x, y, z, yaw, pitch));
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	public Location getLocation() {
		return location;
	}
	
	public void setItemStack(ItemStack item) {
		if (lock) {
			return;
		}
		if (item.getType().equals(Material.AIR)) {
			this.item = new ItemStack(Material.STONE);
			return;
		}
		this.item = item.clone();
	}
	public ItemStack getItemStack() {
		return item;
	}
	
	public void setGravity(boolean bool) {
		this.hasGravity = bool;
	}
	
	public boolean getGravity() {
		return hasGravity;
	}
	
	public void setVelocity(Vector vector) {
		this.velocity = vector.clone();
	}
	
	public Vector getVelocity() {
		return velocity;
	}
	
	public void setPickupDelay(int pickupDelay) {
		this.pickupDelay = pickupDelay;
	}
	public int getPickupDelay() {
		return pickupDelay;
	}
	
	public void setLocked(boolean bool) {
		this.lock = bool;
	}	
	public boolean isLocked() {
		return lock;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getEntityId() {
		return id;
	}
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		org.bukkit.entity.Item itemEntity = (org.bukkit.entity.Item) EntityCreator.create(location, EntityType.DROPPED_ITEM);
		itemEntity.setItemStack(item);
		itemEntity.setPickupDelay(pickupDelay);
		itemEntity.setGlowing(isGlowing);
		itemEntity.setCustomName(customName);
		itemEntity.setCustomNameVisible(custonNameVisible);
		itemEntity.setGravity(hasGravity);
		itemEntity.setVelocity(velocity);
		itemEntity.remove();
		return WrappedDataWatcher.getEntityWatcher(itemEntity);
	}
	
	public void remove() {
		
	}

}
