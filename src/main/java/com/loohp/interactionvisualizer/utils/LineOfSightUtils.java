package com.loohp.interactionvisualizer.utils;

import com.loohp.interactionvisualizer.InteractionVisualizer;
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
                List<BoundingBox> box = BoundingBoxUtils.getBoundingBoxes(blockPos);
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
