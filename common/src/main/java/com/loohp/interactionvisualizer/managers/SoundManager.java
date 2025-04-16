/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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

package com.loohp.interactionvisualizer.managers;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Random;

public class SoundManager {

    public static final float VOLUME = 2.0F;
    public static final int AUDIBLE_RANGE = 16;
    public static final Sound ITEM_PICKUP_SOUND = XSound.ENTITY_ITEM_PICKUP.parseSound();

    private static final Random RANDOM = new Random();

    public static void playItemPickup(Location location, Collection<Player> players, float volume) {
        float range = Math.max(AUDIBLE_RANGE, volume * AUDIBLE_RANGE) + 1;
        for (Player player : players) {
            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) <= range) {
                //noinspection DataFlowIssue
                player.playSound(location, ITEM_PICKUP_SOUND, SoundCategory.PLAYERS, volume, RANDOM.nextFloat() + 1);
            }
        }
    }

    public static void playItemPickup(Location location, Collection<Player> players) {
        playItemPickup(location, players, VOLUME);
    }

}
