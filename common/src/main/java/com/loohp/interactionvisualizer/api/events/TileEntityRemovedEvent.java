/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

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
