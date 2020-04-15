package com.loohp.interactionvisualizer.EntityHolder;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Utils.EntityCreator;

public class ItemFrame {
	
	int id;
	UUID uuid;
	Location location;
	ItemStack item;
	BlockFace facing;
	
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
		return facing;
	}
	
	public float getYaw() {
		switch (facing) {
		case DOWN:
			return 0.0F;
		case EAST:
			return -90.0F;
		case NORTH:
			return 180.0F;
		case SOUTH:
			return 0.0F;
		case UP:
			return 0.0F;
		case WEST:
			return 90.0F;
		default:
			return 0.0F;	
		}
	}
	
	public float getPitch() {
		switch (facing) {
		case DOWN:
			return 90.0F;
		case EAST:
			return 0.0F;
		case NORTH:
			return 0.0F;
		case SOUTH:
			return 0.0F;
		case UP:
			return -90.0F;
		case WEST:
			return 0.0F;
		default:
			return 0.0F;	
		}
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
		org.bukkit.entity.ItemFrame itemframe = (org.bukkit.entity.ItemFrame) EntityCreator.create(new Location(location.getWorld(), 0, 0, 0), EntityType.ITEM_FRAME);
		itemframe.setItem(item, false);
		itemframe.setFacingDirection(facing);
		itemframe.remove();
		return WrappedDataWatcher.getEntityWatcher(itemframe);
	}
	
	public void remove() {
		
	}

}
