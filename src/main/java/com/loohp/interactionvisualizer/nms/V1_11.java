package com.loohp.interactionvisualizer.nms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;

import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.WorldServer;

public class V1_11 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_11_R1.BlockPosition blockpos = new net.minecraft.server.v1_11_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		AxisAlignedBB box = world.getType(blockpos).d(world, blockpos);
		List<BoundingBox> boxes = new ArrayList<>(1);
		boxes.add(new BoundingBox(box.a + pos.getX(), box.b + pos.getY(), box.c + pos.getZ(), box.d + pos.getX(), box.e + pos.getY(), box.f + pos.getZ()));
		return boxes;
	}
	
	@Override
	public List<TileEntity> getTileEntities(ChunkPosition chunk, boolean load) {
		List<TileEntity> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		World world = chunk.getWorld();
		
		((CraftChunk) chunk.getChunk()).getHandle().tileEntities.entrySet().forEach(entry -> {
			net.minecraft.server.v1_11_R1.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				list.add(new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType));
			}
		});
		return list;
	}
	
	@Override
	public PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
		PacketContainer[] packets = new PacketContainer[equipments.size()];
		for (int i = 0; i < equipments.size(); i++) {
			ValuePairs<EquipmentSlot, ItemStack> pair = equipments.get(i);
			PacketContainer packet = InteractionVisualizer.protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
			packet.getIntegers().write(0, entityId);
			ItemSlot libSlot;
			switch (pair.getFirst()) {
			case CHEST:
				libSlot = ItemSlot.CHEST;
				break;
			case FEET:
				libSlot = ItemSlot.FEET;
				break;
			case HEAD:
				libSlot = ItemSlot.HEAD;
				break;
			case LEGS:
				libSlot = ItemSlot.LEGS;
				break;
			case OFF_HAND:
				libSlot = ItemSlot.OFFHAND;
				break;
			case HAND:
			default:
				libSlot = ItemSlot.MAINHAND;
				break;
			}
			packet.getItemSlots().write(0, libSlot);
			packet.getItemModifier().write(0, pair.getSecond());
			packets[i] = packet;
		}
		return packets;
	}

}
