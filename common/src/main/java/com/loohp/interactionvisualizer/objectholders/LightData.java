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

package com.loohp.interactionvisualizer.objectholders;

import com.loohp.interactionvisualizer.utils.LocationUtils;
import org.bukkit.Location;

import java.util.Objects;

public class LightData {

    public static LightData of(Location location) {
        return of(location, 0, null);
    }

    public static LightData of(Location location, LightType lightType) {
        return of(location, 0, lightType);
    }

    public static LightData of(Location location, int lightlevel, LightType lightType) {
        return new LightData(location, lightlevel, lightType);
    }

    private final Location location;
    private final int lightLevel;
    private final LightType lightType;

    private LightData(Location location, int lightlevel, LightType lightType) {
        this.location = location;
        this.lightType = lightType;
        this.lightLevel = lightlevel;
    }

    public Location getLocation() {
        return location;
    }

    public LightType getLightType() {
        return lightType;
    }

    public boolean hasLightType() {
        return lightType != null;
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public boolean isLocationLoaded() {
        return LocationUtils.isLoaded(location);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightData lightData = (LightData) o;
        return lightLevel == lightData.lightLevel && Objects.equals(location, lightData.location) && lightType == lightData.lightType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, lightLevel, lightType);
    }
}