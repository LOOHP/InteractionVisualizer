package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.EnchantmentManager;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Entity.Item;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.PacketSending;
import com.loohp.interactionvisualizer.Utils.RomanNumberUtils;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

import net.md_5.bungee.api.ChatColor;

public class EnchantmentTableDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedETable = new HashMap<Block, HashMap<String, Object>>();
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEnchant(EnchantItemEvent event) {
		if (VanishUtils.isVanished(event.getEnchanter())) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getEnchantBlock();
		Location loc = block.getLocation();
		ItemStack itemstack = new ItemStack(event.getItem().getType());
		if (itemstack.getType().equals(Material.BOOK)) {
			itemstack.setType(Material.ENCHANTED_BOOK);
		}
		itemstack.setAmount(event.getItem().getAmount());
		for (Entry<Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
			Enchantment ench = entry.getKey();
			int level = entry.getValue();
			if (itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemstack.getItemMeta();
				meta.addStoredEnchant(ench, level, true);
				itemstack.setItemMeta(meta);
			} else {
				itemstack.addUnsafeEnchantment(ench, level);
			}
		}
		
		if (!openedETable.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedETable.get(block);
		if (!map.get("Player").equals(event.getEnchanter())) {
			return;
		}
		
		if (!(map.get("Item") instanceof Item)) {
			map.put("Item", new Item(loc.clone().add(0.5, 1.3, 0.5)));
		}
		Item item = (Item) map.get("Item");
		
		if (item.isLocked()) {
			return;
		}
		
		item.setItemStack(itemstack);
		item.setLocked(true);
		item.setVelocity(new Vector(0.0, 0.05, 0.0));
		PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
		for (Player each : InteractionVisualizer.itemDrop) {
			each.spawnParticle(Particle.PORTAL, loc.clone().add(0.5, 2.6, 0.5), 75);
		}
		
		new BukkitRunnable() {
			public void run() {
				item.teleport(loc.clone().add(0.5, 2.3, 0.5));
				item.setVelocity(new Vector(0, 0, 0));
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 20);
		
		new BukkitRunnable() {
			public void run() {
				int level = 0;
				Enchantment ench = null;
				for (Entry<Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
					if (entry.getValue() > level) {
						level = entry.getValue();
						ench = entry.getKey();
					}
				}
				@SuppressWarnings("deprecation")
				String enchantmentName = EnchantmentManager.getEnchConfig().getString("Enchantments." + ench.getName());
				item.setCustomName(ChatColor.AQUA + enchantmentName + " " + RomanNumberUtils.toRoman(level));
				item.setCustomNameVisible(true);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 40);
		
		new BukkitRunnable() {
			public void run() {
				item.setCustomName("");
				item.setCustomNameVisible(false);
				item.setGravity(true);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 80);
		
		new BukkitRunnable() {
			public void run() {
				item.teleport(loc.clone().add(0.5, 1.3, 0.5));
				item.setGravity(false);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
				item.setLocked(false);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 88);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEnchantmentTableTake(InventoryClickEvent event) {
		if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
			return;
		}
		if (event.isCancelled()) {
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
			if (event.getCursor().getType().equals(event.getCurrentItem().getType())) {
				return;
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
		if (!InteractionVisualizer.version.contains("legacy")) {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
				return;
			}
		} else {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
				return;
			}
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!openedETable.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedETable.get(block);
		
		ItemStack itemstack = event.getCurrentItem().clone();
		Location loc = block.getLocation();
		
		Player player = (Player) event.getWhoClicked();
		if (map.get("Item") instanceof String) {
			map.put("Item", new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
		}
		Item item = (Item) map.get("Item");
		
		if ((boolean) map.get("Lock")) {
			return;
		}
		map.put("Lock", true);
		
		new BukkitRunnable() {
			public void run() {
				while (item.isLocked()) {
					try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
				}
				new BukkitRunnable() {
					public void run() {
						map.put("Item", "N/A");
						
						item.setItemStack(itemstack);
						item.setLocked(true);
						
						Vector lift = new Vector(0.0, 0.15, 0.0);
						Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
						item.setVelocity(pickup);
						item.setGravity(true);
						item.setPickupDelay(32767);
						PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
						
						new BukkitRunnable() {
							public void run() {
								PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
								openedETable.remove(block);
							}
						}.runTaskLater(InteractionVisualizer.plugin, 8);
					}
				}.runTask(InteractionVisualizer.plugin);
			}
		}.runTaskAsynchronously(InteractionVisualizer.plugin);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseEnchantmentTable(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}
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
		if (!InteractionVisualizer.version.contains("legacy")) {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
				return;
			}
		} else {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
				return;
			}
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 1) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragEnchantmentTable(InventoryDragEvent event) {
		if (event.isCancelled()) {
			return;
		}
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
		if (!InteractionVisualizer.version.contains("legacy")) {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
				return;
			}
		} else {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
				return;
			}
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 1) {
				PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseEnchantmentTable(InventoryCloseEvent event) {
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!InteractionVisualizer.version.contains("legacy")) {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
				return;
			}
		} else {
			if (!event.getView().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
				return;
			}
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!openedETable.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = openedETable.get(block);
		if (!map.get("Player").equals((Player) event.getPlayer())) {
			return;
		}
		
		if ((boolean) map.get("Lock")) {
			return;
		}
		map.put("Lock", true);
		
		if (event.getView().getItem(0) != null) {
			if (!event.getView().getItem(0).getType().equals(Material.AIR)) {
				if (!(map.get("Item") instanceof Item)) {
					map.put("Item", new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
				}
				Item item = (Item) map.get("Item");
				ItemStack itemstack = event.getView().getItem(0).clone();
				Player player = (Player) event.getPlayer();
				Location loc = event.getView().getTopInventory().getLocation();
				new BukkitRunnable() {
					public void run() {
						while (item.isLocked()) {
							try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
						item.setLocked(true);
						new BukkitRunnable() {
							public void run() {
								map.put("Item", "N/A");
								
								item.setItemStack(itemstack, true);
								item.setLocked(true);
								
								Vector lift = new Vector(0.0, 0.15, 0.0);
								Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
								item.setVelocity(pickup);
								item.setGravity(true);
								item.setPickupDelay(32767);
								PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
								
								new BukkitRunnable() {
									public void run() {
										PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
										map.put("Item", "N/A");
									}
								}.runTaskLater(InteractionVisualizer.plugin, 8);
							}
						}.runTask(InteractionVisualizer.plugin);
					}
				}.runTaskAsynchronously(InteractionVisualizer.plugin);
			}
		}
		
		if (map.get("Item") instanceof Item) {
			Item entity = (Item) map.get("Item");
			new BukkitRunnable() {
				public void run() {
					while (entity.isLocked()) {
						try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
					try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					while (entity.isLocked()) {
						try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), entity));
					map.put("Item", "N/A");
				}
			}.runTaskAsynchronously(InteractionVisualizer.plugin);
		}
		new BukkitRunnable() {
			public void run() {
				while (map.get("Item") instanceof Item) {
					try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
				}
				Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> openedETable.remove(block));
			}
		}.runTaskAsynchronously(InteractionVisualizer.plugin);
	}
	
	public static int run() {		
		return new BukkitRunnable() {
			public void run() {
				
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
					if (!InteractionVisualizer.version.contains("legacy")) {
						if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
							continue;
						}
					} else {
						if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
							continue;
						}
					}
					
					InventoryView view = player.getOpenInventory();
					Block block = view.getTopInventory().getLocation().getBlock();
					Location loc = block.getLocation();
					if (!openedETable.containsKey(block)) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("Player", player);
						map.put("Item", "N/A");
						map.put("Lock", false);
						openedETable.put(block, map);
					}
					HashMap<String, Object> map = openedETable.get(block);
					
					if (!map.get("Player").equals(player)) {
						continue;
					}
					
					if (view.getItem(0) != null) {
						ItemStack itemstack = view.getItem(0);
						if (itemstack != null) {
							if (itemstack.getType().equals(Material.AIR)) {
								itemstack = null;
							}
						}
						
						Item item = null;
						if (map.get("Item") instanceof String) {
							if (itemstack != null) {
								item = new Item(loc.clone().add(0.5, 1.3, 0.5));
								item.setItemStack(itemstack);
								item.setVelocity(new Vector(0, 0, 0));
								item.setPickupDelay(32767);
								item.setGravity(false);
								map.put("Item", item);
								PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
								PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
							} else {
								map.put("Item", "N/A");
							}
						} else {
							item = (Item) map.get("Item");
							if (!item.isLocked()) {
								if (itemstack != null) {
									if (!item.getItemStack().equals(itemstack)) {
										item.setItemStack(itemstack);
										PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
									}
									item.setPickupDelay(32767);
									item.setGravity(false);
								} else {
									map.put("Item", "N/A");
									PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
									item.remove();
								}
							}
						}
					}
				}
				
			}
		}.runTaskTimer(InteractionVisualizer.plugin, 0, 5).getTaskId();
	}
}
