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
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.api.events.TileEntityRemovedEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BrewingStandDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("brewing_stand");
    private final int max = 20 * 20;
    public ConcurrentHashMap<Block, Map<String, Object>> brewstand = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;
    private String progressBarCharacter = "";
    private String emptyColor = "&7";
    private String filledColor = "&e";
    private String noFuelColor = "&c";
    private int progressBarLength = 10;

    public BrewingStandDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.BrewingStand.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
        progressBarCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.BrewingStand.Options.ProgressBarCharacter"));
        emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.BrewingStand.Options.EmptyColor"));
        filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.BrewingStand.Options.FilledColor"));
        noFuelColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.BrewingStand.Options.NoFuelColor"));
        progressBarLength = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.BrewingStand.Options.ProgressBarLength");
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = brewstand.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) brewstand.size() / (double) gcPeriod);
            int delay = 1;
            while (itr.hasNext()) {
                count++;
                if (count > maxper) {
                    count = 0;
                    delay++;
                }
                Entry<Block, Map<String, Object>> entry = itr.next();
                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                    Block block = entry.getKey();
                    if (!isActive(block.getLocation())) {
                        Map<String, Object> map = entry.getValue();
                        if (map.get("Item") instanceof Item) {
                            Item item = (Item) map.get("Item");
                            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                        }
                        if (map.get("Stand") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("Stand");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        brewstand.remove(block);
                        return;
                    }
                    if (!block.getType().equals(Material.BREWING_STAND)) {
                        Map<String, Object> map = entry.getValue();
                        if (map.get("Item") instanceof Item) {
                            Item item = (Item) map.get("Item");
                            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                        }
                        if (map.get("Stand") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("Stand");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        brewstand.remove(block);
                        return;
                    }
                }, delay);
            }
        }, 0, gcPeriod).getTaskId();
    }

    @Override
    public int run() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
                Set<Block> list = nearbyBrewingStand();
                for (Block block : list) {
                    if (brewstand.get(block) == null && isActive(block.getLocation())) {
                        if (block.getType().equals(Material.BREWING_STAND)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("Item", "N/A");
                            map.putAll(spawnArmorStands(block));
                            brewstand.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = brewstand.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) brewstand.size() / (double) checkingPeriod);
            int delay = 1;
            while (itr.hasNext()) {
                Entry<Block, Map<String, Object>> entry = itr.next();

                count++;
                if (count > maxper) {
                    count = 0;
                    delay++;
                }
                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                    Block block = entry.getKey();
                    if (!isActive(block.getLocation())) {
                        return;
                    }
                    if (!block.getType().equals(Material.BREWING_STAND)) {
                        return;
                    }
                    org.bukkit.block.BrewingStand brewingstand = (org.bukkit.block.BrewingStand) block.getState();

                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
                        Inventory inv = brewingstand.getInventory();
                        ItemStack itemstack = inv.getItem(3);
                        if (itemstack != null) {
                            if (inv.getItem(3).getType().equals(Material.AIR)) {
                                itemstack = null;
                            }
                        }

                        Item item = null;
                        if (entry.getValue().get("Item") instanceof String) {
                            if (itemstack != null) {
                                item = new Item(brewingstand.getLocation().clone().add(0.5, 1.0, 0.5));
                                item.setItemStack(itemstack);
                                item.setVelocity(new Vector(0, 0, 0));
                                item.setPickupDelay(32767);
                                item.setGravity(false);
                                entry.getValue().put("Item", item);
                                PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item);
                                PacketManager.updateItem(item);
                            } else {
                                entry.getValue().put("Item", "N/A");
                            }
                        } else {
                            item = (Item) entry.getValue().get("Item");
                            if (itemstack != null) {
                                if (!item.getItemStack().equals(itemstack)) {
                                    item.setItemStack(itemstack);
                                    PacketManager.updateItem(item);
                                }
                                item.setPickupDelay(32767);
                                item.setGravity(false);
                            } else {
                                entry.getValue().put("Item", "N/A");
                                PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                            }
                        }

                        if (brewingstand.getFuelLevel() == 0) {
                            ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
                            if (hasPotion(brewingstand)) {
                                stand.setCustomNameVisible(true);
                                String name = noFuelColor;
                                for (int i = 0; i < progressBarLength; i++) {
                                    name += progressBarCharacter;
                                }
                                stand.setCustomName(name);
                                PacketManager.updateArmorStand(stand);
                            } else {
                                stand.setCustomNameVisible(false);
                                stand.setCustomName("");
                                PacketManager.updateArmorStand(stand);
                            }
                        } else {
                            ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
                            if (hasPotion(brewingstand)) {
                                int time = brewingstand.getBrewingTime();
                                String symbol = "";
                                double percentagescaled = (double) (max - time) / (double) max * (double) progressBarLength;
                                double i = 1;
                                for (i = 1; i < percentagescaled; i++) {
                                    symbol += filledColor + progressBarCharacter;
                                }
                                i = i - 1;
                                if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.33) {
                                    symbol += emptyColor + progressBarCharacter;
                                } else if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.67) {
                                    symbol += emptyColor + progressBarCharacter;
                                } else if ((percentagescaled - i) > 0) {
                                    symbol += filledColor + progressBarCharacter;
                                }
                                for (i = progressBarLength - 1; i >= percentagescaled; i--) {
                                    symbol += emptyColor + progressBarCharacter;
                                }
                                if (!PlainTextComponentSerializer.plainText().serialize(stand.getCustomName()).equals(symbol) || !stand.isCustomNameVisible()) {
                                    stand.setCustomNameVisible(true);
                                    stand.setCustomName(symbol);
                                    PacketManager.updateArmorStandOnlyMeta(stand);
                                }
                            } else {
                                if (!PlainTextComponentSerializer.plainText().serialize(stand.getCustomName()).equals("") || stand.isCustomNameVisible()) {
                                    stand.setCustomNameVisible(false);
                                    stand.setCustomName("");
                                    PacketManager.updateArmorStandOnlyMeta(stand);
                                }
                            }
                        }
                    });
                }, delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseBrewingStand(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (GameMode.SPECTATOR.equals(event.getWhoClicked().getGameMode())) {
            return;
        }
        if (event.getView().getTopInventory() == null) {
            return;
        }
        try {
            if (event.getView().getTopInventory().getLocation() == null) {
                return;
            }
        } catch (Exception | AbstractMethodError e) {
            return;
        }
        if (event.getView().getTopInventory().getLocation().getBlock() == null) {
            return;
        }
        if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.BREWING_STAND)) {
            return;
        }

        if (event.getRawSlot() >= 0 && event.getRawSlot() <= 4) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragBrewingStand(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (GameMode.SPECTATOR.equals(event.getWhoClicked().getGameMode())) {
            return;
        }
        if (event.getView().getTopInventory() == null) {
            return;
        }
        try {
            if (event.getView().getTopInventory().getLocation() == null) {
                return;
            }
        } catch (Exception | AbstractMethodError e) {
            return;
        }
        if (event.getView().getTopInventory().getLocation().getBlock() == null) {
            return;
        }
        if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.BREWING_STAND)) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 4) {
                PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakBrewingStand(TileEntityRemovedEvent event) {
        Block block = event.getBlock();
        if (!brewstand.containsKey(block)) {
            return;
        }

        Map<String, Object> map = brewstand.get(block);
        if (map.get("Item") instanceof Item) {
            Item item = (Item) map.get("Item");
            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
        }
        if (map.get("Stand") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("Stand");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        brewstand.remove(block);
    }

    public boolean hasPotion(org.bukkit.block.BrewingStand brewingstand) {
        Inventory inv = brewingstand.getInventory();
        if (inv.getItem(0) != null) {
            if (!inv.getItem(0).getType().equals(Material.AIR)) {
                return true;
            }
        }
        if (inv.getItem(1) != null) {
            if (!inv.getItem(1).getType().equals(Material.AIR)) {
                return true;
            }
        }
        if (inv.getItem(2) != null) {
            return !inv.getItem(2).getType().equals(Material.AIR);
        }
        return false;
    }

    public Set<Block> nearbyBrewingStand() {
        return TileEntityManager.getTileEntities(TileEntityType.BREWING_STAND);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public Map<String, ArmorStand> spawnArmorStands(Block block) { //.add(0.68, 0.700781, 0.35)
        Map<String, ArmorStand> map = new HashMap<>();
        Location loc = block.getLocation().clone().add(0.5, 0.700781, 0.5);
        ArmorStand slot1 = new ArmorStand(loc.clone());
        setStand(slot1);

        map.put("Stand", slot1);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot1);

        return map;
    }

    public void setStand(ArmorStand stand) {
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setSilent(true);
        stand.setVisible(false);
        stand.setCustomName("");
        stand.setRightArmPose(EulerAngle.ZERO);
    }

}
