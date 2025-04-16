/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ColorUtils;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import com.loohp.interactionvisualizer.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JukeBoxDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("jukebox");

    public ConcurrentHashMap<Block, Map<String, Object>> jukeboxMap = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;
    private boolean showDiscName = true;

    public JukeBoxDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.JukeBox.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
        showDiscName = InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.JukeBox.Options.ShowDiscName");
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = jukeboxMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) jukeboxMap.size() / (double) gcPeriod);
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
                        jukeboxMap.remove(block);
                        return;
                    }
                    if (!block.getType().equals(Material.JUKEBOX)) {
                        Map<String, Object> map = entry.getValue();
                        if (map.get("Item") instanceof Item) {
                            Item item = (Item) map.get("Item");
                            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                        }
                        jukeboxMap.remove(block);
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
                Set<Block> list = nearbyJukeBox();
                for (Block block : list) {
                    if (jukeboxMap.get(block) == null && isActive(block.getLocation())) {
                        if (block.getType().equals(Material.JUKEBOX)) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("Item", "N/A");
                            jukeboxMap.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = jukeboxMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) jukeboxMap.size() / (double) checkingPeriod);
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
                    if (!block.getType().equals(Material.JUKEBOX)) {
                        return;
                    }
                    org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();

                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
                        ItemStack itemstack = jukebox.getRecord() == null ? null : (jukebox.getRecord().getType().equals(Material.AIR) ? null : jukebox.getRecord().clone());

                        if (entry.getValue().get("Item") instanceof String) {
                            if (itemstack != null) {
                                Item item = new Item(jukebox.getLocation().clone().add(0.5, 1.0, 0.5));

                                String disc = jukebox.getPlaying().toString();
                                Component text;
                                if (showDiscName) {
                                    if (itemstack.getItemMeta().hasDisplayName()) {
                                        text = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(getColor(disc) + itemstack.getItemMeta().getDisplayName()));
                                    } else {
                                        text = Component.translatable(TranslationUtils.getRecord(disc));
                                        text = text.color(ColorUtils.toTextColor(getColor(disc)));
                                    }
                                    item.setCustomName(text);
                                    item.setCustomNameVisible(true);
                                } else {
                                    item.setCustomName("");
                                    item.setCustomNameVisible(false);
                                }
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
                            Item item = (Item) entry.getValue().get("Item");
                            if (itemstack != null) {
                                if (!item.getItemStack().equals(itemstack)) {
                                    item.setItemStack(itemstack);
                                    String disc = jukebox.getPlaying().toString();
                                    Component text;
                                    if (showDiscName) {
                                        if (itemstack.getItemMeta().hasDisplayName()) {
                                            text = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(getColor(disc) + itemstack.getItemMeta().getDisplayName()));
                                        } else {
                                            text = Component.translatable(TranslationUtils.getRecord(disc));
                                            text = text.color(ColorUtils.toTextColor(getColor(disc)));
                                        }
                                        item.setCustomName(text);
                                        item.setCustomNameVisible(true);
                                    } else {
                                        item.setCustomName("");
                                        item.setCustomNameVisible(false);
                                    }
                                    PacketManager.updateItem(item);
                                }
                            } else {
                                entry.getValue().put("Item", "N/A");
                                PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                            }
                        }
                    });
                }, delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakJukeBox(TileEntityRemovedEvent event) {
        Block block = event.getBlock();
        if (!jukeboxMap.containsKey(block)) {
            return;
        }

        Map<String, Object> map = jukeboxMap.get(block);
        if (map.get("Item") instanceof Item) {
            Item item = (Item) map.get("Item");
            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
        }
        jukeboxMap.remove(block);
    }

    public Set<Block> nearbyJukeBox() {
        return TileEntityManager.getTileEntities(TileEntityType.JUKEBOX);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public ChatColor getColor(String material) {
        switch (material) {
            case "MUSIC_DISC_11":
                return ChatColor.WHITE;
            case "MUSIC_DISC_13":
                return ChatColor.GOLD;
            case "MUSIC_DISC_BLOCKS":
                return ChatColor.RED;
            case "MUSIC_DISC_CAT":
                return ChatColor.GREEN;
            case "MUSIC_DISC_CHIRP":
                return ChatColor.DARK_RED;
            case "MUSIC_DISC_FAR":
                return ChatColor.GREEN;
            case "MUSIC_DISC_MALL":
                return ChatColor.BLUE;
            case "MUSIC_DISC_MELLOHI":
                return ChatColor.LIGHT_PURPLE;
            case "MUSIC_DISC_STAL":
                return ChatColor.WHITE;
            case "MUSIC_DISC_STRAD":
                return ChatColor.WHITE;
            case "MUSIC_DISC_WAIT":
                return ChatColor.AQUA;
            case "MUSIC_DISC_WARD":
                return ChatColor.DARK_AQUA;
            case "MUSIC_DISC_PIGSTEP":
                return ChatColor.GOLD;
            case "MUSIC_DISC_OTHERSIDE":
                return ChatColor.BLUE;
            case "MUSIC_DISC_5":
                return ChatColor.DARK_AQUA;
            case "MUSIC_DISC_RELIC":
                return ChatColor.AQUA;
            case "MUSIC_DISC_CREATOR":
                return ChatColor.GREEN;
            case "MUSIC_DISC_CREATOR_MUSIC_BOX":
                return ChatColor.GOLD;
            case "MUSIC_DISC_PRECIPICE":
                return ChatColor.GREEN;
            default:
                return ChatColor.WHITE;
        }
    }

}
