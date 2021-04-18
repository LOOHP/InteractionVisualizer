package com.loohp.interactionvisualizer.nms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_16_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.v1_16_R2.EntityItem;
import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R2.VoxelShape;
import net.minecraft.server.v1_16_R2.WorldServer;

public class V1_16_2 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_16_R2.BlockPosition blockpos = new net.minecraft.server.v1_16_R2.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}
	
	@Override
	public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
		if (!chunk.isLoaded() && !load) {
			return null;
		}
		World world = chunk.getWorld();
		return new NMSTileEntitySet<net.minecraft.server.v1_16_R2.BlockPosition, net.minecraft.server.v1_16_R2.TileEntity>(((CraftChunk) chunk.getChunk()).getHandle().tileEntities, entry -> {
			net.minecraft.server.v1_16_R2.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				return new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType);
			} else {
				return null;
			}
		});
	}
	
	@Override
	public PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
		List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack>> nmsList = new ArrayList<>();
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
			net.minecraft.server.v1_16_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
			nmsList.add(new Pair<>(nmsSlot, nmsItem));
		}
		PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, nmsList);
		return new PacketContainer[] {PacketContainer.fromPacket(packet)};
	}
	
	@Override
	public int getItemDespawnRate(Item item) {
		int despawnRate;
		try {
			Object spigotWorldConfig = net.minecraft.server.v1_16_R2.World.class.getField("spigotConfig").get(((CraftWorld) item.getWorld()).getHandle());
			despawnRate = spigotWorldConfig.getClass().getField("itemDespawnRate").getInt(spigotWorldConfig);
			try {
				despawnRate = (int) EntityItem.class.getMethod("getDespawnRate").invoke(((CraftItem) item).getHandle());
			} catch (Throwable ignore) {}
		} catch (Throwable e) {
			despawnRate = 6000;
		}
		return despawnRate;
	}

}
