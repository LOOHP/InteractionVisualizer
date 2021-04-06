package com.loohp.interactionvisualizer.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.utils.InventoryUtils;
import com.loohp.interactionvisualizer.utils.OpenInvUtils;
import com.loohp.interactionvisualizer.utils.VanishUtils;

public class DropperDisplay implements Listener {
	
	public ConcurrentHashMap<Player, List<Item>> link = new ConcurrentHashMap<Player, List<Item>>();
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseDropper(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (event.isCancelled()) {
			return;
		}
		if (VanishUtils.isVanished(player)) {
			return;
		}
		if (OpenInvUtils.isSlientChest(player)) {
			return;
		}
		if (event.getWhoClicked().getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getType().equals(InventoryType.DROPPER)) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.DROPPER)) {
			return;
		}
		if (event.getClick().equals(ClickType.MIDDLE) && !event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		Location loc = block.getLocation();
		
		boolean isIn = true;
		boolean isMove = false;
		ItemStack itemstack = null;
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 8) {
			
			itemstack = event.getCurrentItem();
			if (itemstack != null) {
				if (itemstack.getType().equals(Material.AIR)) {
					itemstack = null;
				} else {
					isIn = false;
					isMove = true;
				}
			}
			if (itemstack == null) {
				itemstack = event.getCursor();
				if (itemstack != null) {
					if (itemstack.getType().equals(Material.AIR)) {
						itemstack = null;
					} else {
						isMove = true;
					}
				}
			} else {
				if (event.getCursor() != null) {
					if (event.getCursor().getType().equals(itemstack.getType())) {
						isIn = true;
					}
				}
			}
			if (itemstack == null) {
				if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
					itemstack = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
					if (itemstack != null) {
						if (itemstack.getType().equals(Material.AIR)) {
							itemstack = null;
						} else {
							isMove = true;
						}
					}
				}
			}		
		}
		
		if (itemstack == null) {
			if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
				itemstack = event.getCurrentItem();
				if (itemstack != null) {
					if (itemstack.getType().equals(Material.AIR)) {
						itemstack = null;
					} else {
						isMove = true;
					}
				}
			}
		}
		
		if (event.isShiftClick() && event.getView().getItem(event.getRawSlot()) != null) {
			if (isIn) {
				if (!InventoryUtils.stillHaveSpace(event.getView().getTopInventory(), event.getView().getItem(event.getRawSlot()).getType())) {
					return;
				}
			} else {
				if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(event.getRawSlot()).getType())) {
					return;
				}
			}
		}
		if (event.getCursor() != null) {
			if (!event.getCursor().getType().equals(Material.AIR)) {
				if (event.getCurrentItem() != null) {
					if (!event.getCurrentItem().getType().equals(Material.AIR)) {
						if (event.getCurrentItem().getType().equals(event.getCursor().getType())) {
							if (event.getCurrentItem().getAmount() >= event.getCurrentItem().getType().getMaxStackSize()) {
								return;
							}
						}
					}
				}
			}
		}
		
		if (isMove == true) {
			PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
			if (itemstack != null) {
				Item item = new Item(loc.clone().add(0.5, 0.5, 0.5).add(event.getWhoClicked().getLocation().getDirection().multiply(-0.8)));
				Vector offset = new Vector(0.0, 0.15, 0.0);
				Vector vector = loc.clone().add(0.5, 0.5, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).add(0.0, -0.5, 0.0).toVector()).multiply(-0.13).add(offset);
				item.setVelocity(vector);
				if (isIn) {
					item.teleport(event.getWhoClicked().getEyeLocation().add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0));
					vector = loc.clone().add(0.5, 0.5, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector()).multiply(0.13).add(offset);
					item.setVelocity(vector);
				}
				PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), item);
				item.setItemStack(itemstack);
				item.setPickupDelay(32767);
				item.setGravity(true);
				PacketManager.updateItem(item);
				if (!link.containsKey(player)) {
					link.put(player, new ArrayList<Item>());
				}
				List<Item> list = link.get(player);
				list.add(item);
				boolean finalIsIn = isIn;
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
					if (finalIsIn) {
						item.teleport(loc.clone().add(0.5, 0.5, 0.5));
					} else {
						item.teleport(event.getWhoClicked().getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0));
					}
					item.setVelocity(new Vector(0.0, 0.0, 0.0));
					item.setGravity(false);
					PacketManager.updateItem(item);
				}, 8);
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
					PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
					list.remove(item);
				}, 20);
			}						
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragDropper(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.DROPPER)) {
			return;
		}
		
		boolean ok = false;
		for (Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
			ItemStack item = event.getView().getItem(entry.getKey());
			if (item == null) {
				ok = true;
				break;
			}
			if (item.getType().equals(Material.AIR)) {
				ok = true;
				break;
			}
			if (!item.getType().equals(entry.getValue().getType())) {
				continue;
			}
			if (item.getAmount() < item.getType().getMaxStackSize()) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		Location loc = block.getLocation();
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 8) {
				PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
				
				ItemStack itemstack = event.getOldCursor();
				if (itemstack != null) {
					if (itemstack.getType().equals(Material.AIR)) {
						itemstack = null;
					}
				}
				
				if (itemstack != null) {
					Item item = new Item(event.getWhoClicked().getEyeLocation().add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0));
					Vector offset = new Vector(0.0, 0.15, 0.0);
					Vector vector = loc.clone().add(0.5, 0.5, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector()).multiply(0.13).add(offset);
					item.setVelocity(vector);
					PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), item);
					item.setItemStack(itemstack);
					item.setCustomName(System.currentTimeMillis() + "");
					item.setPickupDelay(32767);
					item.setGravity(true);
					PacketManager.updateItem(item);
					if (!link.containsKey(player)) {
						link.put(player, new ArrayList<Item>());
					}
					List<Item> list = link.get(player);
					list.add(item);
					Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
						item.teleport(loc.clone().add(0.5, 0.5, 0.5));
						item.setVelocity(new Vector(0.0, 0.0, 0.0));
						item.setGravity(false);
						PacketManager.updateItem(item);
					}, 8);
					Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
						PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
						list.remove(item);
					}, 20);
				}
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseDropper(InventoryCloseEvent event) {
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
		
		if (!link.containsKey((Player) event.getPlayer())) {
			return;
		}
		List<Item> list = link.get((Player) event.getPlayer());
		Iterator<Item> itr = list.iterator();
		while (itr.hasNext()) {
			Item item = itr.next();
			PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
		}
		
		link.remove((Player) event.getPlayer());
	}
}
