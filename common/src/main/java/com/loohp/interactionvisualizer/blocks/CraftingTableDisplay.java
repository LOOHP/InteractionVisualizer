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
import com.loohp.interactionvisualizer.utils.MCVersion;
import com.loohp.interactionvisualizer.utils.MaterialUtils;
import com.loohp.interactionvisualizer.utils.MaterialUtils.MaterialMode;
import com.loohp.interactionvisualizer.utils.VanishUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CraftingTableDisplay extends VisualizerInteractDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("crafting_table");

    public Map<Block, Map<String, Object>> openedBenches = new HashMap<>();
    public Map<Player, Block> playermap = new HashMap<Player, Block>();

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int run() {
        return new BukkitRunnable() {
            public void run() {

                Iterator<Block> itr = openedBenches.keySet().iterator();
                int count = 0;
                int maxper = (int) Math.ceil((double) openedBenches.size() / (double) 5);
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
                            if (!openedBenches.containsKey(block)) {
                                return;
                            }
                            Map<String, Object> map = openedBenches.get(block);

                            Player player = (Player) map.get("Player");
                            if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
                                if (player.getOpenInventory() != null) {
                                    if (player.getOpenInventory().getTopInventory() != null) {
                                        if (!InteractionVisualizer.version.isLegacy()) {
                                            if (!InteractionVisualizer.version.equals(MCVersion.V1_13) && !InteractionVisualizer.version.equals(MCVersion.V1_13_1)) {
                                                if (player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().equalsIgnoreCase("CRAFTING_TABLE")) {
                                                    return;
                                                }
                                            } else {
                                                if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                                                    if (((CraftingInventory) player.getOpenInventory().getTopInventory()).getMatrix().length == 9) {
                                                        return;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                                                if (((CraftingInventory) player.getOpenInventory().getTopInventory()).getMatrix().length == 9) {
                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            for (int i = 0; i <= 9; i++) {
                                if (!(map.get(String.valueOf(i)) instanceof String)) {
                                    Object entity = map.get(String.valueOf(i));
                                    if (i == 5) {
                                        InteractionVisualizer.lightManager.deleteLight(((ArmorStand) entity).getLocation());
                                    }
                                    if (entity instanceof Item) {
                                        PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), (Item) entity);
                                    } else if (entity instanceof ArmorStand) {
                                        PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), (ArmorStand) entity);
                                    }
                                }
                            }
                            openedBenches.remove(block);
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
                if (!InteractionVisualizer.version.equals(MCVersion.V1_13) && !InteractionVisualizer.version.equals(MCVersion.V1_13_1)) {
                    if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().equalsIgnoreCase("CRAFTING_TABLE")) {
                        return;
                    }
                } else {
                    if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory)) {
                        return;
                    }
                    if (((CraftingInventory) player.getOpenInventory().getTopInventory()).getMatrix().length != 9) {
                        return;
                    }
                    if (!player.getTargetBlock(MaterialUtils.getNonSolidSet(), 7).getType().toString().equalsIgnoreCase("CRAFTING_TABLE")) {
                        return;
                    }
                }
            } else {
                if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory)) {
                    return;
                }
                if (((CraftingInventory) player.getOpenInventory().getTopInventory()).getMatrix().length != 9) {
                    return;
                }
                if (!player.getTargetBlock(MaterialUtils.getNonSolidSet(), 7).getType().toString().equalsIgnoreCase("WORKBENCH")) {
                    return;
                }
            }
            Block block = null;
            InventoryView view = player.getOpenInventory();
            if (!InteractionVisualizer.version.isLegacy() && !InteractionVisualizer.version.equals(MCVersion.V1_13) && !InteractionVisualizer.version.equals(MCVersion.V1_13_1)) {
                block = view.getTopInventory().getLocation().getBlock();
            } else {
                block = player.getTargetBlock(MaterialUtils.getNonSolidSet(), 7);
            }
            playermap.put(player, block);
        }

        InventoryView view = player.getOpenInventory();
        Block block = playermap.get(player);
        Location loc = block.getLocation();

        if (!openedBenches.containsKey(block)) {
            Map<String, Object> map = new HashMap<>();
            map.put("Player", player);
            map.put("0", "N/A");
            map.putAll(spawnArmorStands(player, block));
            openedBenches.put(block, map);
        }

        Map<String, Object> map = openedBenches.get(block);

        if (!map.get("Player").equals(player)) {
            return;
        }
        ItemStack[] items = new ItemStack[] {
                view.getItem(1),
                view.getItem(2),
                view.getItem(3),
                view.getItem(4),
                view.getItem(5),
                view.getItem(6),
                view.getItem(7),
                view.getItem(8),
                view.getItem(9)
        };

        if (view.getItem(0) != null) {
            ItemStack itemstack = view.getItem(0);
            if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
                itemstack = null;
            }
            Item item = null;
            if (map.get("0") instanceof String) {
                if (itemstack != null) {
                    item = new Item(loc.clone().add(0.5, 1.2, 0.5));
                    item.setItemStack(itemstack);
                    item.setVelocity(new Vector(0, 0, 0));
                    item.setPickupDelay(32767);
                    item.setGravity(false);
                    map.put("0", item);
                    PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item);
                    PacketManager.updateItem(item);
                } else {
                    map.put("0", "N/A");
                }
            } else {
                item = (Item) map.get("0");
                if (itemstack != null) {
                    if (!item.getItemStack().equals(itemstack)) {
                        item.setItemStack(itemstack);
                        PacketManager.updateItem(item);
                    }
                } else {
                    map.put("0", "N/A");
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            ArmorStand stand = (ArmorStand) map.get(String.valueOf(i + 1));
            ItemStack item = items[i];
            if (item == null || item.getType().equals(Material.AIR)) {
                item = null;
            }
            if (item != null) {
                boolean changed = true;
                if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialMode.BLOCK) && !standMode(stand).equals(MaterialMode.BLOCK)) {
                    toggleStandMode(stand, "Block");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialMode.TOOL) && !standMode(stand).equals(MaterialMode.TOOL)) {
                    toggleStandMode(stand, "Tool");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialMode.ITEM) && !standMode(stand).equals(MaterialMode.ITEM)) {
                    toggleStandMode(stand, "Item");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialMode.STANDING) && !standMode(stand).equals(MaterialMode.STANDING)) {
                    toggleStandMode(stand, "Standing");
                } else if (MaterialUtils.getMaterialType(item.getType()).equals(MaterialMode.LOWBLOCK) && !standMode(stand).equals(MaterialMode.LOWBLOCK)) {
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
        Location loc1 = ((ArmorStand) map.get("5")).getLocation();
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
    public void onCraft(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
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
            if (!event.getCursor().getType().equals(Material.AIR)) {
                if (event.getCursor().getAmount() >= event.getCursor().getType().getMaxStackSize()) {
                    return;
                }
            }
        }
        if (event.isShiftClick()) {
            if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(0).getType())) {
                return;
            }
        }
        if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
            int hotbarSlot = event.getHotbarButton();
            if (hotbarSlot < 0 || (event.getWhoClicked().getInventory().getItem(hotbarSlot) != null && !event.getWhoClicked().getInventory().getItem(hotbarSlot).getType().equals(Material.AIR))) {
                return;
            }
        }

        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }

        Block block = playermap.get((Player) event.getWhoClicked());

        if (!openedBenches.containsKey(block)) {
            return;
        }

        Map<String, Object> map = openedBenches.get(block);
        if (!map.get("Player").equals(event.getWhoClicked())) {
            return;
        }

        ItemStack itemstack = event.getCurrentItem().clone();
        Location loc = block.getLocation();
        Player player = (Player) event.getWhoClicked();

        if (map.get("0") instanceof String) {
            map.put("0", new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
        }
        Item item = (Item) map.get("0");
        ArmorStand slot1 = (ArmorStand) map.get("1");
        ArmorStand slot2 = (ArmorStand) map.get("2");
        ArmorStand slot3 = (ArmorStand) map.get("3");
        ArmorStand slot4 = (ArmorStand) map.get("4");
        ArmorStand slot5 = (ArmorStand) map.get("5");
        ArmorStand slot6 = (ArmorStand) map.get("6");
        ArmorStand slot7 = (ArmorStand) map.get("7");
        ArmorStand slot8 = (ArmorStand) map.get("8");
        ArmorStand slot9 = (ArmorStand) map.get("9");

        Inventory before = Bukkit.createInventory(null, 9);
        for (int i = 1; i < 10; i++) {
            before.setItem(i - 1, player.getOpenInventory().getItem(i).clone());
        }

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {

            Inventory after = Bukkit.createInventory(null, 9);
            for (int i = 1; i < 10; i++) {
                after.setItem(i - 1, player.getOpenInventory().getItem(i).clone());
            }

            if (InventoryUtils.compareContents(before, after)) {
                return;
            }

            item.setLocked(true);
            slot1.setLocked(true);
            slot2.setLocked(true);
            slot3.setLocked(true);
            slot4.setLocked(true);
            slot5.setLocked(true);
            slot6.setLocked(true);
            slot7.setLocked(true);
            slot8.setLocked(true);
            slot9.setLocked(true);

            openedBenches.remove(block);

            float yaw = getCardinalDirection(player);
            Vector vector = new Location(slot8.getWorld(), slot8.getLocation().getX(), slot8.getLocation().getY(), slot8.getLocation().getZ(), yaw, 0).getDirection().normalize();
            slot1.teleport(slot1.getLocation().add(rotateVectorAroundY(vector.clone(), 135).multiply(0.2828)));
            slot2.teleport(slot2.getLocation().add(rotateVectorAroundY(vector.clone(), 180).multiply(0.2)));
            slot3.teleport(slot3.getLocation().add(rotateVectorAroundY(vector.clone(), 225).multiply(0.2828)));
            slot4.teleport(slot4.getLocation().add(rotateVectorAroundY(vector.clone(), 90).multiply(0.2)));

            slot6.teleport(slot6.getLocation().add(rotateVectorAroundY(vector.clone(), -90).multiply(0.2)));
            slot7.teleport(slot7.getLocation().add(rotateVectorAroundY(vector.clone(), 45).multiply(0.2828)));
            slot8.teleport(slot8.getLocation().add(vector.clone().multiply(0.2)));
            slot9.teleport(slot9.getLocation().add(rotateVectorAroundY(vector.clone(), -45).multiply(0.2828)));

            PacketManager.updateArmorStand(slot1);
            PacketManager.updateArmorStand(slot2);
            PacketManager.updateArmorStand(slot3);
            PacketManager.updateArmorStand(slot4);
            PacketManager.updateArmorStand(slot5);
            PacketManager.updateArmorStand(slot6);
            PacketManager.updateArmorStand(slot7);
            PacketManager.updateArmorStand(slot8);
            PacketManager.updateArmorStand(slot9);

            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                for (Player each : InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY)) {
                    each.spawnParticle(Particle.CLOUD, loc.clone().add(0.5, 1.1, 0.5), 10, 0.05, 0.05, 0.05, 0.05);
                }
            }, 6);

            Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                Vector lift = new Vector(0.0, 0.15, 0.0);
                Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
                item.setItemStack(itemstack);
                item.setVelocity(pickup);
                item.setGravity(true);
                item.setPickupDelay(32767);
                PacketManager.updateItem(item);

                Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
                    SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY));
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot1);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot2);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot3);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot4);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot5);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot6);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot7);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot8);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot9);
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                }, 8);
            }, 10);
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseCraftingBench(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }

        if (event.getRawSlot() >= 0 && event.getRawSlot() <= 9) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragCraftingBench(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= 9) {
                PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
                break;
            }
        }
    }

    @EventHandler
    public void onCloseCraftingBench(InventoryCloseEvent event) {
        if (!playermap.containsKey((Player) event.getPlayer())) {
            return;
        }

        Block block = playermap.get((Player) event.getPlayer());

        if (!openedBenches.containsKey(block)) {
            return;
        }

        Map<String, Object> map = openedBenches.get(block);
        if (!map.get("Player").equals(event.getPlayer())) {
            return;
        }

        for (int i = 0; i <= 9; i++) {
            if (!(map.get(String.valueOf(i)) instanceof String)) {
                Object entity = map.get(String.valueOf(i));
                if (entity instanceof Item) {
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), (Item) entity);
                } else if (entity instanceof ArmorStand) {
                    if (!((ArmorStand) entity).isLocked()) {
                        PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), (ArmorStand) entity);
                    }
                    int finalI = i;
                    new BukkitRunnable() {
                        public void run() {
                            if (finalI == 5) {
                                InteractionVisualizer.lightManager.deleteLight(((ArmorStand) entity).getLocation());
                            }
                        }
                    }.runTaskLater(InteractionVisualizer.plugin, 20);
                }
            }
        }
        openedBenches.remove(block);
        playermap.remove((Player) event.getPlayer());
    }

    public MaterialMode standMode(ArmorStand stand) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (plain.startsWith("IV.CraftingTable.")) {
            return MaterialMode.getModeFromName(plain.substring(plain.lastIndexOf(".") + 1));
        }
        return null;
    }

    public void toggleStandMode(ArmorStand stand, String mode) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (!plain.equals("IV.CraftingTable.Item")) {
            if (plain.equals("IV.CraftingTable.Block")) {
                stand.setCustomName("IV.CraftingTable.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.084, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.102), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.14)));

            }
            if (plain.equals("IV.CraftingTable.LowBlock")) {
                stand.setCustomName("IV.CraftingTable.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.02, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.09), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.15)));

            }
            if (plain.equals("IV.CraftingTable.Tool")) {
                stand.setCustomName("IV.CraftingTable.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.1)));
                stand.teleport(stand.getLocation().add(0, 0.26, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
            if (plain.equals("IV.CraftingTable.Standing")) {
                stand.setCustomName("IV.CraftingTable.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(0.323), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(-0.115)));
                stand.teleport(stand.getLocation().add(0, 0.32, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
        }
        if (mode.equals("Block")) {
            stand.setCustomName("IV.CraftingTable.Block");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.14)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.102), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.084, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("LowBlock")) {
            stand.setCustomName("IV.CraftingTable.LowBlock");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.15)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.09), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.02, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("Tool")) {
            stand.setCustomName("IV.CraftingTable.Tool");
            stand.setRightArmPose(new EulerAngle(357.99, 0.0, 300.0));
            stand.teleport(stand.getLocation().add(0, -0.26, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.1)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.3), -90)));
        }
        if (mode.equals("Standing")) {
            stand.setCustomName("IV.CraftingTable.Standing");
            stand.setRightArmPose(new EulerAngle(0.0, 4.7, 4.7));
            stand.teleport(stand.getLocation().add(0, -0.32, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(0.115)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(-0.323), -90)));
        }
    }

    public Map<String, ArmorStand> spawnArmorStands(Player player, Block block) { //.add(0.68, 0.600781, 0.35)
        Map<String, ArmorStand> map = new HashMap<>();
        Location loc = block.getLocation().clone().add(0.5, 0.600781, 0.5);
        ArmorStand center = new ArmorStand(loc);
        float yaw = getCardinalDirection(player);
        center.setRotation(yaw, center.getLocation().getPitch());
        setStand(center);
        center.setCustomName("IV.CraftingTable.Center");
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
        stand.setCustomName("IV.CraftingTable.Item");
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

    public float getCardinalDirection(Entity e) {

        double rotation = (e.getLocation().getYaw() - 90.0F) % 360.0F;

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
