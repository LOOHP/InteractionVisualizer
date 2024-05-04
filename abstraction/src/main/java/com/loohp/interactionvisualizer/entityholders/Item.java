/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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
import com.loohp.interactionvisualizer.utils.ComponentFont;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class Item extends VisualizerEntity {

    private ItemStack item;
    private boolean hasGravity;
    private boolean isGlowing;
    private int pickupDelay;
    private Component customName;
    private boolean customNameVisible;
    private Vector velocity;

    public Item(Location location) {
        super(location);
        this.item = new ItemStack(Material.STONE);
        this.hasGravity = false;
        this.pickupDelay = 0;
        this.customName = null;
        this.customNameVisible = false;
        this.isGlowing = false;
        this.velocity = new Vector(0.0, 0.0, 0.0);
    }

    @Override
    public int cacheCode() {
        int prime = 17;
        int result = super.cacheCode();
        result = prime * result + ((hasGravity) ? 5351 : 8923);
        result = prime * result + pickupDelay;
        result = prime * result + ((hasGravity) ? 6719 : 2753);
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + ((customName == null) ? 0 : customName.hashCode());
        result = prime * result + ((customNameVisible) ? 6199 : 8647);
        result = prime * result + ((velocity == null) ? 0 : velocity.hashCode());
        return result;
    }

    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName == null ? null : ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(customName));
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public void setGlowing(boolean bool) {
        this.isGlowing = bool;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    public void setCustomNameVisible(boolean bool) {
        this.customNameVisible = bool;
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

    public ItemStack getItemStack() {
        return item.clone();
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

    public void setGravity(boolean bool) {
        this.hasGravity = bool;
    }

    public boolean hasGravity() {
        return hasGravity;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector vector) {
        this.velocity = vector.clone();
    }

    public int getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(int pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<?> getDataWatchers() {
        return NMSWrapper.getInstance().getWatchableCollection(this);
    }

    @Override
    public double getHeight() {
        return 0.25;
    }

}
