package com.loohp.interactionvisualizer.utils;

import java.util.List;

import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;

public class BoundingBoxUtils {
	
	private static NMS nmsImplementation = NMS.getInstance();
	
	public static List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		if (nmsImplementation != null) {
			return nmsImplementation.getBoundingBoxes(pos);
		} else {
			throw new RuntimeException("No NMS implementation found for BoundingBoxUtils.getBoundingBoxes(BlockPosition)");
		}
	}
}
