package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import com.loohp.interactionvisualizer.Entity.ArmorStand;
import com.loohp.interactionvisualizer.Entity.Item;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.PacketSending;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class LoomDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedLooms = new HashMap<Block, HashMap<String, Object>>();
	
	@EventHandler
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
		if (event.getView().getTopInventory().getLocation() == null) {
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
		PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
		new BukkitRunnable() {
			public void run() {
				PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 8);
	}
	
	@EventHandler
	public void onUseLoom(InventoryClickEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 3) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler
	public void onDragLoom(InventoryDragEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 3) {
				PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseLoom(InventoryCloseEvent event) {
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
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
				PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
				new BukkitRunnable() {
					public void run() {
						PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
					}
				}.runTaskLater(InteractionVisualizer.plugin, 8);
			}
		}
		
		if (map.get("Banner") instanceof ArmorStand) {
			ArmorStand entity = (ArmorStand) map.get("Banner");
			LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
			for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
				LightAPI.updateChunk(info, LightType.BLOCK);
			}
			PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
			entity.remove();
		}
		openedLooms.remove(block);
	}
	
	public static int run() {		
		return new BukkitRunnable() {
			public void run() {
				
				Iterator<Entry<Block, HashMap<String, Object>>> itr = openedLooms.entrySet().iterator();
				while (itr.hasNext()) {
					Entry<Block, HashMap<String, Object>> entry = itr.next();
					Block block = entry.getKey();
					HashMap<String, Object> map = entry.getValue();
					if (block.getType().equals(Material.LOOM)) {
						Player player = (Player) map.get("Player");
						if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
							if (player.getOpenInventory() != null) {
								if (player.getOpenInventory().getTopInventory() != null) {
									if (player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
										continue;
									}
								}
							}
						}
					}
					
					if (map.get("Banner") instanceof ArmorStand) {
						ArmorStand entity = (ArmorStand) map.get("Banner");
						LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
						for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
							LightAPI.updateChunk(info, LightType.BLOCK);
						}
						PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), (ArmorStand) entity);
						entity.remove();
					}
					itr.remove();
				}
				
				for (Player player : InteractionVisualizer.getOnlinePlayers()) {
					if (VanishUtils.isVanished(player)) {
						continue;
					}
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
					if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().equals(Material.LOOM)) {
						continue;
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
						continue;
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
						stand.setHelmet(item);
					} else {
						stand.setHelmet(new ItemStack(Material.AIR));
					}
					PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
					
					Location loc1 = ((ArmorStand) map.get("Banner")).getLocation();
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
	
	public static HashMap<String, ArmorStand> spawnArmorStands(Player player, Block block) {
		HashMap<String, ArmorStand> map = new HashMap<String, ArmorStand>();
		Location loc = block.getLocation().clone().add(0.5, 0.01, 0.5);
		Location temploc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ()).setDirection(player.getLocation().getDirection().normalize().multiply(-1));
		float yaw = temploc.getYaw();
		ArmorStand banner = new ArmorStand(loc.clone());
		setStand(banner, yaw);
		
		map.put("Banner", banner);
		
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.itemStand, banner);
		
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
