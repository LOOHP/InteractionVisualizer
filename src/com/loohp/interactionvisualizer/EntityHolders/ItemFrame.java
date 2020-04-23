package com.loohp.interactionvisualizer.EntityHolders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Protocol.WatchableCollection;

public class ItemFrame extends VisualizerEntity {
	
	ItemStack item;
	BlockFace facing;
	int framerotation;
	
	public ItemFrame(Location location) {
		super(location);
		this.item = new ItemStack(Material.AIR);
		this.facing = BlockFace.SOUTH;
		this.framerotation = 0;
	}
	
	public EntityType getType() {
		return EntityType.ITEM_FRAME;
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
	
	public void setFrameRotation(int rotation) {
		if (rotation >= 0 && rotation < 8) {
			this.framerotation = rotation;
		} else {
			Bukkit.getLogger().severe("Item Frame Rotation must be between 0 and 7");
		}
	}
	
	public int getFrameRotation() {
		return framerotation;
	}
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		return WatchableCollection.getWatchableCollection(this);
	}

}
