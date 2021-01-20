package com.loohp.interactionvisualizer.NMS;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;

import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;

import net.minecraft.server.v1_13_R2.VoxelShape;
import net.minecraft.server.v1_13_R2.WorldServer;

public class V1_13_1 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_13_R2.BlockPosition blockpos = new net.minecraft.server.v1_13_R2.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}

}
