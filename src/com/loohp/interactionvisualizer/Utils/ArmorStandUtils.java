package com.loohp.interactionvisualizer.Utils;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class ArmorStandUtils {
	
	public static void setRotation(ArmorStand stand, float yaw, float pitch) {
		stand.teleport(new Location(stand.getWorld(), stand.getLocation().getX(), stand.getLocation().getY(), stand.getLocation().getZ(), yaw, pitch));
	}

}
