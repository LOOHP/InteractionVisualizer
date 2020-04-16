package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Iterator;

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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolder.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolder.Item;
import com.loohp.interactionvisualizer.Manager.LightManager;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.MaterialUtils;
import com.loohp.interactionvisualizer.Utils.PacketSending;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

public class CraftingTableDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedBenches = new HashMap<Block, HashMap<String, Object>>();
	public static HashMap<Player, Block> playermap = new HashMap<Player, Block>();
	
	@EventHandler(priority=EventPriority.MONITOR)
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
		
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
			return;
		}
		
		Block block = playermap.get((Player) event.getWhoClicked());
		
		if (!openedBenches.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedBenches.get(block);
		if (!map.get("Player").equals((Player) event.getWhoClicked())) {
			return;
		}
		
		ItemStack itemstack = event.getCurrentItem();
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
		
		openedBenches.remove(block);
		
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
		
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot1);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot2);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot3);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot4);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot5);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot6);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot7);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot8);
		PacketSending.updateArmorStand(InteractionVisualizer.itemStand, slot9);
		
		new BukkitRunnable() {
			public void run() {
				for (Player each : InteractionVisualizer.itemDrop) {
					each.spawnParticle(Particle.CLOUD, loc.clone().add(0.5, 1.1, 0.5), 10, 0.05, 0.05, 0.05, 0.05);
				}
			}
		}.runTaskLater(InteractionVisualizer.plugin, 6);
		
		new BukkitRunnable() {
			public void run() {
				Vector lift = new Vector(0.0, 0.15, 0.0);
				Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
				item.setItemStack(itemstack);
				item.setVelocity(pickup);
				item.setGravity(true);
				item.setPickupDelay(32767);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
				
				new BukkitRunnable() {
					public void run() {
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot1);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot2);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot3);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot4);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot5);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot6);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot7);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot8);
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), slot9);
						PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
					}
				}.runTaskLater(InteractionVisualizer.plugin, 8);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 10);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseCraftingBench(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 9) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragCraftingBench(InventoryDragEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
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
		if (!playermap.containsKey((Player) event.getPlayer())) {
			return;
		}
		
		Block block = playermap.get((Player) event.getPlayer());
		
		if (!openedBenches.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedBenches.get(block);
		if (!map.get("Player").equals((Player) event.getPlayer())) {
			return;
		}
		
		for (int i = 0; i <= 9; i++) {
			if (!(map.get(String.valueOf(i)) instanceof String)) {
				Object entity = map.get(String.valueOf(i));
				if (entity instanceof Item) {
					PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), (Item) entity);
				} else if (entity instanceof ArmorStand) {
					if (((ArmorStand) entity).isLocked() == false) {
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
					}
					int finalI = i;
					new BukkitRunnable() {
						public void run() {
							if (finalI == 5) {
								LightManager.deleteLight(((ArmorStand) entity).getLocation());
							}
						}
					}.runTaskLater(InteractionVisualizer.plugin, 20);
				}
			}
		}
		openedBenches.remove(block);
		playermap.remove((Player) event.getPlayer());
	}
	
	public static int run() {		
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
							HashMap<String, Object> map = openedBenches.get(block);

							Player player = (Player) map.get("Player");
							if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
								if (player.getOpenInventory() != null) {
									if (player.getOpenInventory().getTopInventory() != null) {
										if (!InteractionVisualizer.version.contains("legacy")) {
											if (!InteractionVisualizer.version.equals("1.13") && !InteractionVisualizer.version.equals("1.13.1")) {
												if (player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("CRAFTING_TABLE")) {
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
										LightManager.deleteLight(((ArmorStand) entity).getLocation());
									}
									if (entity instanceof Item) {
										PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), (Item) entity);
									} else if (entity instanceof ArmorStand) {
										PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
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
	
	public static void process(Player player) {
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
			if (!InteractionVisualizer.version.contains("legacy")) {
				if (!InteractionVisualizer.version.equals("1.13") && !InteractionVisualizer.version.equals("1.13.1")) {
					if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("CRAFTING_TABLE")) {
						return;
					}
				} else {
					if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory)) {
						return;
					}
					if (((CraftingInventory) player.getOpenInventory().getTopInventory()).getMatrix().length != 9) {
						return;
					}
					if (!player.getTargetBlock(MaterialUtils.getNonSolidSet(), 7).getType().toString().toUpperCase().equals("CRAFTING_TABLE")) {
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
				if (!player.getTargetBlock(MaterialUtils.getNonSolidSet(), 7).getType().toString().toUpperCase().equals("WORKBENCH")) {
					return;
				}
			}
			Block block = null;
			InventoryView view = player.getOpenInventory();
			if (!InteractionVisualizer.version.contains("legacy") && !InteractionVisualizer.version.equals("1.13") && !InteractionVisualizer.version.equals("1.13.1")) {
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
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("Player", player);
			map.put("0", "N/A");
			map.putAll(spawnArmorStands(player, block));
			openedBenches.put(block, map);
		}
		
		HashMap<String, Object> map = openedBenches.get(block);
		
		if (!map.get("Player").equals(player)) {
			return;
		}
		ItemStack[] items = new ItemStack[]{
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
			if (itemstack.getType().equals(Material.AIR)) {
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
				} else if (MaterialUtils.isTool(item.getType()) && !standMode(stand).equals("Tool")) {
					toggleStandMode(stand, "Tool");
				} else if (!item.getType().isBlock() && !MaterialUtils.isTool(item.getType()) && !standMode(stand).equals("Item")) {
					toggleStandMode(stand, "Item");
				}
				stand.setItemInMainHand(item);
				PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
			} else {
				stand.setItemInMainHand(new ItemStack(Material.AIR));
				PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
			}
		}
		Location loc1 = ((ArmorStand) map.get("5")).getLocation();
		LightManager.deleteLight(loc1);
		int light = loc1.getBlock().getRelative(BlockFace.UP).getLightLevel() - 1;
		if (light < 0) {
			light = 0;
		}
		LightManager.createLight(loc1, light);
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
		stand.setSilent(true);
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
		stand.setSilent(true);
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
