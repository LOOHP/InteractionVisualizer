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

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.protocol.WatchableCollection;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ArmorStand extends VisualizerEntity {

    private boolean hasArms;
    private boolean hasBasePlate;
    private boolean isMarker;
    private boolean hasGravity;
    private boolean isSmall;
    private boolean isInvulnerable;
    private boolean isVisible;
    private EulerAngle rightArmPose;
    private EulerAngle headPose;
    private ItemStack helmet;
    private ItemStack mainhand;
    private Component customName;
    private boolean customNameVisible;
    private Vector velocity;

    public ArmorStand(Location location) {
        super(location);
        this.hasArms = false;
        this.hasBasePlate = true;
        this.isMarker = false;
        this.hasGravity = true;
        this.isSmall = false;
        this.isInvulnerable = false;
        this.isVisible = true;
        this.rightArmPose = new EulerAngle(0.0, 0.0, 0.0);
        this.headPose = new EulerAngle(0.0, 0.0, 0.0);
        this.helmet = new ItemStack(Material.AIR);
        this.mainhand = new ItemStack(Material.AIR);
        this.customName = Component.empty();
        this.customNameVisible = false;
        this.velocity = new Vector(0.0, 0.0, 0.0);
    }

    @Override
    public int cacheCode() {
        int prime = 17;
        int result = super.cacheCode();
        result = prime * result + ((hasArms) ? 5351 : 8923);
        result = prime * result + ((hasBasePlate) ? 2861 : 6607);
        result = prime * result + ((isMarker) ? 9199 : 3163);
        result = prime * result + ((hasGravity) ? 6719 : 2753);
        result = prime * result + ((isSmall) ? 1373 : 3037);
        result = prime * result + ((isInvulnerable) ? 2111 : 2251);
        result = prime * result + ((isVisible) ? 6779 : 6679);
        result = prime * result + ((rightArmPose == null) ? 0 : rightArmPose.hashCode());
        result = prime * result + ((headPose == null) ? 0 : headPose.hashCode());
        result = prime * result + ((helmet == null) ? 0 : helmet.hashCode());
        result = prime * result + ((mainhand == null) ? 0 : mainhand.hashCode());
        result = prime * result + ((customName == null) ? 0 : customName.hashCode());
        result = prime * result + ((customNameVisible) ? 6199 : 8647);
        result = prime * result + ((velocity == null) ? 0 : velocity.hashCode());
        return result;
    }

    public Component getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(customName));
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    public void setCustomNameVisible(boolean bool) {
        this.customNameVisible = bool;
    }

    public void setArms(boolean bool) {
        this.hasArms = bool;
    }

    public boolean hasArms() {
        return hasArms;
    }

    public void setBasePlate(boolean bool) {
        this.hasBasePlate = bool;
    }

    public boolean hasBasePlate() {
        return hasBasePlate;
    }

    public boolean isMarker() {
        return isMarker;
    }

    public void setMarker(boolean bool) {
        this.isMarker = bool;
    }

    public void setGravity(boolean bool) {
        this.hasGravity = bool;
    }

    public boolean hasGravity() {
        return hasGravity;
    }

    public boolean isSmall() {
        return isSmall;
    }

    public void setSmall(boolean bool) {
        this.isSmall = bool;
    }

    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    public void setInvulnerable(boolean bool) {
        this.isInvulnerable = bool;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean bool) {
        this.isVisible = bool;
    }

    public EulerAngle getRightArmPose() {
        return rightArmPose;
    }

    public void setRightArmPose(EulerAngle angle) {
        if (lock) {
            return;
        }
        this.rightArmPose = angle;
    }

    public EulerAngle getHeadPose() {
        return headPose;
    }

    public void setHeadPose(EulerAngle angle) {
        if (lock) {
            return;
        }
        this.headPose = angle;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public void setHelmet(ItemStack item) {
        if (lock) {
            return;
        }
        this.helmet = item.clone();
    }

    public ItemStack getItemInMainHand() {
        return mainhand;
    }

    public void setItemInMainHand(ItemStack item) {
        if (lock) {
            return;
        }
        this.mainhand = item.clone();
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector vector) {
        this.velocity = vector.clone();
    }

    @Override
    public WrappedDataWatcher updateAndGetWrappedDataWatcher() {
        return dataWatcher = WatchableCollection.getWatchableCollection(this, dataWatcher);
    }

    @Override
    public double getHeight() {
        return isSmall ? 0.5 : 1.975;
    }

}
