package com.loohp.interactionvisualizer.api.events;

import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a tile entity is broken by any means. (Does <b>NOT</b> include unloading)
 *
 * @author LOOHP
 */
public class TileEntityRemovedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final Block block;
    private final TileEntityType type;

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

    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
