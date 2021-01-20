package com.loohp.interactionvisualizer.Utils;

import java.util.List;

import com.loohp.interactionvisualizer.NMS.NMS;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

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
