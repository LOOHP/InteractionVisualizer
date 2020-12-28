package com.loohp.interactionvisualizer.Managers;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundManager {
	
	private static Sound itemPickup;
	
	public static void setup() {
		itemPickup = Sound.valueOf("ENTITY_ITEM_PICKUP");
	}
	
	public static void playItemPickup(Location location, List<Player> players) {
		for (Player player: players) {
			player.playSound(location, itemPickup, 2.0F, (float) Math.random() + 1);
		}
	}

}
