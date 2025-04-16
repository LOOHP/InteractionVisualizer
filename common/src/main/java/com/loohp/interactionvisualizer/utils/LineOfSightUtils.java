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

package com.loohp.interactionvisualizer.utils;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LineOfSightUtils {

    public static boolean hasLineOfSight(Location from, Location to) {
        return hasLineOfSight(from, to, 0.01);
    }

    @SuppressWarnings("deprecation")
    public static boolean hasLineOfSight(Location from, Location to, double accuracy) {
        if (!from.getWorld().equals(to.getWorld())) {
            return false;
        }

        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
        Map<BlockPosition, List<Location>> blocks = new LinkedHashMap<>();
        for (double d = 0; d <= distance; d += accuracy) {
            Location pos = from.clone().add(direction.clone().multiply(d));
            BlockPosition blockPos = new BlockPosition(pos);
            List<Location> list = blocks.get(blockPos);
            if (list == null) {
                list = new ArrayList<>();
                blocks.put(blockPos, list);
            }
            list.add(pos);
        }

        for (Entry<BlockPosition, List<Location>> entry : blocks.entrySet()) {
            BlockPosition blockPos = entry.getKey();
            List<Location> pos = entry.getValue();
            if (blockPos.getWorld().isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
                List<BoundingBox> box = NMS.getInstance().getBoundingBoxes(blockPos);
                Material type = blockPos.getBlock().getType();
                if (!type.isTransparent() && !type.toString().contains("GLASS") && !InteractionVisualizer.exemptBlocks.contains(type.toString())) {
                    for (Location point : pos) {
                        if (box.stream().anyMatch(each -> each.contains(point.getX(), point.getY(), point.getZ()))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
