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
import com.loohp.interactionvisualizer.utils.MaterialUtils;
import com.loohp.interactionvisualizer.utils.MaterialUtils.MaterialMode;
import com.loohp.interactionvisualizer.utils.VanishUtils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class SmithingTableDisplay extends VisualizerInteractDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("smithing_table");

    public Map<Block, Map<String, Object>> openedSTables = new HashMap<>();
    public Map<Player, Block> playermap = new HashMap<>();

    @Override
    public EntryKey key() {
        return KEY;
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
            if (!(player.getOpenInventory().getTopInventory() instanceof SmithingInventory)) {
                return;
            }

            Block block = player.getTargetBlockExact(7, FluidCollisionMode.NEVER);
            if (block == null || !block.getType().equals(Material.SMITHING_TABLE)) {
                return;
            }

            playermap.put(player, block);
        }

        InventoryView view = player.getOpenInventory();
        Block block = playermap.get(player);
        Location loc = block.getLocation();

        int maxSlot = view.getTopInventory().getSize() - 1;
        String maxSlotStr = String.valueOf(maxSlot);

        if (!openedSTables.containsKey(block)) {
            Map<String, Object> map = new HashMap<>();
            map.put("Player", player);
            map.put(maxSlotStr, "N/A");
            map.putAll(spawnArmorStands(player, block, maxSlot));
            openedSTables.put(block, map);
        }

        Map<String, Object> map = openedSTables.get(block);

        if (!map.get("Player").equals(player)) {
            return;
        }
        ItemStack[] items = new ItemStack[maxSlot + 1];
        for (int i = 0; i <= maxSlot; i++) {
            items[i] = view.getItem(i);
        }

        if (view.getItem(maxSlot) != null) {
            ItemStack itemstack = view.getItem(maxSlot);
            if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
                itemstack = null;
            }
            Item item = null;
            if (map.get(maxSlotStr) instanceof String) {
                if (itemstack != null) {
                    item = new Item(loc.clone().add(0.5, 1.2, 0.5));
                    item.setItemStack(itemstack);
                    item.setVelocity(new Vector(0, 0, 0));
                    item.setPickupDelay(32767);
                    item.setGravity(false);
                    map.put(maxSlotStr, item);
                    PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item);
                    PacketManager.updateItem(item);
                } else {
                    map.put(maxSlotStr, "N/A");
                }
            } else {
                item = (Item) map.get(maxSlotStr);
                if (itemstack != null) {
                    if (!item.getItemStack().equals(itemstack)) {
                        item.setItemStack(itemstack);
                        PacketManager.updateItem(item);
                    }
                } else {
                    map.put(maxSlotStr, "N/A");
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                }
            }
        }
        for (int i = 0; i < maxSlot; i++) {
            ArmorStand stand = (ArmorStand) map.get(String.valueOf(i));
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

        Location loc1 = ((ArmorStand) map.get("0")).getLocation();
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
    public void onSmithingTable(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        int maxSlot = event.getView().getTopInventory().getSize() - 1;
        String maxSlotStr = String.valueOf(maxSlot);
        if (event.getRawSlot() != maxSlot) {
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
            if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(maxSlot).getType())) {
                return;
            }
        }
        int hotbarSlot = event.getHotbarButton();
        if (hotbarSlot >= 0 && event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
            if (event.getWhoClicked().getInventory().getItem(hotbarSlot) != null && !event.getWhoClicked().getInventory().getItem(hotbarSlot).getType().equals(Material.AIR)) {
                return;
            }
        }

        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }

        Block block = event.getWhoClicked().getTargetBlockExact(7, FluidCollisionMode.NEVER);

        if (!openedSTables.containsKey(block)) {
            return;
        }

        Map<String, Object> map = openedSTables.get(block);
        if (!map.get("Player").equals(event.getWhoClicked())) {
            return;
        }

        ItemStack itemstack = event.getCurrentItem();
        Location loc = block.getLocation();
        Player player = (Player) event.getWhoClicked();

        ArmorStand slot0;
        ArmorStand slot1;
        ArmorStand slot2;
        if (maxSlot == 2) {
            slot0 = null;
            slot1 = (ArmorStand) map.get("0");
            slot2 = (ArmorStand) map.get("1");
        } else {
            slot0 = (ArmorStand) map.get("0");
            slot1 = (ArmorStand) map.get("1");
            slot2 =(ArmorStand) map.get("2");
        }

        if (map.get(maxSlotStr) instanceof String) {
            map.put(maxSlotStr, new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
        }
        Item item = (Item) map.get(maxSlotStr);

        Inventory before = Bukkit.createInventory(null, 9);
        for (int i = 0; i <= maxSlot; i++) {
            before.setItem(i, player.getOpenInventory().getItem(i).clone());
        }

        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {

            Inventory after = Bukkit.createInventory(null, 9);
            for (int i = 0; i <= maxSlot; i++) {
                after.setItem(i, player.getOpenInventory().getItem(i).clone());
            }

            if (InventoryUtils.compareContents(before, after)) {
                return;
            }

            if (slot0 != null) {
                slot0.setLocked(true);
            }
            slot1.setLocked(true);
            slot2.setLocked(true);
            item.setLocked(true);

            openedSTables.remove(block);

            float yaw = getCardinalDirection(player);
            Vector vector = new Location(slot1.getWorld(), slot1.getLocation().getX(), slot1.getLocation().getY(), slot1.getLocation().getZ(), yaw, 0).getDirection().normalize();
            slot1.teleport(slot1.getLocation().add(rotateVectorAroundY(vector.clone(), 90).multiply(0.1)));
            slot2.teleport(slot2.getLocation().add(rotateVectorAroundY(vector.clone(), -90).multiply(0.1)));

            if (slot0 != null) {
                PacketManager.updateArmorStand(slot0);
            }
            PacketManager.updateArmorStand(slot1);
            PacketManager.updateArmorStand(slot2);

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
                    if (slot0 != null) {
                        PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot0);
                    }
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot1);
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), slot2);
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
                }, 8);
            }, 10);
        }, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseSmithingTable(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }
        int maxSlot = event.getView().getTopInventory().getSize() - 1;
        if (event.getRawSlot() >= 0 && event.getRawSlot() <= maxSlot) {
            PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
        }
    }

    @EventHandler
    public void onDragSmithingTable(InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }
        if (event.getView().getTopInventory() == null) {
            return;
        }
        if (!playermap.containsKey((Player) event.getWhoClicked())) {
            return;
        }
        int maxSlot = event.getView().getTopInventory().getSize() - 1;
        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot <= maxSlot) {
                PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
                break;
            }
        }
    }

    @EventHandler
    public void onCloseSmithingTable(InventoryCloseEvent event) {
        if (!playermap.containsKey((Player) event.getPlayer())) {
            return;
        }

        Block block = playermap.get((Player) event.getPlayer());

        if (!openedSTables.containsKey(block)) {
            return;
        }

        Map<String, Object> map = openedSTables.get(block);
        if (!map.get("Player").equals(event.getPlayer())) {
            return;
        }

        int maxSlot = event.getView().getTopInventory().getSize() - 1;

        for (int i = 0; i <= maxSlot; i++) {
            if (!(map.get(String.valueOf(i)) instanceof String)) {
                Object entity = map.get(String.valueOf(i));
                if (entity instanceof Item) {
                    PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), (Item) entity);
                } else if (entity instanceof ArmorStand) {
                    PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), (ArmorStand) entity);
                }
            }
        }

        if (map.get("0") instanceof ArmorStand) {
            ArmorStand entity = (ArmorStand) map.get("0");
            InteractionVisualizer.lightManager.deleteLight(entity.getLocation());
            PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), entity);
        }
        openedSTables.remove(block);
        playermap.remove((Player) event.getPlayer());
    }

    public MaterialMode standMode(ArmorStand stand) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (plain.startsWith("IV.SmithingTable.")) {
            return MaterialMode.getModeFromName(plain.substring(plain.lastIndexOf(".") + 1));
        }
        return null;
    }

    public void toggleStandMode(ArmorStand stand, String mode) {
        String plain = PlainTextComponentSerializer.plainText().serialize(stand.getCustomName());
        if (!plain.equals("IV.SmithingTable.Item")) {
            if (plain.equals("IV.SmithingTable.Block")) {
                stand.setCustomName("IV.SmithingTable.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.084, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.102), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.14)));

            }
            if (plain.equals("IV.SmithingTable.LowBlock")) {
                stand.setCustomName("IV.SmithingTable.Item");
                stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
                stand.setRightArmPose(EulerAngle.ZERO);
                stand.teleport(stand.getLocation().add(0.0, -0.02, 0.0));
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.09), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.15)));

            }
            if (plain.equals("IV.SmithingTable.Tool")) {
                stand.setCustomName("IV.SmithingTable.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.1)));
                stand.teleport(stand.getLocation().add(0, 0.26, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
            if (plain.equals("IV.SmithingTable.Standing")) {
                stand.setCustomName("IV.SmithingTable.Item");
                stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(0.323), -90)));
                stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(-0.115)));
                stand.teleport(stand.getLocation().add(0, 0.32, 0));
                stand.setRightArmPose(EulerAngle.ZERO);
            }
        }
        if (mode.equals("Block")) {
            stand.setCustomName("IV.SmithingTable.Block");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.14)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.102), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.084, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("LowBlock")) {
            stand.setCustomName("IV.SmithingTable.LowBlock");
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.15)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.09), -90)));
            stand.teleport(stand.getLocation().add(0.0, 0.02, 0.0));
            stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
            stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
        }
        if (mode.equals("Tool")) {
            stand.setCustomName("IV.SmithingTable.Tool");
            stand.setRightArmPose(new EulerAngle(357.99, 0.0, 300.0));
            stand.teleport(stand.getLocation().add(0, -0.26, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.1)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.3), -90)));
        }
        if (mode.equals("Standing")) {
            stand.setCustomName("IV.SmithingTable.Standing");
            stand.setRightArmPose(new EulerAngle(0.0, 4.7, 4.7));
            stand.teleport(stand.getLocation().add(0, -0.32, 0));
            stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(0.115)));
            stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(-0.323), -90)));
        }
    }

    public Map<String, ArmorStand> spawnArmorStands(Player player, Block block, int maxSlot) { //.add(0.68, 0.600781, 0.35)
        Map<String, ArmorStand> map = new HashMap<>();
        Location loc = block.getLocation().clone().add(0.5, 0.600781, 0.5);
        ArmorStand center = new ArmorStand(loc);
        float yaw = getCardinalDirection(player);
        center.setRotation(yaw, center.getLocation().getPitch());
        setStand(center);
        center.setCustomName("IV.SmithingTable.Center");
        Vector vector = rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.19), -100).add(center.getLocation().clone().getDirection().normalize().multiply(-0.11));
        ArmorStand middle = new ArmorStand(loc.clone().add(vector));
        setStand(middle, yaw);

        ArmorStand slot0 = new ArmorStand(middle.getLocation());
        setStand(slot0, yaw);
        ArmorStand slot1 = new ArmorStand(middle.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
        setStand(slot1, yaw + 20);
        ArmorStand slot2 = new ArmorStand(middle.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.3), 90)));
        setStand(slot2, yaw - 20);

        if (maxSlot == 2) {
            map.put("0", slot1);
            map.put("1", slot2);
        } else {
            map.put("0", slot0);
            map.put("1", slot1);
            map.put("2", slot2);

            PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot0);
        }
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot1);
        PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, KEY), slot2);

        return map;
    }

    public void setStand(ArmorStand stand, float yaw) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setSilent(true);
        stand.setSmall(true);
        stand.setRightArmPose(EulerAngle.ZERO);
        stand.setCustomName("IV.SmithingTable.Item");
        stand.setRotation(yaw, stand.getLocation().getPitch());
    }

    public void setStand(ArmorStand stand) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setGravity(false);
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
