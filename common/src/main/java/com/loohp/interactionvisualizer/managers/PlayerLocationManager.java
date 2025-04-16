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

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.entityholders.VisualizerEntity;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class PlayerLocationManager {

    public static boolean hasPlayerNearby(Location location, double range, boolean eyeLocation, Predicate<Player> predicate) {
        World world = location.getWorld();
        for (Player player : world.getPlayers()) {
            Location playerLocation = eyeLocation ? player.getEyeLocation() : player.getLocation();
            if (playerLocation.getWorld().equals(world) && predicate.test(player) && playerLocation.distanceSquared(location) <= range * range) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPlayerNearby(Location location) {
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        ChunkPosition chunkpos = new ChunkPosition(world, chunkX, chunkZ);
        if (!chunkpos.isLoaded()) {
            return false;
        }

        Set<ChunkPosition> nearby = new HashSet<>();
        for (int z = -InteractionVisualizer.tileEntityCheckingRange; z <= InteractionVisualizer.tileEntityCheckingRange; z++) {
            for (int x = -InteractionVisualizer.tileEntityCheckingRange; x <= InteractionVisualizer.tileEntityCheckingRange; x++) {
                nearby.add(new ChunkPosition(world, chunkX + x, chunkZ + z));
            }
        }

        for (Player player : world.getPlayers()) {
            Location playerLocation = player.getLocation();
            ChunkPosition playerChunk = new ChunkPosition(world, playerLocation.getBlockX() >> 4, playerLocation.getBlockZ() >> 4);
            if (nearby.contains(playerChunk)) {
                return true;
            }
        }
        return false;
    }

    public static Location getPlayerLocation(Player player) {
        return player.getLocation();
    }

    public static Location getPlayerEyeLocation(Player player) {
        return player.getEyeLocation();
    }

    public static Collection<Player> filterOutOfRange(Collection<Player> players, VisualizerEntity entity) {
        return filterOutOfRange(players, entity.getLocation());
    }

    public static Collection<Player> filterOutOfRange(Collection<Player> players, Entity entity) {
        return filterOutOfRange(players, entity.getLocation());
    }

    public static Collection<Player> filterOutOfRange(Collection<Player> players, Location location) {
        return filterOutOfRange(players, location, player -> true);
    }

    public static Collection<Player> filterOutOfRange(Collection<Player> players, Location location, Predicate<Player> predicate) {
        Collection<Player> playersInRange = new HashSet<>();
        int range = InteractionVisualizer.playerTrackingRange.getOrDefault(location.getWorld(), 64);
        range *= range;
        for (Player player : players) {
            Location playerLocation = PlayerLocationManager.getPlayerLocation(player);
            if (playerLocation.getWorld().equals(location.getWorld()) && (playerLocation.distanceSquared(location) <= range) && predicate.test(player)) {
                playersInRange.add(player);
            }
        }
        return playersInRange;
    }

}
