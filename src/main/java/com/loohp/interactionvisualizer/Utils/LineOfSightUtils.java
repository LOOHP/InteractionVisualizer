package com.loohp.interactionvisualizer.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

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
        
        Map<BlockPosition, List<BoundingBox>> boxes = new HashMap<>();
        for (BlockPosition pos : blocks.keySet()) {
        	if (pos.getWorld().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
	        	boxes.put(pos, BoundingBoxUtils.getBoundingBoxes(pos));
        	}
        }
        
        for (Entry<BlockPosition, List<Location>> entry : blocks.entrySet()) {
        	BlockPosition blockPos = entry.getKey();
        	List<Location> pos = entry.getValue();
        	List<BoundingBox> box = boxes.getOrDefault(blockPos, new ArrayList<>());
        	if (blockPos.getWorld().isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
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
