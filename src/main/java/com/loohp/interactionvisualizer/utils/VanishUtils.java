package com.loohp.interactionvisualizer.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.loohp.interactionvisualizer.InteractionVisualizer;

import de.myzelyam.api.vanish.VanishAPI;

public class VanishUtils {
	
	public static Boolean vanishenabled = InteractionVisualizer.vanish;
	public static Boolean cmienabled = InteractionVisualizer.cmi;
	public static Boolean ess3enabled = InteractionVisualizer.ess3;
	
	public static boolean isVanished(Player player) {
		if (vanishenabled) {
			if (VanishAPI.isInvisible(player)) {
				return true;
			}
		}
		if (cmienabled) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if (user.isVanished()) {
				return true;
			}
		}
		if (ess3enabled) {
			Essentials ess3 = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess3.getUser(player);
			if (user.isVanished()) {
				return true;
			}
		}
		return false;
	}

}
