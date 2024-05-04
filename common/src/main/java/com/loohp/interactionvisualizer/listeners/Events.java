/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactionvisualizer.listeners;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class Events implements Listener {

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        int defaultRange = Bukkit.spigot().getConfig().getInt("world-settings.default.entity-tracking-range.players", 64);
        int range = Bukkit.spigot().getConfig().getInt("world-settings." + world.getName() + ".entity-tracking-range.players", defaultRange);
        InteractionVisualizer.playerTrackingRange.put(world, range);
        for (Entity entity : world.getNearbyEntities(new Location(event.getWorld(), 0, 0, 0), 2, 2, 2)) {
            if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
                entity.remove();
            }
        }
    }

}
