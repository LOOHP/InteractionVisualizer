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
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SoulCampfireDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("soul_campfire");

    public ConcurrentHashMap<Block, Map<String, Object>> soulcampfireMap = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;
    private String progressBarCharacter = "";
    private String emptyColor = "&7";
    private String filledColor = "&e";
    private int progressBarLength = 10;

    public SoulCampfireDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.SoulCampfire.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
        progressBarCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.SoulCampfire.Options.ProgressBarCharacter"));
        emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.SoulCampfire.Options.EmptyColor"));
        filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.SoulCampfire.Options.FilledColor"));
        progressBarLength = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.SoulCampfire.Options.ProgressBarLength");
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = soulcampfireMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) soulcampfireMap.size() / (double) gcPeriod);
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
                        if (map.get("1") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("1");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("2") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("2");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("3") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("3");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("4") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("4");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        soulcampfireMap.remove(block);
                        return;
                    }
                    if (!block.getType().equals(Material.SOUL_CAMPFIRE)) {
                        Map<String, Object> map = entry.getValue();
                        if (map.get("1") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("1");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("2") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("2");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("3") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("3");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        if (map.get("4") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("4");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        soulcampfireMap.remove(block);
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
                Set<Block> list = nearbySoulCampfire();
                for (Block block : list) {
                    if (soulcampfireMap.get(block) == null && isActive(block.getLocation())) {
                        if (block.getType().equals(Material.SOUL_CAMPFIRE)) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.putAll(spawnArmorStands(block));
                            soulcampfireMap.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = soulcampfireMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) soulcampfireMap.size() / (double) checkingPeriod);
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
                    if (!block.getType().equals(Material.SOUL_CAMPFIRE)) {
                        return;
                    }
                    org.bukkit.block.Campfire soulcampfire = (org.bukkit.block.Campfire) block.getState();
                    boolean isLit = ((Campfire) block.getBlockData()).isLit();

                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
                        ItemStack itemstack1 = soulcampfire.getItem(0);
                        if (itemstack1 != null) {
                            if (itemstack1.getType().equals(Material.AIR)) {
                                itemstack1 = null;
                            }
                        }
                        ItemStack itemstack2 = soulcampfire.getItem(1);
                        if (itemstack2 != null) {
                            if (itemstack2.getType().equals(Material.AIR)) {
                                itemstack2 = null;
                            }
                        }
                        ItemStack itemstack3 = soulcampfire.getItem(2);
                        if (itemstack3 != null) {
                            if (itemstack3.getType().equals(Material.AIR)) {
                                itemstack3 = null;
                            }
                        }
                        ItemStack itemstack4 = soulcampfire.getItem(3);
                        if (itemstack4 != null) {
                            if (itemstack4.getType().equals(Material.AIR)) {
                                itemstack4 = null;
                            }
                        }

                        ArmorStand stand1 = (ArmorStand) entry.getValue().get("1");
                        ArmorStand stand2 = (ArmorStand) entry.getValue().get("2");
                        ArmorStand stand3 = (ArmorStand) entry.getValue().get("3");
                        ArmorStand stand4 = (ArmorStand) entry.getValue().get("4");

                        if (isLit && itemstack1 != null) {
                            int time = soulcampfire.getCookTime(0);
                            int max = soulcampfire.getCookTimeTotal(0);
                            String symbol = "";
                            double percentagescaled = (double) time / (double) max * (double) progressBarLength;
                            double i = 1;
                            for (i = 1; i < percentagescaled; i++) {
                                symbol = symbol + filledColor + progressBarCharacter;
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

                            if (!PlainTextComponentSerializer.plainText().serialize(stand1.getCustomName()).equals(symbol) || !stand1.isCustomNameVisible()) {
                                stand1.setCustomNameVisible(true);
                                stand1.setCustomName(symbol);
                                PacketManager.updateArmorStandOnlyMeta(stand1);
                            }
                        } else {
                            if (!PlainTextComponentSerializer.plainText().serialize(stand1.getCustomName()).equals("") || stand1.isCustomNameVisible()) {
                                stand1.setCustomNameVisible(false);
                                stand1.setCustomName("");
                                PacketManager.updateArmorStandOnlyMeta(stand1);
                            }
                        }
                        if (isLit && itemstack2 != null) {
                            int time = soulcampfire.getCookTime(1);
                            int max = soulcampfire.getCookTimeTotal(1);
                            String symbol = "";
                            double percentagescaled = (double) time / (double) max * (double) progressBarLength;
                            double i = 1;
                            for (i = 1; i < percentagescaled; i++) {
                                symbol = symbol + filledColor + progressBarCharacter;
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

                            if (!PlainTextComponentSerializer.plainText().serialize(stand2.getCustomName()).equals(symbol) || !stand2.isCustomNameVisible()) {
                                stand2.setCustomNameVisible(true);
                                stand2.setCustomName(symbol);
                                PacketManager.updateArmorStandOnlyMeta(stand2);
                            }
                        } else {
                            if (!PlainTextComponentSerializer.plainText().serialize(stand2.getCustomName()).equals("") || stand2.isCustomNameVisible()) {
                                stand2.setCustomNameVisible(false);
                                stand2.setCustomName("");
                                PacketManager.updateArmorStandOnlyMeta(stand2);
                            }
                        }
                        if (isLit && itemstack3 != null) {
                            int time = soulcampfire.getCookTime(2);
                            int max = soulcampfire.getCookTimeTotal(2);
                            String symbol = "";
                            double percentagescaled = (double) time / (double) max * (double) progressBarLength;
                            double i = 1;
                            for (i = 1; i < percentagescaled; i++) {
                                symbol = symbol + filledColor + progressBarCharacter;
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

                            if (!PlainTextComponentSerializer.plainText().serialize(stand3.getCustomName()).equals(symbol) || !stand3.isCustomNameVisible()) {
                                stand3.setCustomNameVisible(true);
                                stand3.setCustomName(symbol);
                                PacketManager.updateArmorStandOnlyMeta(stand3);
                            }
                        } else {
                            if (!PlainTextComponentSerializer.plainText().serialize(stand3.getCustomName()).equals("") || stand3.isCustomNameVisible()) {
                                stand3.setCustomNameVisible(false);
                                stand3.setCustomName("");
                                PacketManager.updateArmorStandOnlyMeta(stand3);
                            }
                        }
                        if (isLit && itemstack4 != null) {
                            int time = soulcampfire.getCookTime(3);
                            int max = soulcampfire.getCookTimeTotal(3);
                            String symbol = "";
                            double percentagescaled = (double) time / (double) max * (double) progressBarLength;
                            double i = 1;
                            for (i = 1; i < percentagescaled; i++) {
                                symbol = symbol + filledColor + progressBarCharacter;
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

                            if (!PlainTextComponentSerializer.plainText().serialize(stand4.getCustomName()).equals(symbol) || !stand4.isCustomNameVisible()) {
                                stand4.setCustomNameVisible(true);
                                stand4.setCustomName(symbol);
                                PacketManager.updateArmorStandOnlyMeta(stand4);
                            }
                        } else {
                            if (!PlainTextComponentSerializer.plainText().serialize(stand4.getCustomName()).equals("") || stand4.isCustomNameVisible()) {
                                stand4.setCustomNameVisible(false);
                                stand4.setCustomName("");
                                PacketManager.updateArmorStandOnlyMeta(stand4);
                            }
                        }
                    });
                }, delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakSoulCampfire(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!soulcampfireMap.containsKey(block)) {
            return;
        }

        Map<String, Object> map = soulcampfireMap.get(block);
        if (map.get("1") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("1");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        if (map.get("2") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("2");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        if (map.get("3") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("3");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        if (map.get("4") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("4");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        soulcampfireMap.remove(block);
    }

    public Set<Block> nearbySoulCampfire() {
        return TileEntityManager.getTileEntites(TileEntityType.SOUL_CAMPFIRE);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public Map<String, ArmorStand> spawnArmorStands(Block block) {
        Map<String, ArmorStand> map = new HashMap<>();

        Location origin = block.getLocation();
        BlockData blockData = block.getState().getBlockData();
        BlockFace facing = ((Directional) blockData).getFacing();
        Location target = block.getRelative(facing).getLocation();
        Vector direction = rotateVectorAroundY(target.toVector().subtract(origin.toVector()).multiply(0.44194173), 135);

        Location loc = origin.clone().add(0.5, 0.3, 0.5);
        ArmorStand slot1 = new ArmorStand(loc.clone().add(direction));
        setStand(slot1);
        ArmorStand slot2 = new ArmorStand(loc.clone().add(rotateVectorAroundY(direction.clone(), 90)));
        setStand(slot2);
        ArmorStand slot3 = new ArmorStand(loc.clone().add(rotateVectorAroundY(direction.clone(), 180)));
        setStand(slot3);
        ArmorStand slot4 = new ArmorStand(loc.clone().add(rotateVectorAroundY(direction.clone(), -90)));
        setStand(slot4);

        map.put("1", slot1);
        map.put("2", slot2);
        map.put("3", slot3);
        map.put("4", slot4);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot1);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot2);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot3);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot4);

        return map;
    }

    public void setStand(ArmorStand stand) {
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setCustomName("");
        stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
    }

    public Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);

        double currentX = vector.getX();
        double currentZ = vector.getZ();

        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);

        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }

}
