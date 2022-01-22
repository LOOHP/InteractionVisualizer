package com.loohp.interactionvisualizer.managers;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Random;

public class SoundManager {

    public static final float VOLUME = 2.0F;
    public static final int AUDIBLE_RANGE = 16;

    private static final Random random = new Random();
    private static Sound itemPickup;

    public static void setup() {
        itemPickup = Sound.valueOf("ENTITY_ITEM_PICKUP");
    }

    public static void playItemPickup(Location location, Collection<Player> players, float volume) {
        float range = Math.max(AUDIBLE_RANGE, volume * AUDIBLE_RANGE) + 1;
        for (Player player : players) {
            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) <= range) {
                player.playSound(location, itemPickup, 2.0F, random.nextFloat() + 1);
            }
        }
    }

    public static void playItemPickup(Location location, Collection<Player> players) {
        playItemPickup(location, players, VOLUME);
    }

}
