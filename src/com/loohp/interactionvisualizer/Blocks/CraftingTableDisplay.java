package com.loohp.interactionvisualizer.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.EntityCreator;
import com.loohp.interactionvisualizer.Utils.PacketSending;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class CraftingTableDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedBenches = new HashMap<Block, HashMap<String, Object>>();
	@SuppressWarnings("serial")
	public static List<Material> tools = new ArrayList<Material>(){
		{
			add(Material.WOODEN_AXE);
			add(Material.WOODEN_HOE);
			add(Material.WOODEN_PICKAXE);
			add(Material.WOODEN_SHOVEL);
			add(Material.WOODEN_SWORD);
			add(Material.STONE_AXE);
			add(Material.STONE_HOE);
			add(Material.STONE_PICKAXE);
			add(Material.STONE_SHOVEL);
			add(Material.STONE_SWORD);
			add(Material.IRON_AXE);
			add(Material.IRON_HOE);
			add(Material.IRON_PICKAXE);
			add(Material.IRON_SHOVEL);
			add(Material.IRON_SWORD);
			add(Material.GOLDEN_AXE);
			add(Material.GOLDEN_HOE);
			add(Material.GOLDEN_PICKAXE);
			add(Material.GOLDEN_SHOVEL);
			add(Material.GOLDEN_SWORD);
			add(Material.DIAMOND_AXE);
			add(Material.DIAMOND_HOE);
			add(Material.DIAMOND_PICKAXE);
			add(Material.DIAMOND_SHOVEL);
			add(Material.DIAMOND_SWORD);
			add(Material.BOW);
			add(Material.FISHING_ROD);
		}
	};
	

	@EventHandler
	public void onUseCraftingBench(InventoryClickEvent event) {
		if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.CRAFTING_TABLE)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 9) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler
	public void onDragCraftingBench(InventoryDragEvent event) {
		if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.CRAFTING_TABLE)) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 9) {
				PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseCraftingBench(InventoryCloseEvent event) {
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.CRAFTING_TABLE)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!openedBenches.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedBenches.get(block);
		if (!map.get("Player").equals((Player) event.getPlayer())) {
			return;
		}
		
		for (int i = 0; i <= 9; i++) {
			if (map.get(String.valueOf(i)) instanceof Entity) {
				Entity entity = (Entity) map.get(String.valueOf(i));
				if (i == 5) {
					LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
					for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
						LightAPI.updateChunk(info, LightType.BLOCK);
					}
				}
				if (entity instanceof Item) {
					PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), (Item) entity);
				} else if (entity instanceof ArmorStand) {
					PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
				}
				entity.remove();
			}
		}
		openedBenches.remove(block);
	}
	
	public static int run() {		
		return new BukkitRunnable() {
			public void run() {
				
				for (Player player : InteractionVisualizer.getOnlinePlayers()) {
					if (player.getGameMode().equals(GameMode.SPECTATOR)) {
						continue;
					}
					if (player.getOpenInventory() == null) {
						continue;
					}
					if (player.getOpenInventory().getTopInventory() == null) {
						continue;
					}
					if (player.getOpenInventory().getTopInventory().getLocation() == null) {
						continue;
					}
					if (player.getOpenInventory().getTopInventory().getLocation().getBlock() == null) {
						continue;
					}
					if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().equals(Material.CRAFTING_TABLE)) {
						continue;
					}
					
					InventoryView view = player.getOpenInventory();
					Block block = view.getTopInventory().getLocation().getBlock();
					Location loc = block.getLocation();
					
					if (!openedBenches.containsKey(block)) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("Player", player);
						map.put("0", "N/A");
						map.putAll(spawnArmorStands(player, block));
						openedBenches.put(block, map);
					}
					
					HashMap<String, Object> map = openedBenches.get(block);
					
					if (!map.get("Player").equals(player)) {
						continue;
					}
					ItemStack[] items = new ItemStack[]{view.getItem(1),view.getItem(2),view.getItem(3),view.getItem(4),view.getItem(5),view.getItem(6),view.getItem(7),view.getItem(8),view.getItem(9)};

					if (view.getItem(0) != null) {
						ItemStack itemstack = view.getItem(0);
						if (itemstack.getType().equals(Material.AIR)) {
							itemstack = null;
						}
						Item item = null;
						if (map.get("0") instanceof String) {
							if (itemstack != null) {
								item = (Item) EntityCreator.create(loc.clone().add(0.5, 1.2, 0.5), EntityType.DROPPED_ITEM);
								item.setItemStack(itemstack);
								item.setVelocity(new Vector(0, 0, 0));
								item.setPickupDelay(32767);
								item.setGravity(false);
								map.put("0", item);
								PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
								PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
							} else {
								map.put("0", "N/A");
							}
						} else {
							item = (Item) map.get("0");
							if (itemstack != null) {
								if (!item.getItemStack().equals(itemstack)) {
									item.setItemStack(itemstack);
									PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
								}
								item.setPickupDelay(32767);
								item.setGravity(false);
							} else {
								map.put("0", "N/A");
								PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
								item.remove();
							}
						}
					}
					for (int i = 0; i < 9; i++) {
						ArmorStand stand = (ArmorStand) map.get(String.valueOf(i + 1));
						ItemStack item = items[i];
						if (item.getType().equals(Material.AIR)) {
							item = null;
						}
						if (item != null) {
							if (item.getType().isBlock() && !standMode(stand).equals("Block")) {
								toggleStandMode(stand, "Block");
							} else if (tools.contains(item.getType()) && !standMode(stand).equals("Tool")) {
								toggleStandMode(stand, "Tool");
							} else if (!item.getType().isBlock() && !tools.contains(item.getType()) && !standMode(stand).equals("Item")) {
								toggleStandMode(stand, "Item");
							}
							stand.getEquipment().setItemInMainHand(item);
							PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
						} else {
							stand.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
							PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
						}
					}
					Location loc1 = ((ArmorStand) map.get("5")).getLocation();
					LightAPI.deleteLight(loc1, LightType.BLOCK, false);
					int light = loc1.getBlock().getRelative(BlockFace.UP).getLightLevel() - 1;
					if (light < 0) {
						light = 0;
					}
					LightAPI.createLight(loc1, LightType.BLOCK, light, false);
					for (ChunkInfo info : LightAPI.collectChunks(loc1, LightType.BLOCK, 15)) {
						LightAPI.updateChunk(info, LightType.BLOCK);
					}
				}
				
			}
		}.runTaskTimer(InteractionVisualizer.plugin, 0, 5).getTaskId();
	}
	
	public static String standMode(ArmorStand stand) {
		if (stand.getCustomName().startsWith("IV.CraftingTable.")) {
			return stand.getCustomName().substring(stand.getCustomName().lastIndexOf("."));
		}
		return null;
	}
	
	public static void toggleStandMode(ArmorStand stand, String mode) {
		if (!stand.getCustomName().equals("IV.CraftingTable.Item")) {
			if (stand.getCustomName().equals("IV.CraftingTable.Block")) {
				stand.setCustomName("IV.CraftingTable.Item");
				stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().multiply(-0.09), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().multiply(-0.12)));
			}
			if (stand.getCustomName().equals("IV.CraftingTable.Tool")) {
				stand.setCustomName("IV.CraftingTable.Item");
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().multiply(0.3), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().multiply(0.1)));
				stand.teleport(stand.getLocation().add(0, 0.26, 0));
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
			}
		}
		if (mode.equals("Block")) {
			stand.setCustomName("IV.CraftingTable.Block");
			stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().multiply(0.12)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().multiply(0.09), -90)));
			stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
			stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());		
		}
		if (mode.equals("Tool")) {
			stand.setCustomName("IV.CraftingTable.Tool");
			stand.setRightArmPose(new EulerAngle(357.99, 0.0, 300.0));
			stand.teleport(stand.getLocation().add(0, -0.26, 0));
			stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().multiply(-0.1)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().multiply(-0.3), -90)));
		}
	}
	
	public static HashMap<String, ArmorStand> spawnArmorStands(Player player, Block block) { //.add(0.68, 0.600781, 0.35)
		HashMap<String, ArmorStand> map = new HashMap<String, ArmorStand>();
		Location loc = block.getLocation().clone().add(0.5, 0.600781, 0.5);
		ArmorStand center = (ArmorStand) EntityCreator.create(loc, EntityType.ARMOR_STAND);
		float yaw = getCardinalDirection(player);
		center.setRotation(yaw, center.getLocation().getPitch());
		setStand(center);
		center.setCustomName("IV.CraftingTable.Center");
		Vector vector = rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.19), -100).add(center.getLocation().clone().getDirection().normalize().multiply(-0.11));
		ArmorStand slot5 = (ArmorStand) EntityCreator.create(loc.clone().add(vector), EntityType.ARMOR_STAND);
		setStand(slot5, yaw);
		ArmorStand slot2 = (ArmorStand) EntityCreator.create(slot5.getLocation().clone().add(center.getLocation().clone().getDirection().normalize().multiply(0.2)), EntityType.ARMOR_STAND);
		setStand(slot2, yaw);
		ArmorStand slot1 = (ArmorStand) EntityCreator.create(slot2.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)), EntityType.ARMOR_STAND);
		setStand(slot1, yaw);
		ArmorStand slot3 = (ArmorStand) EntityCreator.create(slot2.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)), EntityType.ARMOR_STAND);
		setStand(slot3, yaw);
		ArmorStand slot4 = (ArmorStand) EntityCreator.create(slot5.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)), EntityType.ARMOR_STAND);
		setStand(slot4, yaw);
		ArmorStand slot6 = (ArmorStand) EntityCreator.create(slot5.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)), EntityType.ARMOR_STAND);
		setStand(slot6, yaw);
		ArmorStand slot8 = (ArmorStand) EntityCreator.create(slot5.getLocation().clone().add(center.getLocation().getDirection().clone().normalize().multiply(-0.2)), EntityType.ARMOR_STAND);
		setStand(slot8, yaw);
		ArmorStand slot7 = (ArmorStand) EntityCreator.create(slot8.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), -90)), EntityType.ARMOR_STAND);
		setStand(slot7, yaw);
		ArmorStand slot9 = (ArmorStand) EntityCreator.create(slot8.getLocation().clone().add(rotateVectorAroundY(center.getLocation().clone().getDirection().normalize().multiply(0.2), 90)), EntityType.ARMOR_STAND);
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
		center.remove();
		
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot1);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot2);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot3);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot4);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot5);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot6);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot7);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot8);
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, slot9);
		
		return map;
	}
	
	public static void setStand(ArmorStand stand, float yaw) {
		stand.setArms(true);
		stand.setBasePlate(false);
		stand.setMarker(true);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setInvulnerable(true);
		stand.setVisible(false);
		stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
		stand.setCustomName("IV.CraftingTable.Item");
		stand.setRotation(yaw, stand.getLocation().getPitch());
	}
	
	public static void setStand(ArmorStand stand) {
		stand.setArms(true);
		stand.setBasePlate(false);
		stand.setMarker(true);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setInvulnerable(true);
		stand.setVisible(false);
	}
	
	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
	    double rad = Math.toRadians(degrees);
	   
	    double currentX = vector.getX();
	    double currentZ = vector.getZ();
	   
	    double cosine = Math.cos(rad);
	    double sine = Math.sin(rad);
	   
	    return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
	}
	
	public static float getCardinalDirection(Entity e) {

		double rotation = (e.getLocation().getYaw() - 90.0F) % 360.0F;

		if (rotation < 0.0D) {
			rotation += 360.0D;
		}
		if ((0.0D <= rotation) && (rotation < 45.0D))
			return 90.0F;
		if ((45.0D <= rotation) && (rotation < 135.0D))
			return 180.0F;
		if ((135.0D <= rotation) && (rotation < 225.0D))
			return -90.0F;
		if ((225.0D <= rotation) && (rotation < 315.0D))
			return 0.0F;
		if ((315.0D <= rotation) && (rotation < 360.0D)) {
			return 90.0F;
		}
		return 0.0F;
	}

}
