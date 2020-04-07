package com.loohp.interactionvisualizer.Entity;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.EntityCreator;

public class ItemFrame {
	
	int id;
	UUID uuid;
	Location location;
	ItemStack item;
	BlockFace facing;
	Optional<org.bukkit.entity.ItemFrame> snapshotEntity = Optional.empty();
	
	public ItemFrame(Location location) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.location = location;
		this.item = new ItemStack(Material.AIR);
		this.facing = BlockFace.SOUTH;
	}
	
	public EntityType getType() {
		return EntityType.ITEM_FRAME;
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
	
	public BlockFace getAttachedFace() {
		org.bukkit.entity.ItemFrame itemframe = null;
		if (snapshotEntity.isPresent()) {
			itemframe = snapshotEntity.get();
		} else {
			itemframe = (org.bukkit.entity.ItemFrame) EntityCreator.create(location, EntityType.ITEM_FRAME);
			snapshotEntity = Optional.of(itemframe);
		}
		itemframe.setItem(item, false);
		itemframe.setFacingDirection(facing);
		new BukkitRunnable() {
			public void run() {
				if (snapshotEntity.isPresent()) {
					snapshotEntity.get().remove();
					snapshotEntity = Optional.empty();
				}
			}
		}.runTaskLater(InteractionVisualizer.plugin, 1);
		return itemframe.getAttachedFace();
	}
	
	public Location getRealLocation() {
		org.bukkit.entity.ItemFrame itemframe = null;
		if (snapshotEntity.isPresent()) {
			itemframe = snapshotEntity.get();
		} else {
			itemframe = (org.bukkit.entity.ItemFrame) EntityCreator.create(location, EntityType.ITEM_FRAME);
			snapshotEntity = Optional.of(itemframe);
		}
		itemframe.setItem(item, false);
		itemframe.setFacingDirection(facing);
		new BukkitRunnable() {
			public void run() {
				if (snapshotEntity.isPresent()) {
					snapshotEntity.get().remove();
					snapshotEntity = Optional.empty();
				}
			}
		}.runTaskLater(InteractionVisualizer.plugin, 1);
		return itemframe.getLocation();
	}
	
	public void setItem(ItemStack item) {
		this.item = item.clone();
	}
	public ItemStack getItem() {
		return item;
	}
	
	public void setFacingDirection(BlockFace facing) {
		this.facing = facing;
	}
	
	public BlockFace getFacingDirection() {
		return facing;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getEntityId() {
		return id;
	}
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		org.bukkit.entity.ItemFrame itemframe = null;
		if (snapshotEntity.isPresent()) {
			itemframe = snapshotEntity.get();
		} else {
			itemframe = (org.bukkit.entity.ItemFrame) EntityCreator.create(location, EntityType.ITEM_FRAME);
			snapshotEntity = Optional.of(itemframe);
		}
		itemframe.setItem(item, false);
		itemframe.setFacingDirection(facing);
		new BukkitRunnable() {
			public void run() {
				if (snapshotEntity.isPresent()) {
					snapshotEntity.get().remove();
					snapshotEntity = Optional.empty();
				}
			}
		}.runTaskLater(InteractionVisualizer.plugin, 1);
		return WrappedDataWatcher.getEntityWatcher(itemframe);
	}
	
	public void remove() {
		
	}

}
