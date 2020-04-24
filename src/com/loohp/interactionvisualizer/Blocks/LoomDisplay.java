package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.Managers.LightManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.SoundManager;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

import ru.beykerykt.lightapi.LightType;

public class LoomDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedLooms = new HashMap<Block, HashMap<String, Object>>();
	
	@EventHandler(priority=EventPriority.MONITOR)
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
		
		if (event.getView().getTopInventory() == null) {
			return;
		}
		try {
			if (event.getView().getTopInventory().getLocation() == null) {
				return;
			}
		} catch (Exception e) {
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
		
		ItemStack itemstack = event.getCurrentItem();
		Location loc = block.getLocation();
		
		HashMap<String, Object> map = openedLooms.get(block);
		if (!map.get("Player").equals((Player) event.getWhoClicked())) {
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		Item item = new Item(block.getLocation().clone().add(0.5, 1.5, 0.5));
		item.setItemStack(itemstack);
		item.setLocked(true);
		item.setGravity(true);
		Vector lift = new Vector(0.0, 0.15, 0.0);
		Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
		item.setVelocity(pickup);
		item.setPickupDelay(32767);
		PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		PacketManager.updateItem(item);
		new BukkitRunnable() {
			public void run() {
				SoundManager.playItemPickup(item.getLocation(), InteractionVisualizer.itemDrop);
				PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 8);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
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
		} catch (Exception e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 3) {
			PacketManager.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
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
		} catch (Exception e) {
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
				PacketManager.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
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
		} catch (Exception e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!openedLooms.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedLooms.get(block);
		if (!map.get("Player").equals((Player) event.getPlayer())) {
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
				Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(block.getLocation().clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
				item.setVelocity(pickup);
				item.setPickupDelay(32767);
				PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
				PacketManager.updateItem(item);
				new BukkitRunnable() {
					public void run() {
						PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
					}
				}.runTaskLater(InteractionVisualizer.plugin, 8);
			}
		}
		
		if (map.get("Banner") instanceof ArmorStand) {
			ArmorStand entity = (ArmorStand) map.get("Banner");
			LightManager.deleteLight(entity.getLocation());
			PacketManager.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
		}
		openedLooms.remove(block);
	}
	
	public static int run() {		
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
							HashMap<String, Object> map = openedLooms.get(block);
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
								LightManager.deleteLight(entity.getLocation());
								PacketManager.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
							}
							openedLooms.remove(block);
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
		if (player.getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}
		if (player.getOpenInventory().getTopInventory().getLocation() == null) {
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
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("Player", player);
			map.putAll(spawnArmorStands(player, block));
			openedLooms.put(block, map);
		}
		HashMap<String, Object> map = openedLooms.get(block);
		
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
			if (!item.getType().equals(stand.getHelmet().getType())) {
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
		LightManager.deleteLight(loc1);
		int skylight = loc1.getBlock().getRelative(BlockFace.UP).getLightFromSky();
		int blocklight = loc1.getBlock().getRelative(BlockFace.UP).getLightFromBlocks() - 1;
		blocklight = blocklight < 0 ? 0 : blocklight;
		if (skylight > 0) {
			LightManager.createLight(loc1, skylight, LightType.SKY);
		}
		if (blocklight > 0) {
			LightManager.createLight(loc1, blocklight, LightType.BLOCK);
		}
	}
	
	public static HashMap<String, ArmorStand> spawnArmorStands(Player player, Block block) {
		HashMap<String, ArmorStand> map = new HashMap<String, ArmorStand>();
		Location loc = block.getLocation().clone().add(0.5, 0.01, 0.5);
		Location temploc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()).setDirection(player.getLocation().getDirection().normalize().multiply(-1));
		float yaw = temploc.getYaw();
		ArmorStand banner = new ArmorStand(loc.clone());
		setStand(banner, yaw);
		
		map.put("Banner", banner);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizer.itemStand, banner);
		
		return map;
	}
	
	public static void setStand(ArmorStand stand, float yaw) {
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
	
	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
	    double rad = Math.toRadians(degrees);
	   
	    double currentX = vector.getX();
	    double currentZ = vector.getZ();
	   
	    double cosine = Math.cos(rad);
	    double sine = Math.sin(rad);
	   
	    return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
	}
}
