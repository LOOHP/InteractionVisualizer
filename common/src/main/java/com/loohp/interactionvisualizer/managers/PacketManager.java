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

package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.DynamicVisualizerEntity;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.entityholders.ItemFrame;
import com.loohp.interactionvisualizer.entityholders.VisualizerEntity;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.utils.LineOfSightUtils;
import com.loohp.interactionvisualizer.utils.LocationUtils;
import com.loohp.interactionvisualizer.utils.SyncUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PacketManager implements Listener {

    private static final Vector VECTOR_ZERO = new Vector(0.0, 0.0, 0.0);
    private static final Plugin plugin = InteractionVisualizer.plugin;
    private static final Map<VisualizerEntity, Integer> cache = new ConcurrentHashMap<>();
    private static final Map<DynamicVisualizerEntity, Set<Player>> dynamicTracking = new ConcurrentHashMap<>();
    public static Map<VisualizerEntity, Collection<Player>> active = new ConcurrentHashMap<>();
    public static Map<VisualizerEntity, Boolean> loaded = new ConcurrentHashMap<>();
    public static Map<Player, Set<VisualizerEntity>> playerStatus = new ConcurrentHashMap<>();

    public static void run() {
        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            for (Entry<VisualizerEntity, Boolean> entry : loaded.entrySet()) {
                VisualizerEntity entity = entry.getKey();
                if (entity instanceof ArmorStand) {
                    ArmorStand stand = (ArmorStand) entity;
                    if (PlayerLocationManager.hasPlayerNearby(stand.getLocation())) {
                        Collection<Player> players = active.get(entity);
                        if (entry.getValue()) {
                            if (players != null) {
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(stand.getLocation()) && isOccluding(stand.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand, false, false);
                                        loaded.put(entity, false);
                                    }
                                });
                            }
                        } else {
                            if (players != null) {
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(stand.getLocation()) && !isOccluding(stand.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        sendArmorStandSpawn(players, stand);
                                        updateArmorStand(stand);
                                        loaded.put(entity, true);
                                    }
                                });
                            }
                        }
                    }
                } else if (entity instanceof Item) {
                    Item item = (Item) entity;
                    if (PlayerLocationManager.hasPlayerNearby(item.getLocation())) {
                        Collection<Player> players = active.get(entity);
                        if (entry.getValue()) {
                            if (players != null) {
                                if (item.getVelocity().equals(VECTOR_ZERO) && !item.hasGravity()) {
                                    updateItemAsync(item, true);
                                }
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(item.getLocation()) && isOccluding(item.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        removeItem(InteractionVisualizerAPI.getPlayers(), item, false, false);
                                        loaded.put(entity, false);
                                    }
                                });
                            }
                        } else {
                            if (players != null) {
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(item.getLocation()) && !isOccluding(item.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        sendItemSpawn(players, item);
                                        updateItem(item);
                                        loaded.put(entity, true);
                                    }
                                });
                            }
                        }
                    }
                } else if (entity instanceof ItemFrame) {
                    ItemFrame frame = (ItemFrame) entity;
                    if (PlayerLocationManager.hasPlayerNearby(frame.getLocation())) {
                        Collection<Player> players = active.get(entity);
                        if (entry.getValue()) {
                            if (players != null) {
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(frame.getLocation()) && isOccluding(frame.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        removeItemFrame(InteractionVisualizerAPI.getPlayers(), frame, false, false);
                                        loaded.put(entity, false);
                                    }
                                });
                            }
                        } else {
                            if (players != null) {
                                SyncUtils.runAsyncWithSyncCondition(() -> LocationUtils.isLoaded(frame.getLocation()) && !isOccluding(frame.getLocation().getBlock().getType()), () -> {
                                    if (active.containsKey(entity)) {
                                        sendItemFrameSpawn(players, frame);
                                        updateItemFrame(frame);
                                        loaded.put(entity, true);
                                    }
                                });
                            }
                        }
                    }
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
            run();
        });
    }

    public static void dynamicEntity() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<DynamicVisualizerEntity> itr = dynamicTracking.keySet().iterator();
            while (itr.hasNext()) {
                DynamicVisualizerEntity entity = itr.next();
                Set<Player> players = dynamicTracking.get(entity);
                if (players == null || players.isEmpty()) {
                    itr.remove();
                    continue;
                }
                Iterator<Player> itr2 = players.iterator();
                while (itr2.hasNext()) {
                    Player player = itr2.next();
                    Location location = player.getEyeLocation();
                    if (!location.getWorld().equals(entity.getWorld())) {
                        itr2.remove();
                    } else {
                        NMS.getInstance().teleportEntity(player, entity.getEntityId(), entity.getViewingLocation(location, location.getDirection()));
                    }
                }
            }
        }, 0, 2);
    }

    private static boolean isOccluding(Material material) {
        if (InteractionVisualizer.exemptBlocks.contains(material.toString().toUpperCase())) {
            return false;
        }
        return material.isOccluding();
    }

    public static void update() {
        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    Set<VisualizerEntity> activeList = playerStatus.get(player);
                    if (activeList != null) {
                        Collection<Player> playerList = new HashSet<>();
                        playerList.add(player);

                        Location playerLocation = PlayerLocationManager.getPlayerLocation(player);
                        Location playerEyeLocation = PlayerLocationManager.getPlayerEyeLocation(player);
                        for (Entry<VisualizerEntity, Collection<Player>> entry : active.entrySet()) {
                            VisualizerEntity entity = entry.getKey();
                            int range = InteractionVisualizer.playerTrackingRange.getOrDefault(entity.getWorld(), 64);
                            range *= range;

                            boolean playerActive = activeList.contains(entity);
                            boolean sameWorld = entity.getWorld().equals(playerLocation.getWorld());
                            boolean inRange = sameWorld && (entity.getLocation().distanceSquared(playerLocation) <= range);
                            Boolean hasLineOfSight = InteractionVisualizer.hideIfObstructed ? null : true;
                            boolean isLoaded = loaded.getOrDefault(entity, false);
                            Location entityCenter = entity.getLocation();
                            entityCenter.setY(entityCenter.getY() + (entity instanceof Item ? (entity.getHeight() * 1.7) : (entity.getHeight() * 0.7)));

                            if (playerActive && (!sameWorld || !inRange || !(hasLineOfSight != null ? hasLineOfSight : (hasLineOfSight = LineOfSightUtils.hasLineOfSight(playerEyeLocation, entityCenter))))) {
                                if (entity instanceof ArmorStand) {
                                    ArmorStand stand = (ArmorStand) entity;
                                    removeArmorStand(playerList, stand, false, true);
                                } else if (entity instanceof Item) {
                                    Item item = (Item) entity;
                                    removeItem(playerList, item, false, true);
                                } else if (entity instanceof ItemFrame) {
                                    ItemFrame frame = (ItemFrame) entity;
                                    removeItemFrame(playerList, frame, false, true);
                                }
                            } else if (!playerActive && entry.getValue().contains(player) && isLoaded && sameWorld && inRange && (hasLineOfSight != null ? hasLineOfSight : (hasLineOfSight = LineOfSightUtils.hasLineOfSight(playerEyeLocation, entityCenter)))) {
                                if (entity instanceof ArmorStand) {
                                    ArmorStand stand = (ArmorStand) entity;
                                    sendArmorStandSpawn(playerList, stand);
                                    updateArmorStand(playerList, stand);
                                } else if (entity instanceof Item) {
                                    Item item = (Item) entity;
                                    sendItemSpawn(playerList, item);
                                    updateItem(playerList, item);
                                } else if (entity instanceof ItemFrame) {
                                    ItemFrame frame = (ItemFrame) entity;
                                    sendItemFrameSpawn(playerList, frame);
                                    updateItemFrame(playerList, frame);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
            update();
        });
    }

    public static void sendHandMovement(Collection<Player> players, Player entity) {
        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().sendHandMovement(playersInRange, entity);
        });
    }

    public static void sendArmorStandSpawn(Collection<Player> players, ArmorStand entity) {
        if (!active.containsKey(entity)) {
            active.put(entity, players);
            loaded.put(entity, true);
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().spawnArmorStand(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.add(entity);
                }
            });
            if (entity instanceof DynamicVisualizerEntity) {
                boolean absent = false;
                Set<Player> tracking = dynamicTracking.get((DynamicVisualizerEntity) entity);
                if (tracking == null) {
                    tracking = ConcurrentHashMap.newKeySet();
                    absent = true;
                }
                tracking.addAll(playersInRange);
                if (absent) {
                    dynamicTracking.put((DynamicVisualizerEntity) entity, tracking);
                }
            }
        });
    }

    public static void updateArmorStand(ArmorStand entity) {
        Collection<Player> players = active.get(entity);
        if (players == null) {
            return;
        }
        updateArmorStand(players, entity);
    }

    public static void updateArmorStand(Collection<Player> players, ArmorStand entity) {
        updateArmorStand(players, entity, false);
    }

    public static void updateArmorStand(Collection<Player> players, ArmorStand entity, boolean bypasscache) {
        if (!bypasscache) {
            Integer lastCode = cache.get(entity);
            if (lastCode != null) {
                if (lastCode == entity.cacheCode()) {
                    return;
                }
            }
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().updateArmorStand(playersInRange, entity);
        });

        cache.put(entity, entity.cacheCode());
    }

    public static void updateArmorStandOnlyMeta(ArmorStand entity) {
        Collection<Player> players = active.get(entity);
        if (players == null) {
            return;
        }
        updateArmorStandOnlyMeta(players, entity);
    }

    public static void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity) {
        updateArmorStandOnlyMeta(players, entity, false);
    }

    public static void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity, boolean bypasscache) {
        if (!bypasscache) {
            Integer lastCode = cache.get(entity);
            if (lastCode != null) {
                if (lastCode == entity.cacheCode()) {
                    return;
                }
            }
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().updateArmorStandOnlyMeta(playersInRange, entity);
        });

        cache.put(entity, entity.cacheCode());
    }

    public static void removeArmorStand(Collection<Player> players, ArmorStand entity, boolean removeFromActive, boolean bypassFilter) {
        if (removeFromActive) {
            active.remove(entity);
            loaded.remove(entity);
            cache.remove(entity);
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = bypassFilter ? players : PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().removeArmorStand(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.remove(entity);
                }
            });
            if (entity instanceof DynamicVisualizerEntity) {
                Set<Player> tracking = dynamicTracking.get((DynamicVisualizerEntity) entity);
                if (tracking != null) {
                    tracking.removeAll(playersInRange);
                }
            }
        });
    }

    public static void removeArmorStand(Collection<Player> players, ArmorStand entity) {
        removeArmorStand(players, entity, true, false);
    }

    public static void sendItemSpawn(Collection<Player> players, Item entity) {
        if (!active.containsKey(entity)) {
            active.put(entity, players);
            loaded.put(entity, true);
        }
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().spawnItem(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.add(entity);
                }
            });
        });
    }

    public static void updateItem(Item entity) {
        Collection<Player> players = active.get(entity);
        if (players == null) {
            return;
        }
        updateItem(players, entity);
    }

    public static void updateItem(Collection<Player> players, Item entity) {
        updateItem(players, entity, false);
    }

    public static void updateItem(Collection<Player> players, Item entity, boolean bypasscache) {
        if (!bypasscache) {
            Integer lastCode = cache.get(entity);
            if (lastCode != null) {
                if (lastCode == entity.cacheCode()) {
                    return;
                }
            }
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().updateItem(playersInRange, entity);
        });

        cache.put(entity, entity.cacheCode());
    }

    public static void updateItemAsync(Item entity, boolean bypasscache) {
        Collection<Player> players = active.get(entity);
        if (players == null) {
            return;
        }
        updateItemAsync(players, entity, bypasscache);
    }

    public static void updateItemAsync(Collection<Player> players, Item entity, boolean bypasscache) {
        if (!bypasscache) {
            Integer lastCode = cache.get(entity);
            if (lastCode != null) {
                if (lastCode == entity.cacheCode()) {
                    return;
                }
            }
        }

        if (!plugin.isEnabled()) {
            return;
        }

        Runnable task = () -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().updateItem(playersInRange, entity);
        };

        if (InteractionVisualizer.allPacketsSync) {
            Bukkit.getScheduler().runTask(plugin, task);
        } else {
            InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(task);
        }

        cache.put(entity, entity.cacheCode());
    }

    public static void removeItem(Collection<Player> players, Item entity, boolean removeFromActive, boolean bypassFilter) {
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }
        if (removeFromActive) {
            active.remove(entity);
            loaded.remove(entity);
            cache.remove(entity);
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = bypassFilter ? players : PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().removeItem(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.remove(entity);
                }
            });
        });
    }

    public static void removeItem(Collection<Player> players, Item entity) {
        removeItem(players, entity, true, false);
    }

    public static void sendItemFrameSpawn(Collection<Player> players, ItemFrame entity) {
        if (!active.containsKey(entity)) {
            active.put(entity, players);
            loaded.put(entity, true);
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().spawnItemFrame(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.add(entity);
                }
            });
        });
    }

    public static void updateItemFrame(ItemFrame entity) {
        Collection<Player> players = active.get(entity);
        if (players == null) {
            return;
        }

        updateItemFrame(players, entity);
    }

    public static void updateItemFrame(Collection<Player> players, ItemFrame entity) {
        updateItemFrame(players, entity, false);
    }

    public static void updateItemFrame(Collection<Player> players, ItemFrame entity, boolean bypasscache) {
        if (!bypasscache) {
            Integer lastCode = cache.get(entity);
            if (lastCode != null) {
                if (lastCode == entity.cacheCode()) {
                    return;
                }
            }
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().updateItemFrame(playersInRange, entity);
        });

        cache.put(entity, entity.cacheCode());
    }

    public static void removeItemFrame(Collection<Player> players, ItemFrame entity, boolean removeFromActive, boolean bypassFilter) {
        if (removeFromActive) {
            active.remove(entity);
            loaded.remove(entity);
            cache.remove(entity);
        }

        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> playersInRange = bypassFilter ? players : PlayerLocationManager.filterOutOfRange(players, entity);
            NMS.getInstance().removeItemFrame(playersInRange, entity);
            playersInRange.forEach((each) -> {
                Set<VisualizerEntity> list = playerStatus.get(each);
                if (list != null) {
                    list.remove(entity);
                }
            });
        });
    }

    public static void removeItemFrame(Collection<Player> players, ItemFrame entity) {
        removeItemFrame(players, entity, true, false);
    }

    public static void reset(Player theplayer) {
        Bukkit.getScheduler().runTask(plugin, () -> removeAll(theplayer));
        int delay = 10 + (int) Math.ceil((double) active.size() / 5.0);
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendPlayerPackets(theplayer), delay);
    }

    public static void removeAll(Player theplayer) {
        playerStatus.put(theplayer, ConcurrentHashMap.newKeySet());
        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> player = new HashSet<>();
            player.add(theplayer);
            int count = 0;
            int delay = 1;
            for (Entry<VisualizerEntity, Collection<Player>> entry : active.entrySet()) {
                count++;
                if (count > 5) {
                    delay++;
                    count = 0;
                }
                VisualizerEntity entity = entry.getKey();
                if (entity instanceof ArmorStand) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> removeArmorStand(player, (ArmorStand) entity, false, false), delay);
                }
                if (entity instanceof Item) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> removeItem(player, (Item) entity, false, false), delay);
                }
                if (entity instanceof ItemFrame) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> removeItemFrame(player, (ItemFrame) entity, false, false), delay);
                }
            }
        });
    }

    public static void sendPlayerPackets(Player theplayer) {
        playerStatus.put(theplayer, Collections.newSetFromMap(new ConcurrentHashMap<VisualizerEntity, Boolean>()));
        if (!plugin.isEnabled()) {
            return;
        }
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            Collection<Player> player = new HashSet<>();
            player.add(theplayer);
            int count = 0;
            int delay = 1;
            for (Entry<VisualizerEntity, Collection<Player>> entry : active.entrySet()) {
                VisualizerEntity entity = entry.getKey();
                if (entry.getValue().contains(theplayer)) {
                    Boolean isLoaded = loaded.get(entity);
                    if (isLoaded != null && isLoaded) {
                        count++;
                        if (count > 5) {
                            delay++;
                            count = 0;
                        }
                        if (entity instanceof ArmorStand) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                sendArmorStandSpawn(player, (ArmorStand) entity);
                                updateArmorStand(player, (ArmorStand) entity, true);
                            }, delay);
                        }
                        if (entity instanceof Item) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                sendItemSpawn(player, (Item) entity);
                                updateItem(player, (Item) entity, true);
                            }, delay);
                        }
                        if (entity instanceof ItemFrame) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                sendItemFrameSpawn(player, (ItemFrame) entity);
                                updateItemFrame(player, (ItemFrame) entity, true);
                            }, delay);
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerStatus.put(event.getPlayer(), ConcurrentHashMap.newKeySet());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        playerStatus.put(event.getPlayer(), ConcurrentHashMap.newKeySet());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        playerStatus.remove(event.getPlayer());
    }

}
