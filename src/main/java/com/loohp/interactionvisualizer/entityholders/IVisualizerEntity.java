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
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public interface IVisualizerEntity {

    void setRotation(float yaw, float pitch);

    World getWorld();

    void teleport(Location location);

    void teleport(World world, double x, double y, double z);

    void teleport(World world, double x, double y, double z, float yaw, float pitch);

    Location getLocation();

    void setLocation(Location location);

    boolean isSilent();

    void setSilent(boolean bool);

    UUID getUniqueId();

    int getEntityId();

    boolean isLocked();

    void setLocked(boolean bool);

    double getHeight();

    WrappedDataWatcher updateAndGetWrappedDataWatcher();

}
