package com.loohp.interactionvisualizer.nms;

import java.util.List;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;

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
	
	public abstract PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments);
	
	public abstract List<BoundingBox> getBoundingBoxes(BlockPosition pos);
	
	public abstract NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load);
	
}
