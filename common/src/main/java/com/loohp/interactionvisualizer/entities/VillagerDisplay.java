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

package com.loohp.interactionvisualizer.entities;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerDisplay;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.InventoryUtils;
import com.loohp.interactionvisualizer.utils.MCVersion;
import com.loohp.interactionvisualizer.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.util.Vector;

import java.util.Objects;

public class VillagerDisplay implements Listener, VisualizerDisplay {

    public static final EntryKey KEY = new EntryKey("villager");

    @Override
    public EntryKey key() {
        return KEY;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVillageTrade(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
            return;
        }
        if (!(event.getView().getTopInventory() instanceof MerchantInventory)) {
            return;
        }
        if (event.getRawSlot() != 2) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.AIR)) {
            return;
        }
        if (event.getCursor() != null) {
            if (!event.getCursor().getType().equals(Material.AIR)) {
                if (event.getCursor().getAmount() >= event.getCursor().getType().getMaxStackSize()) {
                    return;
                }
            }
        }
        if (event.isShiftClick()) {
            if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(2).getType())) {
                return;
            }
        }
        int hotbarSlot = event.getHotbarButton();
        if (hotbarSlot >= 0 && !Objects.equals(event.getClickedInventory(), event.getWhoClicked().getInventory())) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);
            if (hotbarItem != null) {
                return;
            }
        }

        ItemStack item0 = null;
        if (event.getView().getItem(0) != null) {
            if (!event.getView().getItem(0).getType().equals(Material.AIR)) {
                item0 = event.getView().getItem(0).clone();
            }
        }
        ItemStack item1 = null;
        if (event.getView().getItem(1) != null) {
            if (!event.getView().getItem(1).getType().equals(Material.AIR)) {
                item1 = event.getView().getItem(1).clone();
            }
        }
        ItemStack item2 = event.getCurrentItem().clone();
        Player player = (Player) event.getWhoClicked();
        MerchantInventory tradeinv = (MerchantInventory) event.getView().getTopInventory();
        if (InteractionVisualizer.version.isNewerOrEqualTo(MCVersion.V1_14)) {
            if (!(tradeinv.getHolder() instanceof org.bukkit.entity.AbstractVillager)) {
                return;
            }
        } else {
            if (!(tradeinv.getHolder() instanceof Villager)) {
                return;
            }
        }
        NPC villager = (NPC) tradeinv.getHolder();
        Vector lift = new Vector(0.0, 0.20, 0.0);
        if (item0 != null) {
            Item in = new Item(player.getEyeLocation());
            Vector vector = villager.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(player.getEyeLocation().toVector()).multiply(0.12).add(lift);
            in.setItemStack(item0);
            in.setGravity(true);
            in.setVelocity(vector);
            PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), in);
            PacketManager.updateItem(in);

            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), in), 14);
        }

        ItemStack item1final = item1;
        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            if (item1final != null) {
                Item in = new Item(player.getEyeLocation());
                Vector vector = villager.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(player.getEyeLocation().toVector()).multiply(0.12).add(lift);
                in.setItemStack(item1final);
                in.setGravity(true);
                in.setVelocity(vector);
                PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), in);
                PacketManager.updateItem(in);

                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), in), 14);
            }
        }, 8);

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            Item out = new Item(villager.getEyeLocation());
            Vector vector = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(villager.getEyeLocation().toVector()).multiply(0.10).add(lift);
            out.setItemStack(item2);
            out.setGravity(true);
            out.setVelocity(vector);
            PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), out);
            PacketManager.updateItem(out);

            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), out), 12);
        }, 40);
    }

}
