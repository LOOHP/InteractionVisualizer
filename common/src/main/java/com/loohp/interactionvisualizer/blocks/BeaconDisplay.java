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
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.DynamicVisualizerEntity.PathType;
import com.loohp.interactionvisualizer.entityholders.SurroundingPlaneArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ColorUtils;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import com.loohp.interactionvisualizer.utils.RomanNumberUtils;
import com.loohp.interactionvisualizer.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BeaconDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("beacon");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0");

    private static Method paperGetEffectRangeMethod;

    static {
        try {
            paperGetEffectRangeMethod = Beacon.class.getMethod("getEffectRange");
        } catch (NoSuchMethodException e) {
            paperGetEffectRangeMethod = null;
        }
    }

    public Map<Block, Map<String, Object>> beaconMap = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;
    private PathType pathType = PathType.FACE;

    public BeaconDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Beacon.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
        pathType = PathType.valueOf(InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Beacon.PathType"));
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = beaconMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) beaconMap.size() / (double) gcPeriod);
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
                        beaconMap.remove(block);
                        return;
                    }
                    if (!block.getType().equals(Material.BEACON)) {
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
                        beaconMap.remove(block);
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
                Set<Block> list = nearbyBeacon();
                for (Block block : list) {
                    if (beaconMap.get(block) == null && isActive(block.getLocation())) {
                        if (block.getType().equals(Material.BEACON)) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("Item", "N/A");
                            map.putAll(spawnArmorStands(block));
                            beaconMap.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = beaconMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) beaconMap.size() / (double) checkingPeriod);
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
                    if (!block.getType().equals(Material.BEACON)) {
                        return;
                    }
                    org.bukkit.block.Beacon beacon = (org.bukkit.block.Beacon) block.getState();
                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
                        String arrow = "\u27f9";
                        String up = "\u25b2";
                        ChatColor color = getBeaconColor(block);
                        ArmorStand line1 = (ArmorStand) entry.getValue().get("1");
                        ArmorStand line2 = (ArmorStand) entry.getValue().get("2");
                        ArmorStand line3 = (ArmorStand) entry.getValue().get("3");

                        String one = color + up + beacon.getTier() + " " + arrow + " " + DECIMAL_FORMAT.format(getRange(beacon)) + "m";
                        if (beacon.getTier() == 0) {
                            if (!PlainTextComponentSerializer.plainText().serialize(line1.getCustomName()).equals("") || line1.isCustomNameVisible()) {
                                line1.setCustomName("");
                                line1.setCustomNameVisible(false);
                                PacketManager.updateArmorStandOnlyMeta(line1);
                            }
                            if (!PlainTextComponentSerializer.plainText().serialize(line2.getCustomName()).equals(one) || !line2.isCustomNameVisible()) {
                                line2.setCustomName(one);
                                line2.setCustomNameVisible(true);
                                PacketManager.updateArmorStandOnlyMeta(line2);
                            }
                            if (!PlainTextComponentSerializer.plainText().serialize(line3.getCustomName()).equals("") || line3.isCustomNameVisible()) {
                                line3.setCustomName("");
                                line3.setCustomNameVisible(false);
                                PacketManager.updateArmorStandOnlyMeta(line3);
                            }
                        } else {
                            Component primaryEffectText = null;
                            Component secondaryEffectText = null;
                            if (beacon.getPrimaryEffect() != null) {
                                TranslatableComponent effectTrans = Component.translatable(TranslationUtils.getEffect(beacon.getPrimaryEffect().getType()));
                                effectTrans = effectTrans.color(ColorUtils.toTextColor(color));
                                Component levelText = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(" " + color + RomanNumberUtils.toRoman(beacon.getPrimaryEffect().getAmplifier() + 1)));
                                effectTrans = effectTrans.append(levelText);
                                primaryEffectText = effectTrans;
                            }
                            if (beacon.getSecondaryEffect() != null) {
                                TranslatableComponent effectTrans = Component.translatable(TranslationUtils.getEffect(beacon.getSecondaryEffect().getType()));
                                effectTrans = effectTrans.color(ColorUtils.toTextColor(color));
                                Component levelText = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(" " + color + RomanNumberUtils.toRoman(beacon.getSecondaryEffect().getAmplifier() + 1)));
                                effectTrans = effectTrans.append(levelText);
                                secondaryEffectText = effectTrans;
                            }
                            if (secondaryEffectText == null) {
                                if (!PlainTextComponentSerializer.plainText().serialize(line1.getCustomName()).equals("") || line1.isCustomNameVisible()) {
                                    line1.setCustomName("");
                                    line1.setCustomNameVisible(false);
                                    PacketManager.updateArmorStandOnlyMeta(line1);
                                }
                                if (!PlainTextComponentSerializer.plainText().serialize(line2.getCustomName()).equals(one) || !line2.isCustomNameVisible()) {
                                    line2.setCustomName(one);
                                    line2.setCustomNameVisible(true);
                                    PacketManager.updateArmorStandOnlyMeta(line2);
                                }
                                if (primaryEffectText == null) {
                                    if (!PlainTextComponentSerializer.plainText().serialize(line3.getCustomName()).equals("") || line3.isCustomNameVisible()) {
                                        line3.setCustomName("");
                                        line3.setCustomNameVisible(false);
                                        PacketManager.updateArmorStandOnlyMeta(line3);
                                    }
                                } else {
                                    if (!line3.getCustomName().equals(primaryEffectText) || !line3.isCustomNameVisible()) {
                                        line3.setCustomName(primaryEffectText);
                                        line3.setCustomNameVisible(true);
                                        PacketManager.updateArmorStandOnlyMeta(line3);
                                    }
                                }
                            } else {
                                if (!PlainTextComponentSerializer.plainText().serialize(line1.getCustomName()).equals(one) || !line1.isCustomNameVisible()) {
                                    line1.setCustomName(one);
                                    line1.setCustomNameVisible(true);
                                    PacketManager.updateArmorStandOnlyMeta(line1);
                                }
                                if (primaryEffectText == null) {
                                    if (!PlainTextComponentSerializer.plainText().serialize(line2.getCustomName()).equals("") || line2.isCustomNameVisible()) {
                                        line2.setCustomName("");
                                        line2.setCustomNameVisible(false);
                                        PacketManager.updateArmorStandOnlyMeta(line2);
                                    }
                                } else {
                                    if (!line2.getCustomName().equals(primaryEffectText) || !line2.isCustomNameVisible()) {
                                        line2.setCustomName(primaryEffectText);
                                        line2.setCustomNameVisible(true);
                                        PacketManager.updateArmorStandOnlyMeta(line2);
                                    }
                                }
                                if (!line3.getCustomName().equals(secondaryEffectText) || !line3.isCustomNameVisible()) {
                                    line3.setCustomName(secondaryEffectText);
                                    line3.setCustomNameVisible(true);
                                    PacketManager.updateArmorStandOnlyMeta(line3);
                                }
                            }
                        }
                    });
                }, delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakBeacon(TileEntityRemovedEvent event) {
        Block block = event.getBlock();
        if (!beaconMap.containsKey(block)) {
            return;
        }

        Map<String, Object> map = beaconMap.get(block);
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
        beaconMap.remove(block);
    }

    public Set<Block> nearbyBeacon() {
        return TileEntityManager.getTileEntities(TileEntityType.BEACON);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public Map<String, ArmorStand> spawnArmorStands(Block block) {
        Map<String, ArmorStand> map = new HashMap<>();
        Location origin = block.getLocation().add(0.5, 0.25, 0.5);

        SurroundingPlaneArmorStand line1 = new SurroundingPlaneArmorStand(origin.clone().add(0.0, 0.25, 0.0), 0.7, pathType);
        setStand(line1);
        SurroundingPlaneArmorStand line2 = new SurroundingPlaneArmorStand(origin.clone(), 0.7, pathType);
        setStand(line2);
        SurroundingPlaneArmorStand line3 = new SurroundingPlaneArmorStand(origin.clone().add(0.0, -0.25, 0.0), 0.7, pathType);
        setStand(line3);

        map.put("1", line1);
        map.put("2", line2);
        map.put("3", line3);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), line1);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), line2);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), line3);

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
        stand.setRightArmPose(EulerAngle.ZERO);
    }

    public ChatColor getBeaconColor(Block block) {
        Block glass = block.getRelative(BlockFace.UP);
        switch (glass.getType()) {
            case ORANGE_STAINED_GLASS:
            case ORANGE_STAINED_GLASS_PANE:
                return ChatColor.GOLD;
            case MAGENTA_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS_PANE:
                return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
                return ChatColor.AQUA;
            case YELLOW_STAINED_GLASS:
            case YELLOW_STAINED_GLASS_PANE:
                return ChatColor.YELLOW;
            case LIME_STAINED_GLASS:
            case LIME_STAINED_GLASS_PANE:
                return ChatColor.GREEN;
            case PINK_STAINED_GLASS:
            case PINK_STAINED_GLASS_PANE:
                return ChatColor.LIGHT_PURPLE;
            case GRAY_STAINED_GLASS:
            case GRAY_STAINED_GLASS_PANE:
                return ChatColor.DARK_GRAY;
            case LIGHT_GRAY_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS_PANE:
                return ChatColor.GRAY;
            case CYAN_STAINED_GLASS:
            case CYAN_STAINED_GLASS_PANE:
                return ChatColor.DARK_AQUA;
            case PURPLE_STAINED_GLASS:
            case PURPLE_STAINED_GLASS_PANE:
                return ChatColor.DARK_PURPLE;
            case BLUE_STAINED_GLASS:
            case BLUE_STAINED_GLASS_PANE:
                return ChatColor.BLUE;
            case BROWN_STAINED_GLASS:
            case BROWN_STAINED_GLASS_PANE:
                return ChatColor.GOLD;
            case GREEN_STAINED_GLASS:
            case GREEN_STAINED_GLASS_PANE:
                return ChatColor.DARK_GREEN;
            case RED_STAINED_GLASS:
            case RED_STAINED_GLASS_PANE:
                return ChatColor.RED;
            case BLACK_STAINED_GLASS:
            case BLACK_STAINED_GLASS_PANE:
            case GLASS:
            case GLASS_PANE:
            case WHITE_STAINED_GLASS:
            case WHITE_STAINED_GLASS_PANE:
            default:
                return ChatColor.WHITE;
        }
    }

    public double getRange(Beacon beacon) {
        if (paperGetEffectRangeMethod != null) {
            try {
                return (double) paperGetEffectRangeMethod.invoke(beacon);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return beacon.getTier() * 10 + 10;
    }

}
