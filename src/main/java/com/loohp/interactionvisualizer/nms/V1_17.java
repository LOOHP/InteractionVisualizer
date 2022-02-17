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
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.BlockBase.BlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class V1_17 extends NMS {

    private static Method voxelShapeGetAABBList;
    private static boolean entityDestoryIsInt;
    private static Method worldServerGetType;
    private static Method blockDataGetShape;
    private static Method blockPositionGetX;
    private static Method blockPositionGetY;
    private static Method blockPositionGetZ;
    private static Field nmsChunkL;
    private static Method tileEntityGetBlock;
    private static Method blockDataGetBlock;
    private static Method worldServerGetTileEntity;
    private static Method nmsNBTTagCompoundGetString;
    private static Method worldServerGetEntities;
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
                entityDestoryIsInt = true;
            } catch (NoSuchMethodException e) {
                entityDestoryIsInt = false;
            }
            worldServerGetType = WorldServer.class.getMethod("getType", net.minecraft.core.BlockPosition.class);
            blockDataGetShape = BlockData.class.getMethod("getShape", IBlockAccess.class, net.minecraft.core.BlockPosition.class);
            blockPositionGetX = net.minecraft.core.BlockPosition.class.getMethod("getX");
            blockPositionGetY = net.minecraft.core.BlockPosition.class.getMethod("getY");
            blockPositionGetZ = net.minecraft.core.BlockPosition.class.getMethod("getZ");
            nmsChunkL = Chunk.class.getField("l");
            tileEntityGetBlock = net.minecraft.world.level.block.entity.TileEntity.class.getMethod("getBlock");
            blockDataGetBlock = BlockData.class.getMethod("getBlock");
            worldServerGetTileEntity = WorldServer.class.getMethod("getTileEntity", net.minecraft.core.BlockPosition.class);
            nmsNBTTagCompoundGetString = NBTTagCompound.class.getMethod("getString", String.class);
            worldServerGetEntities = WorldServer.class.getMethod("getEntities");
            nmsEntityGetBukkitEntity = net.minecraft.world.entity.Entity.class.getMethod("getBukkitEntity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        net.minecraft.core.BlockPosition blockpos = new net.minecraft.core.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
        try {
            VoxelShape shape = (VoxelShape) blockDataGetShape.invoke(worldServerGetType.invoke(blockpos), world, blockpos);
            return ((List<AxisAlignedBB>) voxelShapeGetAABBList.invoke(shape)).stream().map(each -> new BoundingBox(each.a + pos.getX(), each.b + pos.getY(), each.c + pos.getZ(), each.d + pos.getX(), each.e + pos.getY(), each.f + pos.getZ())).collect(Collectors.toList());
        } catch (Exception e) {
            List<BoundingBox> boxes = new ArrayList<>();
            boxes.add(BoundingBox.of(pos.getBlock()));
            return boxes;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        try {
            return new NMSTileEntitySet<net.minecraft.core.BlockPosition, net.minecraft.world.level.block.entity.TileEntity>((Map<net.minecraft.core.BlockPosition, net.minecraft.world.level.block.entity.TileEntity>) nmsChunkL.get(((CraftChunk) chunk.getChunk()).getHandle()), entry -> {
                net.minecraft.core.BlockPosition pos = entry.getKey();
                try {
                    Material type = CraftMagicNumbers.getMaterial((net.minecraft.world.level.block.Block) blockDataGetBlock.invoke(tileEntityGetBlock.invoke(entry.getValue())));
                    TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
                    if (tileEntityType != null) {
                        return new TileEntity(world, (int) blockPositionGetX.invoke(pos), (int) blockPositionGetY.invoke(pos), (int) blockPositionGetZ.invoke(pos), tileEntityType);
                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            });
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
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
        if (entityDestoryIsInt) {
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
            return nmsNBTTagCompoundGetString.invoke(((net.minecraft.world.level.block.entity.TileEntity) worldServerGetTileEntity.invoke(((CraftWorld) block.getWorld()).getHandle(), new net.minecraft.core.BlockPosition(block.getX(), block.getY(), block.getZ()))).Z_(), "CustomName").toString();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        try {
            return new WrappedIterable<net.minecraft.world.entity.Entity, Entity>(((LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerGetEntities.invoke(((CraftWorld) world).getHandle())).a(), entry -> {
                try {
                    return (Entity) nmsEntityGetBukkitEntity.invoke(entry);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

}
