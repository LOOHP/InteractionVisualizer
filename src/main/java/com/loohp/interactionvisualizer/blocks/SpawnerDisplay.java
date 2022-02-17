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
import com.loohp.interactionvisualizer.entityholders.DynamicVisualizerEntity.PathType;
import com.loohp.interactionvisualizer.entityholders.SurroundingPlaneArmorStand;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnerDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("spawner");

    public ConcurrentHashMap<Block, Map<String, Object>> spawnerMap = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;
    private String progressBarCharacter = "";
    private String emptyColor = "&7";
    private String filledColor = "&e";
    private int progressBarLength = 10;
    private String spawnRange = "";
    private PathType pathType = PathType.FACE;

    public SpawnerDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Spawner.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
        progressBarCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Spawner.Options.ProgressBarCharacter"));
        emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Spawner.Options.EmptyColor"));
        filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Spawner.Options.FilledColor"));
        progressBarLength = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Spawner.Options.ProgressBarLength");
        spawnRange = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Spawner.Options.SpawnRange"));
        pathType = PathType.valueOf(InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Spawner.PathType"));
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = spawnerMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) spawnerMap.size() / (double) gcPeriod);
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
                        spawnerMap.remove(block);
                        return;
                    }
                    if (!isSpawner(block.getType())) {
                        Map<String, Object> map = entry.getValue();
                        if (map.get("1") instanceof ArmorStand) {
                            ArmorStand stand = (ArmorStand) map.get("1");
                            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                        }
                        spawnerMap.remove(block);
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
                Set<Block> list = nearbySpawner();
                for (Block block : list) {
                    if (spawnerMap.get(block) == null && isActive(block.getLocation())) {
                        if (isSpawner(block.getType())) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.putAll(spawnArmorStands(block));
                            spawnerMap.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = spawnerMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) spawnerMap.size() / (double) checkingPeriod);
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
                    if (!isSpawner(block.getType())) {
                        return;
                    }
                    org.bukkit.block.CreatureSpawner spawner = (org.bukkit.block.CreatureSpawner) block.getState();

                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
                        ArmorStand stand = (ArmorStand) entry.getValue().get("1");

                        int activeRange = spawner.getRequiredPlayerRange();

                        if (PlayerLocationManager.hasPlayerNearby(spawner.getLocation(), activeRange, false, player -> !player.getGameMode().equals(GameMode.SPECTATOR))) {
                            int max = spawner.getMaxSpawnDelay();
                            int time = max - spawner.getDelay();
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

                            symbol += spawnRange.replace("{SpawnRange}", spawner.getSpawnRange() + "");

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
                    });
                }, delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakSpawner(TileEntityRemovedEvent event) {
        Block block = event.getBlock();
        if (!spawnerMap.containsKey(block)) {
            return;
        }

        Map<String, Object> map = spawnerMap.get(block);
        if (map.get("1") instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) map.get("1");
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
        }
        spawnerMap.remove(block);
    }

    public Set<Block> nearbySpawner() {
        return TileEntityManager.getTileEntites(TileEntityType.SPAWNER);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public Map<String, ArmorStand> spawnArmorStands(Block block) {
        Map<String, ArmorStand> map = new HashMap<>();

        Location origin = block.getLocation();

        Location loc = origin.clone().add(0.5, 0.2, 0.5);
        SurroundingPlaneArmorStand slot1 = new SurroundingPlaneArmorStand(loc.clone(), 0.7, pathType);
        setStand(slot1);

        map.put("1", slot1);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), slot1);

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

    public boolean isSpawner(String material) {
        if (material.equalsIgnoreCase("SPAWNER")) {
            return true;
        }
        return material.equalsIgnoreCase("MOB_SPAWNER");
    }

    public boolean isSpawner(Material material) {
        return isSpawner(material.toString());
    }

}
