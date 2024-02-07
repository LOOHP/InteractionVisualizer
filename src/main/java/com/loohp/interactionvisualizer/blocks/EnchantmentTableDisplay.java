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

package com.loohp.interactionvisualizer.blocks;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.VisualizerInteractDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.objectholders.EnchantmentTableAnimation;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.CustomMapUtils;
import com.loohp.interactionvisualizer.utils.InventoryUtils;
import com.loohp.interactionvisualizer.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class EnchantmentTableDisplay extends VisualizerInteractDisplay implements Listener {

    public static final Pattern VALID_NUMBER = Pattern.compile("-?[0-9]+");

    private static Method bukkitEnchantmentGetIdMethod;
    private static Set<String> translatableEnchantments = new HashSet<>();
    private static Map<String, String> customDefinedEnchantmentNames = new HashMap<>();

    static {
        if (InteractionVisualizer.version.isLegacy()) {
            try {
                bukkitEnchantmentGetIdMethod = Enchantment.class.getMethod("getId");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Set<String> getTranslatableEnchantments() {
        return translatableEnchantments;
    }

    public static Map<String, String> getCustomDefinedEnchantmentNames() {
        return customDefinedEnchantmentNames;
    }

    public static String getEnchantmentIdOrKey(Enchantment enchantment) {
        if (InteractionVisualizer.version.isLegacy()) {
            try {
                return bukkitEnchantmentGetIdMethod.invoke(enchantment).toString();
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return "-1";
            }
        } else {
            return enchantment.getKey().toString();
        }
    }

    public Map<Player, Block> playermap = new ConcurrentHashMap<>();

    public EnchantmentTableDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        translatableEnchantments = new HashSet<>(InteractionVisualizer.plugin.getConfiguration().getStringList("Blocks.EnchantmentTable.Options.TranslatableEnchantments"));
        customDefinedEnchantmentNames = new HashMap<>();
        if (InteractionVisualizer.plugin.getConfiguration().isConfigurationSection("Blocks.EnchantmentTable.Options.CustomDefinedEnchantmentNames")) {
            for (Map.Entry<String, Object> entry : InteractionVisualizer.plugin.getConfiguration().getConfigurationSection("Blocks.EnchantmentTable.Options.CustomDefinedEnchantmentNames").getValues(false).entrySet()) {
                String key = entry.getKey();
                if (!key.contains(":") && !VALID_NUMBER.matcher(key).matches()) {
                    key = "minecraft:" + key;
                }
                customDefinedEnchantmentNames.put(key, entry.getValue().toString());
            }
        }
    }

    @Override
    public EntryKey key() {
        return EnchantmentTableAnimation.KEY;
    }

    @Override
    public void process(Player player) {
        if (VanishUtils.isVanished(player)) {
            return;
        }
        if (!playermap.containsKey(player)) {
            if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                return;
            }
            if (player.getOpenInventory().getTopInventory().getLocation() == null) {
                return;
            }
            if (player.getOpenInventory().getTopInventory().getLocation().getBlock() == null) {
                return;
            }
            if (!InteractionVisualizer.version.isLegacy()) {
                if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().equalsIgnoreCase("ENCHANTING_TABLE")) {
                    return;
                }
            } else {
                if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().equalsIgnoreCase("ENCHANTMENT_TABLE")) {
                    return;
                }
            }
            InventoryView view = player.getOpenInventory();
            Block block = view.getTopInventory().getLocation().getBlock();
            playermap.put(player, block);
        }

        InventoryView view = player.getOpenInventory();
        Block block = playermap.get(player);

        EnchantmentTableAnimation animation = EnchantmentTableAnimation.getTableAnimation(block, player);
        if (animation == null) {
            return;
        }

        ItemStack itemstack = view.getItem(0) != null && !view.getItem(0).getType().equals(Material.AIR) ? view.getItem(0).clone() : null;

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            if (!animation.isEnchanting()) {
                animation.queueSetItem(itemstack, a -> {
                    InventoryView inventory = player.getOpenInventory();
                    if (inventory.equals(view)) {
                        if (inventory.countSlots() > 0) {
                            ItemStack current = view.getItem(0) != null && !view.getItem(0).getType().equals(Material.AIR) ? view.getItem(0).clone() : null;
                            return current.isSimilar(itemstack);
                        }
                    }
                    return false;
                });
            }
        }, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (VanishUtils.isVanished(event.getEnchanter())) {
            return;
        }
        Block block = event.getEnchantBlock();
        Player player = event.getEnchanter();

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            if (!player.getOpenInventory().getTopInventory().getType().equals(InventoryType.ENCHANTING)) {
                return;
            }
            ItemStack itemstack = player.getOpenInventory().getItem(0).clone();
            Map<Enchantment, Integer> enchantsAdded = new HashMap<Enchantment, Integer>();
            if (itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
                enchantsAdded.putAll(((EnchantmentStorageMeta) itemstack.getItemMeta()).getStoredEnchants());
            }
            enchantsAdded.putAll(itemstack.getEnchantments());

            enchantsAdded = CustomMapUtils.sortMapByValueReverse(enchantsAdded);

            EnchantmentTableAnimation animation = EnchantmentTableAnimation.getTableAnimation(block, player);
            if (animation == null) {
                return;
            }

            if (!animation.isEnchanting()) {
                animation.queueEnchant(enchantsAdded, event.getExpLevelCost(), itemstack, a -> true);
            }
        }, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantmentTableTake(InventoryClickEvent event) {
        if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
            return;
        }
        if (event.getRawSlot() != 0) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.AIR)) {
            return;
        }

        if (event.getCursor() != null) {
            if (event.getCursor().getType().equals(event.getCurrentItem().getType())) {
                return;
            }
        }

        if (event.isShiftClick()) {
            if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(event.getRawSlot()).getType())) {
                return;
            }
        }

        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }

        Block block = playermap.get((Player) event.getWhoClicked());
        Player player = (Player) event.getWhoClicked();

        EnchantmentTableAnimation animation = EnchantmentTableAnimation.getTableAnimation(block, player);
        if (animation == null) {
            return;
        }

        ItemStack itemstack = event.getCurrentItem().clone();
        int slot = event.getRawSlot();

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            if (player.getOpenInventory().getItem(slot) == null || (itemstack.isSimilar(player.getOpenInventory().getItem(slot)) && itemstack.getAmount() == player.getOpenInventory().getItem(slot).getAmount())) {
                return;
            }

            animation.queuePickupAnimation(itemstack, a -> {
                ItemStack stack = a.getItemStack();
                return stack != null && stack.isSimilar(itemstack);
            });
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUseEnchantmentTable(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!playermap.containsKey(player)) {
            return;
        }

        if (event.getRawSlot() >= 0 && event.getRawSlot() <= 1) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragEnchantmentTable(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!playermap.containsKey(player)) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 1) {
                PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
                break;
            }
        }
    }

    @EventHandler
    public void onCloseEnchantmentTable(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!playermap.containsKey(player)) {
            return;
        }

        Block block = playermap.remove(player);

        ItemStack itemstack = event.getView().getItem(0) != null ? (!event.getView().getItem(0).getType().equals(Material.AIR) ? event.getView().getItem(0).clone() : null) : null;

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            EnchantmentTableAnimation animation = EnchantmentTableAnimation.getTableAnimation(block, player);
            if (animation == null) {
                return;
            }
            animation.queuePickupAnimation(itemstack, a -> {
                ItemStack stack = a.getItemStack();
                return stack != null && stack.isSimilar(itemstack);
            });
            animation.queueClose(a -> true);
        }, 1);
    }

}
