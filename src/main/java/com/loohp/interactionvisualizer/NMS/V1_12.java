package com.loohp.interactionvisualizer.NMS;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.WorldServer;

public class V1_12 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_12_R1.BlockPosition blockpos = new net.minecraft.server.v1_12_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		AxisAlignedBB box = world.getType(blockpos).d(world, blockpos);
		List<BoundingBox> boxes = new ArrayList<>(1);
		boxes.add(new BoundingBox(box.a + pos.getX(), box.b + pos.getY(), box.c + pos.getZ(), box.d + pos.getX(), box.e + pos.getY(), box.f + pos.getZ()));
		return boxes;
	}

}
