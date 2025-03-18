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
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.MaterialUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Crafter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
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

public class CrafterDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("crafter");

    public ConcurrentHashMap<Block, Map<String, Object>> crafterMap = new ConcurrentHashMap<>();
    private int checkingPeriod = 20;
    private int gcPeriod = 600;

    public CrafterDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Crafter.CheckingPeriod");
        gcPeriod = InteractionVisualizerAPI.getGCPeriod();
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, Map<String, Object>>> itr = crafterMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) crafterMap.size() / (double) gcPeriod);
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
                        for (int i = 1; i <= 9; i++) {
                            if (map.get(String.valueOf(i)) instanceof ArmorStand) {
                                ArmorStand stand = (ArmorStand) map.get(String.valueOf(i));
                                PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                            }
                        }
                        crafterMap.remove(block);
                        return;
                    }
                    if (!block.getType().equals(Material.CRAFTER) || getCardinalDirection(block) < 0F) {
                        Map<String, Object> map = entry.getValue();
                        for (int i = 1; i <= 9; i++) {
                            if (map.get(String.valueOf(i)) instanceof ArmorStand) {
                                ArmorStand stand = (ArmorStand) map.get(String.valueOf(i));
                                PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
                            }
                        }
                        crafterMap.remove(block);
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
                Set<Block> list = nearbyCrafter();
                for (Block block : list) {
                    if (crafterMap.get(block) == null && isActive(block.getLocation())) {
                        if (block.getType().equals(Material.CRAFTER) && getCardinalDirection(block) >= 0F) {
                            Map<String, Object> map = new HashMap<>(spawnArmorStands(block));
                            crafterMap.put(block, map);
                        }
                    }
                }
            });

            Iterator<Entry<Block, Map<String, Object>>> itr = crafterMap.entrySet().iterator();
            int count = 0;
            int maxper = (int) Math.ceil((double) crafterMap.size() / (double) checkingPeriod);
            int delay = 1;
            while (itr.hasNext()) {
                Entry<Block, Map<String, Object>> entry = itr.next();

                count++;
                if (count > maxper) {
                    count = 0;
                    delay++;
                }
                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> handleUpdate(entry.getKey(), entry.getValue()), delay);
            }
        }, 0, checkingPeriod).getTaskId();
    }

    public void handleUpdate(Block block, Map<String, Object> map) {
        Crafter crafter = (Crafter) block.getState();
        Inventory inventory = crafter.getInventory();

        ItemStack[] items = new ItemStack[] {
                inventory.getItem(0),
                inventory.getItem(1),
                inventory.getItem(2),
                inventory.getItem(3),
                inventory.getItem(4),
                inventory.getItem(5),
                inventory.getItem(6),
                inventory.getItem(7),
                inventory.getItem(8)
        };

        for (int i = 0; i < 9; i++) {
            ArmorStand stand = (ArmorStand) map.get(String.valueOf(i + 1));
            ItemStack item = items[i];
            if (crafter.isSlotDisabled(i)) {
                item = new ItemStack(Material.BARRIER);
            } else if (item == null || item.getType().equals(Material.AIR)) {
                item = null;
            }
            if (item != null) {
                boolean changed = true;
                if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialUtils.MaterialMode.BLOCK) && !standMode(stand).equals(MaterialUtils.MaterialMode.BLOCK)) {
                    toggleStandMode(stand, "Block");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialUtils.MaterialMode.TOOL) && !standMode(stand).equals(MaterialUtils.MaterialMode.TOOL)) {
                    toggleStandMode(stand, "Tool");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialUtils.MaterialMode.ITEM) && !standMode(stand).equals(MaterialUtils.MaterialMode.ITEM)) {
                    toggleStandMode(stand, "Item");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialUtils.MaterialMode.STANDING) && !standMode(stand).equals(MaterialUtils.MaterialMode.STANDING)) {
                    toggleStandMode(stand, "Standing");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialUtils.MaterialMode.LOWBLOCK) && !standMode(stand).equals(MaterialUtils.MaterialMode.LOWBLOCK)) {
                    toggleStandMode(stand, "LowBlock");
                } else {
                    changed = false;
                }
                if (!item.getType().equals(stand.getItemInMainHand().getType())) {
                    changed = true;
                    stand.setItemInMainHand(item);
                }
                if (changed) {
                    PacketManager.updateArmorStand(stand);
                }
            } else {
                if (!stand.getItemInMainHand().getType().equals(Material.AIR)) {
                    stand.setItemInMainHand(new ItemStack(Material.AIR));
                    PacketManager.updateArmorStand(stand);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrafterDropItem(BlockDispenseEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block.getType().equals(Material.CRAFTER)) {
            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                Map<String, Object> map = crafterMap.get(block);
                if (map != null) {
                    handleUpdate(block, map);
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCrafterMoveItems(InventoryMoveItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Location initiatorLocation = event.getInitiator().getLocation();
        if (initiatorLocation != null) {
            Block block = initiatorLocation.getBlock();
            if (block.getType().equals(Material.CRAFTER)) {
                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                    Map<String, Object> map = crafterMap.get(block);
                    if (map != null) {
                        handleUpdate(block, map);
                    }
                }, 1);
            }
        }
        Location destinationLocation = event.getDestination().getLocation();
        if (destinationLocation != null) {
            Block block = destinationLocation.getBlock();
            if (block.getType().equals(Material.CRAFTER)) {
                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                    Map<String, Object> map = crafterMap.get(block);
                    if (map != null) {
                        handleUpdate(block, map);
                    }
                }, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseCrafter(InventoryClickEvent event) {
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
        Block block = event.getView().getTopInventory().getLocation().getBlock();
        if (block == null) {
            return;
        }
        if (!block.getType().equals(Material.CRAFTER)) {
            return;
        }

        if (event.getRawSlot() >= 0 && event.getRawSlot() <= 2) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
        }
        Map<String, Object> map = crafterMap.get(block);
        if (map != null) {
            handleUpdate(block, map);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakCrafter(TileEntityRemovedEvent event) {
        Block block = event.getBlock();
        if (!crafterMap.containsKey(block)) {
            return;
        }

        Map<String, Object> map = crafterMap.get(block);
        for (int i = 1; i <= 9; i++) {
            if (map.get(String.valueOf(i)) instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) map.get(String.valueOf(i));
                PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
            }
        }
        crafterMap.remove(block);
    }

    public Set<Block> nearbyCrafter() {
        return TileEntityManager.getTileEntities(TileEntityType.CRAFTER);
    }

    public boolean isActive(Location loc) {
        return PlayerLocationManager.hasPlayerNearby(loc);
    }

    public MaterialUtils.MaterialMode standMode(ArmorStand stand) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (plain.startsWith("IV.Crafter.")) {
            return MaterialUtils.MaterialMode.getModeFromName(plain.substring(plain.lastIndexOf(".") + 1));
        }
        return null;
    }

    public void toggleStandMode(ArmorStand stand, String mode) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (!plain.equals("IV.Crafter.Item")) {
            if (plain.equals("IV.Crafter.Block")) {
                stand.setCustomName("IV.Crafter.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.084, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.102), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.14)));

            }
            if (plain.equals("IV.Crafter.LowBlock")) {
                stand.setCustomName("IV.Crafter.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.02, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.09), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.15)));

            }
            if (plain.equals("IV.Crafter.Tool")) {
                stand.setCustomName("IV.Crafter.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.1)));
                stand.teleport(stand.getLocation().add(0, 0.26, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
            if (plain.equals("IV.Crafter.Standing")) {
                stand.setCustomName("IV.Crafter.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(0.323), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(-0.115)));
                stand.teleport(stand.getLocation().add(0, 0.32, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
        }
        if (mode.equals("Block")) {
            stand.setCustomName("IV.Crafter.Block");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.14)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.102), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.084, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("LowBlock")) {
            stand.setCustomName("IV.Crafter.LowBlock");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.15)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.09), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.02, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("Tool")) {
            stand.setCustomName("IV.Crafter.Tool");
            stand.setRightArmPose(new EulerAngle(357.99, 0.0, 300.0));
            stand.teleport(stand.getLocation().add(0, -0.26, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.1)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.3), -90)));
        }
        if (mode.equals("Standing")) {
            stand.setCustomName("IV.Crafter.Standing");
            stand.setRightArmPose(new EulerAngle(0.0, 4.7, 4.7));
            stand.teleport(stand.getLocation().add(0, -0.32, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(0.115)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(-0.323), -90)));
        }
    }

    public Map<String, ArmorStand> spawnArmorStands(Block block) { //.add(0.68, 0.600781, 0.35)
        Map<String, ArmorStand> map = new HashMap<>();
        Location loc = block.getLocation().clone().add(0.5, 0.600781, 0.5);
        ArmorStand center = new ArmorStand(loc);
        float yaw = getCardinalDirection(block);
        center.setRotation(yaw, center.getLocation().getPitch());
        setStand(center);
        center.setCustomName("IV.Crafter.Center");
        Vector vector = rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.19), -100).add(center.getLocation().clone().getDirection().normalize().multiply(-0.11));
        ArmorStand slot5 = new ArmorStand(loc.clone().add(vector));
        setStand(slot5, yaw);
        ArmorStand slot2 = new ArmorStand(slot5.getLocation().clone().add(center.getLocation().clone().getDirection().normalize().multiply(0.2)));
        setStand(slot2, yaw);
        ArmorStand slot1 = new ArmorStand(slot2.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)));
        setStand(slot1, yaw);
        ArmorStand slot3 = new ArmorStand(slot2.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)));
        setStand(slot3, yaw);
        ArmorStand slot4 = new ArmorStand(slot5.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)));
        setStand(slot4, yaw);
        ArmorStand slot6 = new ArmorStand(slot5.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)));
        setStand(slot6, yaw);
        ArmorStand slot8 = new ArmorStand(slot5.getLocation().clone().add(center.getLocation().getDirection().clone().normalize().multiply(-0.2)));
        setStand(slot8, yaw);
        ArmorStand slot7 = new ArmorStand(slot8.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)));
        setStand(slot7, yaw);
        ArmorStand slot9 = new ArmorStand(slot8.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)));
        setStand(slot9, yaw);

        map.put("1", slot1);
        map.put("2", slot2);
        map.put("3", slot3);
        map.put("4", slot4);
        map.put("5", slot5);
        map.put("6", slot6);
        map.put("7", slot7);
        map.put("8", slot8);
        map.put("9", slot9);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot1);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot2);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot3);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot4);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot5);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot6);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot7);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot8);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot9);

        return map;
    }

    public void setStand(ArmorStand stand, float yaw) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setSilent(true);
        stand.setRightArmPose(EulerAngle.ZERO);
        stand.setCustomName("IV.Crafter.Item");
        stand.setRotation(yaw, stand.getLocation().getPitch());
    }

    public void setStand(ArmorStand stand) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
    }

    public Vector rotateVectorAroundY(Vector vector, double degrees) {
        double rad = Math.toRadians(degrees);

        double currentX = vector.getX();
        double currentZ = vector.getZ();

        double cosine = Math.cos(rad);
        double sine = Math.sin(rad);

        return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
    }

    public float getCardinalDirection(Block block) {
        org.bukkit.block.data.type.Crafter crafter = (org.bukkit.block.data.type.Crafter) block.getBlockData();
        String[] parts = crafter.getOrientation().name().split("_");
        BlockFace facing = BlockFace.valueOf(parts[0]);
        BlockFace grid = BlockFace.valueOf(parts[1]);
        if (grid.getModY() == 0) {
            return -1F;
        }

        double rotation = (Math.atan2(facing.getDirection().getZ(), facing.getDirection().getX()) + 90.0F) % 360.0F;

        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if ((0.0D <= rotation) && (rotation < 45.0D)) {
            return 90.0F;
        }
        if ((45.0D <= rotation) && (rotation < 135.0D)) {
            return 180.0F;
        }
        if ((135.0D <= rotation) && (rotation < 225.0D)) {
            return -90.0F;
        }
        if ((225.0D <= rotation) && (rotation < 315.0D)) {
            return 0.0F;
        }
        if ((315.0D <= rotation) && (rotation < 360.0D)) {
            return 90.0F;
        }
        return 0.0F;
    }

}
