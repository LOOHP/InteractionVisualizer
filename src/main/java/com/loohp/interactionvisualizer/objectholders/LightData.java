package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.Location;

import com.loohp.interactionvisualizer.utils.LocationUtils;

public class LightData {
	
	private Location location;
	private int lightLevel;
	private LightType lightType;
	
	public static LightData of(Location location) {
		return of(location, 0, null);
	}
	
	public static LightData of(Location location, LightType lightType) {
		return of(location, 0, lightType);
	}
	
	public static LightData of(Location location, int lightlevel, LightType lightType) {
		return new LightData(location, lightlevel, lightType);
	}
	
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
			if (other.location != null) {
				return false;
			}
		} else if (!location.equals(other.location)) {
			return false;
		}
		return true;
	}
}