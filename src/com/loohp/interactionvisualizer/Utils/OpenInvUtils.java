package com.loohp.interactionvisualizer.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.loohp.interactionvisualizer.InteractionVisualizer;

public class OpenInvUtils {
	
	public static boolean openinvhook = InteractionVisualizer.openinv;
	
	public static boolean isSlientChest(Player player) {
		if (!openinvhook) {
			return false;
		}
		OpenInv openinv = (OpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
		boolean isSilent = openinv.getPlayerSilentChestStatus(player);
		if (isSilent) {
			return true;
		}
		return false;
	}

}
