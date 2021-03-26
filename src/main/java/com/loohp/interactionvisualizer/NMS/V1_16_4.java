package com.loohp.interactionvisualizer.NMS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.ObjectHolders.ValuePairs;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R3.VoxelShape;
import net.minecraft.server.v1_16_R3.WorldServer;

public class V1_16_4 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_16_R3.BlockPosition blockpos = new net.minecraft.server.v1_16_R3.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}

	@Override
	public List<TileEntity> getTileEntities(ChunkPosition chunk, boolean load) {
		List<TileEntity> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		World world = chunk.getWorld();
		
		((CraftChunk) chunk.getChunk()).getHandle().tileEntities.entrySet().forEach(entry -> {
			net.minecraft.server.v1_16_R3.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				list.add(new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType));
			}
		});
		return list;
	}
	
	@Override
	public PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
		List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R3.ItemStack>> nmsList = new ArrayList<>();
		for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
			EnumItemSlot nmsSlot;
			switch (pair.getFirst()) {
			case CHEST:
				nmsSlot = EnumItemSlot.CHEST;
				break;
			case FEET:
				nmsSlot = EnumItemSlot.FEET;
				break;
			case HEAD:
				nmsSlot = EnumItemSlot.HEAD;
				break;
			case LEGS:
				nmsSlot = EnumItemSlot.LEGS;
				break;
			case OFF_HAND:
				nmsSlot = EnumItemSlot.OFFHAND;
				break;
			case HAND:
			default:
				nmsSlot = EnumItemSlot.MAINHAND;
				break;
			}
			net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
			nmsList.add(new Pair<>(nmsSlot, nmsItem));
		}
		PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, nmsList);
		return new PacketContainer[] {PacketContainer.fromPacket(packet)};
	}

}