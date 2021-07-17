package com.loohp.interactionvisualizer.utils;

import org.bukkit.Location;

public class LocationUtils {
	
	public static boolean isLoaded(Location location) {
		return location.isWorldLoaded() && location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

}
