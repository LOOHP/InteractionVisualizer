package com.loohp.interactionvisualizer.NMS;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;

import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

import net.minecraft.server.v1_16_R1.WorldServer;
import net.minecraft.server.v1_16_R1.VoxelShape;

public class V1_16 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_16_R1.BlockPosition blockpos = new net.minecraft.server.v1_16_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}

}
