/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.loohp.interactionvisualizer.objectholders.WrappedIterable;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class V1_18 extends NMS {

    private static Method voxelShapeGetAABBList;
    private static boolean entityDestroyIsInt;
    private static Method nmsTileEntityGetNBTTag;
    private static Field worldServerPersistentEntitySectionManager;
    private static Method nmsEntityGetBukkitEntity;

    static {
        try {
            try {
                voxelShapeGetAABBList = VoxelShape.class.getMethod("d");
            } catch (NoSuchMethodException | SecurityException e) {
                voxelShapeGetAABBList = VoxelShape.class.getMethod("toList");
            }
            try {
                PacketPlayOutEntityDestroy.class.getConstructor(int.class);
                entityDestroyIsInt = true;
            } catch (NoSuchMethodException e) {
                entityDestroyIsInt = false;
            }
            nmsTileEntityGetNBTTag = net.minecraft.world.level.block.entity.TileEntity.class.getMethod("Z_");
            worldServerPersistentEntitySectionManager = WorldServer.class.getField("P");
            nmsEntityGetBukkitEntity = net.minecraft.world.entity.Entity.class.getMethod("getBukkitEntity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        net.minecraft.core.BlockPosition blockpos = new net.minecraft.core.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
        VoxelShape shape = world.a_(blockpos).j(world, blockpos);
        try {
            return ((List<AxisAlignedBB>) voxelShapeGetAABBList.invoke(shape)).stream().map(each -> new BoundingBox(each.a + pos.getX(), each.b + pos.getY(), each.c + pos.getZ(), each.d + pos.getX(), each.e + pos.getY(), each.f + pos.getZ())).collect(Collectors.toList());
        } catch (Exception e) {
            List<BoundingBox> boxes = new ArrayList<>();
            boxes.add(BoundingBox.of(pos.getBlock()));
            return boxes;
        }
    }

    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        return new NMSTileEntitySet<net.minecraft.core.BlockPosition, net.minecraft.world.level.block.entity.TileEntity>(((CraftChunk) chunk.getChunk()).getHandle().E(), entry -> {
            net.minecraft.core.BlockPosition pos = entry.getKey();
            Material type = CraftMagicNumbers.getMaterial(entry.getValue().q().b());
            TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
            if (tileEntityType != null) {
                return new TileEntity(world, pos.u(), pos.v(), pos.w(), tileEntityType);
            } else {
                return null;
            }
        });
    }

    @Override
    public PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsList = new ArrayList<>();
        for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
            EnumItemSlot nmsSlot;
            switch (pair.getFirst()) {
                case CHEST:
                    nmsSlot = EnumItemSlot.e;
                    break;
                case FEET:
                    nmsSlot = EnumItemSlot.c;
                    break;
                case HEAD:
                    nmsSlot = EnumItemSlot.f;
                    break;
                case LEGS:
                    nmsSlot = EnumItemSlot.d;
                    break;
                case OFF_HAND:
                    nmsSlot = EnumItemSlot.b;
                    break;
                case HAND:
                default:
                    nmsSlot = EnumItemSlot.a;
                    break;
            }
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
            nmsList.add(new Pair<>(nmsSlot, nmsItem));
        }
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, nmsList);
        return new PacketContainer[] {PacketContainer.fromPacket(packet)};
    }

    @Override
    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        if (entityDestroyIsInt) {
            PacketContainer[] packets = new PacketContainer[entityIds.length];
            for (int i = 0; i < entityIds.length; i++) {
                PacketContainer packet = InteractionVisualizer.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                packet.getIntegers().write(0, entityIds[i]);
                packets[i] = packet;
            }
            return packets;
        } else {
            return new PacketContainer[] {PacketContainer.fromPacket(new PacketPlayOutEntityDestroy(entityIds))};
        }
    }

    @Override
    public int getItemDespawnRate(Item item) {
        int despawnRate;
        try {
            Object spigotWorldConfig = net.minecraft.world.level.World.class.getField("spigotConfig").get(((CraftWorld) item.getWorld()).getHandle());
            despawnRate = spigotWorldConfig.getClass().getField("itemDespawnRate").getInt(spigotWorldConfig);
            try {
                despawnRate = (int) EntityItem.class.getMethod("getDespawnRate").invoke(((CraftItem) item).getHandle());
            } catch (Throwable ignore) {
            }
        } catch (Throwable e) {
            despawnRate = 6000;
        }
        return despawnRate;
    }

    @Override
    public String getBannerCustomName(Block block) {
        try {
            return ((NBTTagCompound) nmsTileEntityGetNBTTag.invoke(((CraftWorld) block.getWorld()).getHandle().c_(new net.minecraft.core.BlockPosition(block.getX(), block.getY(), block.getZ())))).l("CustomName");
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    @SuppressWarnings({"resource", "unchecked"})
    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        try {
            PersistentEntitySectionManager<net.minecraft.world.entity.Entity> manager = (PersistentEntitySectionManager<net.minecraft.world.entity.Entity>) worldServerPersistentEntitySectionManager.get(((CraftWorld) world).getHandle());
            return new WrappedIterable<net.minecraft.world.entity.Entity, Entity>(manager.d().a(), entry -> {
                try {
                    return (Entity) nmsEntityGetBukkitEntity.invoke(entry);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new WrappedIterable<>(Collections.emptyList(), entry -> null);
        }
    }

}
