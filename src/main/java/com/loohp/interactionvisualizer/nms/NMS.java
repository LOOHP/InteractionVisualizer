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
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.loohp.interactionvisualizer.objectholders.WrappedIterable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class NMS {

    private static NMS instance = null;

    public static NMS getInstance() {
        if (instance == null) {
            switch (InteractionVisualizer.version) {
                case V1_18:
                    instance = new V1_18();
                    break;
                case V1_17:
                    instance = new V1_17();
                    break;
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
                    break;
            }
        }
        return instance;
    }

    public abstract PacketContainer[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments);

    public PacketContainer[] createEntityDestroyPacket(int... entityIds) {
        PacketContainer packet = InteractionVisualizer.protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntegerArrays().write(0, entityIds);
        return new PacketContainer[] {packet};
    }

    public abstract List<BoundingBox> getBoundingBoxes(BlockPosition pos);

    public abstract NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load);

    public abstract int getItemDespawnRate(Item item);

    public abstract String getBannerCustomName(Block block);

    public abstract WrappedIterable<?, Entity> getEntities(World world);

}
