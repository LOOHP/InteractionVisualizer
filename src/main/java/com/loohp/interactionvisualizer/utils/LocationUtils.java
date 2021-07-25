package com.loohp.interactionvisualizer.utils;

import org.bukkit.Location;

public class LocationUtils {
	
	public static boolean isLoaded(Location location) {
		try {
			return location.getWorld() != null && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

}
