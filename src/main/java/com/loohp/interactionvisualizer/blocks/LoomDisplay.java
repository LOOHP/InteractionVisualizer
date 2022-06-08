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
import com.loohp.interactionvisualizer.api.VisualizerInteractDisplay;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.SoundManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.LightType;
import com.loohp.interactionvisualizer.utils.InventoryUtils;
import com.loohp.interactionvisualizer.utils.LocationUtils;
import com.loohp.interactionvisualizer.utils.VanishUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoomDisplay extends VisualizerInteractDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("loom");

    public Map<Block, Map<String, Object>> openedLooms = new HashMap<>();

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int run() {
        return new BukkitRunnable() {
            public void run() {

                Iterator<Block> itr = openedLooms.keySet().iterator();
                int count = 0;
                int maxper = (int) Math.ceil((double) openedLooms.size() / (double) 5);
                int delay = 1;
                while (itr.hasNext()) {
                    count++;
                    if (count > maxper) {
                        count = 0;
                        delay++;
                    }
                    Block block = itr.next();
                    new BukkitRunnable() {
                        public void run() {
                            if (!openedLooms.containsKey(block)) {
                                return;
                            }
                            Map<String, Object> map = openedLooms.get(block);
                            if (block.getType().equals(Material.LOOM)) {
                                Player player = (Player) map.get("Player");
                                if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                                    if (player.getOpenInventory() != null) {
                                        if (player.getOpenInventory().getTopInventory() != null) {
                                            if (player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }

                            if (map.get("Banner") instanceof ArmorStand) {
                                ArmorStand entity = (ArmorStand) map.get("Banner");
                                InteractionVisualizer.lightManager.deleteLight(entity.getLocation());
                                PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), entity);
                            }
                            openedLooms.remove(block);
                        }
                    }.runTaskLater(InteractionVisualizer.plugin, delay);
                }
            }
        }.runTaskTimer(InteractionVisualizer.plugin, 0, 5).getTaskId();
    }

    @Override
    public void process(Player player) {
        if (VanishUtils.isVanished(player)) {
            return;
        }
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (player.getOpenInventory().getTopInventory().getLocation() == null) {
            return;
        }
        if (!LocationUtils.isLoaded(player.getOpenInventory().getTopInventory().getLocation())) {
            return;
        }
        if (player.getOpenInventory().getTopInventory().getLocation().getBlock() == null) {
            return;
        }
        if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
            return;
        }

        InventoryView view = player.getOpenInventory();
        Block block = view.getTopInventory().getLocation().getBlock();
        if (!openedLooms.containsKey(block)) {
            Map<String, Object> map = new HashMap<>();
            map.put("Player", player);
            map.putAll(spawnArmorStands(player, block));
            openedLooms.put(block, map);
        }
        Map<String, Object> map = openedLooms.get(block);

        if (!map.get("Player").equals(player)) {
            return;
        }

        ItemStack input = view.getItem(0);
        if (input != null) {
            if (input.getType().equals(Material.AIR)) {
                input = null;
            }
        }
        ItemStack output = view.getItem(3);
        if (output != null) {
            if (output.getType().equals(Material.AIR)) {
                output = null;
            }
        }

        ItemStack item = null;
        if (output == null) {
            if (input != null) {
                item = input;
            }
        } else {
            item = output;
        }

        ArmorStand stand = (ArmorStand) map.get("Banner");
        if (item != null) {
            if (!item.isSimilar(stand.getHelmet())) {
                stand.setHelmet(item);
                PacketManager.updateArmorStand(stand);
            }
        } else {
            if (!stand.getHelmet().getType().equals(Material.AIR)) {
                stand.setHelmet(new ItemStack(Material.AIR));
                PacketManager.updateArmorStand(stand);
            }
        }

        Location loc1 = ((ArmorStand) map.get("Banner")).getLocation();
        InteractionVisualizer.lightManager.deleteLight(loc1);
        int skylight = loc1.getBlock().getRelative(BlockFace.UP).getLightFromSky();
        int blocklight = loc1.getBlock().getRelative(BlockFace.UP).getLightFromBlocks() - 1;
        blocklight = Math.max(blocklight, 0);
        if (skylight > 0) {
            InteractionVisualizer.lightManager.createLight(loc1, skylight, LightType.SKY);
        }
        if (blocklight > 0) {
            InteractionVisualizer.lightManager.createLight(loc1, blocklight, LightType.BLOCK);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoom(InventoryClickEvent event) {
        if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        if (event.getRawSlot() != 0 && event.getRawSlot() != 3) {
            return;
        }
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.AIR)) {
            return;
        }
        if (event.getRawSlot() == 3) {
            if (event.getCursor() != null) {
                if (!event.getCursor().getType().equals(Material.AIR)) {
                    if (event.getCursor().getAmount() >= event.getCursor().getType().getMaxStackSize()) {
                        return;
                    }
                }
            }
        } else {
            if (event.getCursor() != null) {
                if (event.getCursor().getType().equals(event.getCurrentItem().getType())) {
                    return;
                }
            }
        }

        if (event.isShiftClick()) {
            if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(event.getRawSlot()).getType())) {
                return;
            }
        }
        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
            if (event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null && !event.getWhoClicked().getInventory().getItem(event.getHotbarButton()).getType().equals(Material.AIR)) {
                return;
            }
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
        if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
            return;
        }

        Block block = event.getView().getTopInventory().getLocation().getBlock();

        if (!openedLooms.containsKey(block)) {
            return;
        }

        ItemStack itemstack = event.getCurrentItem().clone();
        Location loc = block.getLocation();
        Player player = (Player) event.getWhoClicked();

        Inventory before = Bukkit.createInventory(null, 9);
        before.setItem(0, player.getOpenInventory().getItem(0).clone());
        before.setItem(1, player.getOpenInventory().getItem(1).clone());
        before.setItem(2, player.getOpenInventory().getItem(2).clone());

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {

            Inventory after = Bukkit.createInventory(null, 9);
            after.setItem(0, player.getOpenInventory().getItem(0).clone());
            after.setItem(1, player.getOpenInventory().getItem(1).clone());
            after.setItem(2, player.getOpenInventory().getItem(2).clone());

            if (InventoryUtils.compareContents(before, after)) {
                return;
            }

            Map<String, Object> map = openedLooms.get(block);
            if (!map.get("Player").equals(event.getWhoClicked())) {
                return;
            }

            Item item = new Item(block.getLocation().clone().add(0.5, 1.5, 0.5));
            item.setItemStack(itemstack);
            item.setLocked(true);
            item.setGravity(true);
            Vector lift = new Vector(0.0, 0.15, 0.0);
            Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
            item.setVelocity(pickup);
            item.setPickupDelay(32767);
            PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item);
            PacketManager.updateItem(item);

            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY));
                PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
            }, 8);
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseLoom(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
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
        if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
            return;
        }

        if (event.getRawSlot() >= 0 && event.getRawSlot() <= 3) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragLoom(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
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
        if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 3) {
                PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
                break;
            }
        }
    }

    @EventHandler
    public void onCloseLoom(InventoryCloseEvent event) {
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

        Block block = event.getView().getTopInventory().getLocation().getBlock();

        if (!openedLooms.containsKey(block)) {
            return;
        }

        Map<String, Object> map = openedLooms.get(block);
        if (!map.get("Player").equals(event.getPlayer())) {
            return;
        }

        if (event.getView().getItem(0) != null) {
            if (!event.getView().getItem(0).getType().equals(Material.AIR)) {
                Player player = (Player) event.getPlayer();
                Item item = new Item(block.getLocation().clone().add(0.5, 1.5, 0.5));
                item.setItemStack(event.getView().getItem(0));
                item.setLocked(true);
                item.setGravity(true);
                Vector lift = new Vector(0.0, 0.15, 0.0);
                Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(block.getLocation().clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
                item.setVelocity(pickup);
                item.setPickupDelay(32767);
                PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item);
                PacketManager.updateItem(item);
                new BukkitRunnable() {
                    public void run() {
                        PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                    }
                }.runTaskLater(InteractionVisualizer.plugin, 8);
            }
        }

        if (map.get("Banner") instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) map.get("Banner");
            InteractionVisualizer.lightManager.deleteLight(entity.getLocation());
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), entity);
        }
        openedLooms.remove(block);
    }

    public Map<String, ArmorStand> spawnArmorStands(Player player, Block block) {
        Map<String, ArmorStand> map = new HashMap<>();
        Location loc = block.getLocation().clone().add(0.5, 0.03, 0.5);
        Location temploc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()).setDirection(player.getLocation().getDirection().normalize().multiply(-1));
        float yaw = temploc.getYaw();
        ArmorStand banner = new ArmorStand(loc.clone());
        setStand(banner, yaw);

        map.put("Banner", banner);

        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), banner);

        return map;
    }

    public void setStand(ArmorStand stand, float yaw) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setSmall(true);
        stand.setCustomName("IV.Loom.Banner");
        stand.setRotation(yaw, stand.getLocation().getPitch());
        stand.setHeadPose(new EulerAngle(0.0, 0.0, 0.0));
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
