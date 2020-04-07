package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.EntityCreator;
import com.loohp.interactionvisualizer.Utils.PacketSending;

public class EnchantmentTableDisplay implements Listener {
	
	public static HashMap<Block, HashMap<String, Object>> openedETable = new HashMap<Block, HashMap<String, Object>>();

	@EventHandler
	public void onUseEnchantmentTable(InventoryClickEvent event) {
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
	
	@EventHandler
	public void onDragEnchantmentTable(InventoryDragEvent event) {
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
		
		if (map.get("Item") instanceof Item) {
			Entity entity = (Entity) map.get("Item");
			PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), (Item) entity);
		}
		openedETable.remove(block);
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
								item = (Item) EntityCreator.create(loc.clone().add(0.5, 1.3, 0.5), EntityType.DROPPED_ITEM);
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
		}.runTaskTimer(InteractionVisualizer.plugin, 0, 5).getTaskId();
	}
}
