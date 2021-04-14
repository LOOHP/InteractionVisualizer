package com.loohp.interactionvisualizer.api.events;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;

/**
 * Called when a tile entity is broken by any means. (Does <b>NOT</b> include unloading)
 * @author LOOHP
 *
 */
public class TileEntityRemovedEvent extends Event {
	
	private Block block;
	private TileEntityType type;
	
	public TileEntityRemovedEvent(Block block, TileEntityType type) {
		this.block = block;
		this.type = type;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public TileEntityType getTileEntityType() {
		return type;
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
