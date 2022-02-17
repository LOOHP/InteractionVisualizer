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

package com.loohp.interactionvisualizer.objectholders;

import com.loohp.interactionvisualizer.utils.LocationUtils;
import org.bukkit.Location;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lightLevel;
        result = prime * result + ((lightType == null) ? 0 : lightType.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LightData other = (LightData) obj;
        if (lightLevel != other.lightLevel) {
            return false;
        }
        if (lightType != other.lightType) {
            return false;
        }
        if (location == null) {
            return other.location == null;
        } else {
            return location.equals(other.location);
        }
    }

}