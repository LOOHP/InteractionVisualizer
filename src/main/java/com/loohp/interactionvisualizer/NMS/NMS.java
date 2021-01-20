package com.loohp.interactionvisualizer.NMS;

import java.util.List;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

public abstract class NMS {
	
	private static NMS instance;
	
	public static NMS getInstance() {
		if (instance == null) {
			switch (InteractionVisualizer.version) {
			case V1_16_4:
				instance = new V1_16_4();
				break;
			case V1_16_2:
				instance = new V1_16_2();
				break;
			case V1_16:
				instance = new V1_16();
				break;
			case V1_15:
				instance = new V1_15();
				break;
			case V1_14:
				instance = new V1_14();
				break;
			case V1_13_1:
				instance = new V1_13_1();
				break;
			case V1_13:
				instance = new V1_13();
				break;
			case V1_12:
				instance = new V1_12();
				break;
			case V1_11:
				instance = new V1_11();
				break;
			default:
				instance = null;
				break;
			}
		}
		return instance;
	}
	
	public abstract List<BoundingBox> getBoundingBoxes(BlockPosition pos);

}
