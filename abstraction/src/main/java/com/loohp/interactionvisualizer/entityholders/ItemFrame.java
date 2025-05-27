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

package com.loohp.interactionvisualizer.entityholders;

import com.loohp.interactionvisualizer.nms.NMSWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemFrame extends VisualizerEntity {

    private ItemStack item;
    private BlockFace facing;
    private int framerotation;

    public ItemFrame(Location location) {
        super(location);
        this.item = new ItemStack(Material.AIR);
        this.facing = BlockFace.SOUTH;
        this.framerotation = 0;
    }

    @Override
    public int cacheCode() {
        int prime = 17;
        int result = super.cacheCode();
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + ((facing == null) ? 0 : facing.hashCode());
        result = prime * result + framerotation;
        return result;
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

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    public BlockFace getFacingDirection() {
        return facing;
    }

    public void setFacingDirection(BlockFace facing) {
        this.facing = facing;
    }

    public int getFrameRotation() {
        return framerotation;
    }

    public void setFrameRotation(int rotation) {
        if (rotation >= 0 && rotation < 8) {
            this.framerotation = rotation;
        } else {
            Bukkit.getLogger().severe("Item Frame Rotation must be between 0 and 7");
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<?> getDataWatchers() {
        return NMSWrapper.getInstance().getWatchableCollection(this);
    }

    @Override
    public double getHeight() {
        return 0.75;
    }

}
