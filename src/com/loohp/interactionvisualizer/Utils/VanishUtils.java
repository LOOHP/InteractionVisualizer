package com.loohp.interactionvisualizer.Utils;

import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;

import de.myzelyam.api.vanish.VanishAPI;

public class VanishUtils {
	
	public static boolean vanishenabled = InteractionVisualizer.vanish;
	
	public static boolean isVanished(Player player) {
		if (!vanishenabled) {
			return false;
		}
		if (VanishAPI.isInvisible(player)) {
			return true;
		}
		return false;
	}

}
