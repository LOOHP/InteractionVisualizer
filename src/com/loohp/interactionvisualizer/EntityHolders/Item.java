package com.loohp.interactionvisualizer.EntityHolders;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Protocol.WatchableCollection;

public class Item extends VisualizerEntity {
	
	ItemStack item;
	boolean hasGravity;
	boolean isGlowing;
	int pickupDelay;
	String customName;
	boolean custonNameVisible;
	Vector velocity;
	
	public Item(Location location, int id, UUID uuid) {
		super(location, id, uuid);
		this.item = new ItemStack(Material.STONE);
		this.hasGravity = false;
		this.pickupDelay = 0;
		this.customName = "";
		this.custonNameVisible = false;
		this.isGlowing = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
	}
	
	public Item(Location location) {
		super(location);
		this.item = new ItemStack(Material.STONE);
		this.hasGravity = false;
		this.pickupDelay = 0;
		this.customName = "";
		this.custonNameVisible = false;
		this.isGlowing = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
	}
	
	public Item deepClone() {
		Item newItem = new Item(location, id, uuid);
		newItem.setSilent(isSilent);
		newItem.setGravity(hasGravity);
		newItem.setGlowing(isGlowing);
		newItem.setPickupDelay(pickupDelay);
		newItem.setItemStack(item);
		newItem.setCustomName(customName);
		newItem.setCustomNameVisible(custonNameVisible);
		newItem.setVelocity(velocity);
		newItem.setLocked(lock);
		return newItem;
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
	
	public void setItemStack(ItemStack item, boolean force) {
		if (lock && !force) {
			return;
		}
		if (item.getType().equals(Material.AIR)) {
			this.item = new ItemStack(Material.STONE);
			return;
		}
		this.item = item.clone();
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
		return item.clone();
	}
	
	public void setGravity(boolean bool) {
		this.hasGravity = bool;
	}
	
	public boolean hasGravity() {
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
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		return WatchableCollection.getWatchableCollection(this);
	}

}
