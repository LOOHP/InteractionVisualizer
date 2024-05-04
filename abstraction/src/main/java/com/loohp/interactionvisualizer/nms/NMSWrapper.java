/*
 * This file is part of ImageFrame.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
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

import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.ItemFrame;
import com.loohp.interactionvisualizer.entityholders.VisualizerEntity;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.loohp.interactionvisualizer.objectholders.WrappedIterable;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

public abstract class NMSWrapper {

    private static Plugin plugin;
    private static NMSWrapper instance;

    @Deprecated
    public static Plugin getPlugin() {
        return plugin;
    }

    @Deprecated
    public static NMSWrapper getInstance() {
        return instance;
    }

    @Deprecated
    public static void setup(NMSWrapper instance, Plugin plugin) {
        NMSWrapper.instance = instance;
        NMSWrapper.plugin = plugin;
    }

    public static int getItemFrameData(ItemFrame frame) {
        switch (frame.getAttachedFace()) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 0;
        }
    }

    public abstract Enchantment getPowerEnchantment();

    public abstract Particle getItemCrackParticle();

    public abstract int getItemAge(Item item);

    public abstract int getItemDamage(ItemStack itemStack);

    public abstract Component getItemHoverName(ItemStack itemStack);

    public abstract Object[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments);

    public abstract Object[] createEntityDestroyPacket(int... entityIds);

    public abstract Object createEntityMetadataPacket(int entityId, List<?> dataWatchers);

    public abstract List<BoundingBox> getBoundingBoxes(BlockPosition pos);

    public abstract NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load);

    public abstract int getItemDespawnRate(Item item);

    public abstract String getBannerCustomName(Block block);

    public abstract WrappedIterable<?, Entity> getEntities(World world);

    public abstract Future<Integer> getNextEntityId();

    public abstract ChatColor getRarityColor(ItemStack itemStack);

    public abstract String getTranslationKey(ItemStack itemStack);

    public abstract String getEnchantmentTranslationKey(Enchantment enchantment);

    public abstract String getEffectTranslationKey(PotionEffectType type);

    public abstract EntityType getEntityType(VisualizerEntity entity);

    public abstract List<?> getWatchableCollection(ArmorStand stand);

    public abstract List<?> getWatchableCollection(com.loohp.interactionvisualizer.entityholders.Item item);

    public abstract List<?> getWatchableCollection(ItemFrame frame);

    public abstract List<?> createCustomNameWatchableCollection(Component name);

    public abstract List<?> resetCustomNameWatchableCollection(Entity entity);

    public abstract void sendHandMovement(Collection<Player> players, Player entity);

    public abstract void teleportEntity(Player player, int entityId, Location location);

    public abstract void spawnArmorStand(Collection<Player> players, ArmorStand entity);

    public abstract void updateArmorStand(Collection<Player> players, ArmorStand entity);

    public abstract void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity);

    public abstract void removeArmorStand(Collection<Player> players, ArmorStand entity);

    public abstract void spawnItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity);

    public abstract void updateItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity);

    public abstract void removeItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity);

    public abstract void spawnItemFrame(Collection<Player> players, ItemFrame entity);

    public abstract void updateItemFrame(Collection<Player> players, ItemFrame entity);

    public abstract void removeItemFrame(Collection<Player> players, ItemFrame entity);

    public abstract void sendPacket(Player player, Object packet);

}
