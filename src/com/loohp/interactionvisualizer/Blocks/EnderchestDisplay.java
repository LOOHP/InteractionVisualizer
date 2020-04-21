package com.loohp.interactionvisualizer.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Holder.Item;
import com.loohp.interactionvisualizer.Manager.PacketManager;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.MaterialUtils;
import com.loohp.interactionvisualizer.Utils.OpenInvUtils;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

public class EnderchestDisplay implements Listener {
	
	public static ConcurrentHashMap<Player, List<Item>> link = new ConcurrentHashMap<Player, List<Item>>();
	public static HashMap<Player, Block> playermap = new HashMap<Player, Block>();
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onOpenEnderChest(InventoryOpenEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (VanishUtils.isVanished((Player) event.getPlayer())) {
			return;
		}
		if (OpenInvUtils.isSlientChest((Player) event.getPlayer())) {
			return;
		}
		if (event.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
			return;
		}
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getType().equals(InventoryType.ENDER_CHEST)) {
			return;
		}
		if (!InteractionVisualizer.version.contains("legacy") && !InteractionVisualizer.version.equals("1.13") && !InteractionVisualizer.version.equals("1.13.1")) {
			if (event.getPlayer().getTargetBlockExact(7, FluidCollisionMode.NEVER) != null) {
				if (!event.getPlayer().getTargetBlockExact(7, FluidCollisionMode.NEVER).getType().equals(Material.ENDER_CHEST)) {
					return;
				}
			} else {
				return;
			}
		} else {
			if (event.getPlayer().getTargetBlock(MaterialUtils.getNonSolidSet(), 7) != null) {
				if (!event.getPlayer().getTargetBlock(MaterialUtils.getNonSolidSet(), 7).getType().equals(Material.ENDER_CHEST)) {
					return;
				}
			} else {
				return;
			}
		}
		
		Block block = null;
		if (!InteractionVisualizer.version.contains("legacy") && !InteractionVisualizer.version.equals("1.13") && !InteractionVisualizer.version.equals("1.13.1")) {
			block = event.getPlayer().getTargetBlockExact(7, FluidCollisionMode.NEVER);
		} else {
			block = event.getPlayer().getTargetBlock(MaterialUtils.getNonSolidSet(), 7);
		}
		
		playermap.put((Player) event.getPlayer(), block);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseEnderChest(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
			return;
		}
		
		Block block = playermap.get((Player) event.getWhoClicked());
		Location loc = block.getLocation();
		
		boolean isIn = true;
		boolean isMove = false;
		ItemStack itemstack = null;
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 26) {
			
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
		
		if (event.isShiftClick()) {
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
			PacketManager.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
			if (itemstack != null) {
				Item item = new Item(loc.clone().add(0.5, 1, 0.5));
				Vector offset = new Vector(0.0, 0.15, 0.0);
				Vector vector = loc.clone().add(0.5, 1, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().add(0.0, -0.5, 0.0).toVector()).multiply(-0.15).add(offset);
				item.setVelocity(vector);
				if (isIn) {
					item.teleport(event.getWhoClicked().getEyeLocation());
					vector = loc.clone().add(0.5, 1, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().toVector()).multiply(0.15).add(offset);
					item.setVelocity(vector);
				}
				PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
				item.setItemStack(itemstack);
				item.setPickupDelay(32767);
				item.setGravity(true);
				PacketManager.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
				if (!link.containsKey((Player) event.getWhoClicked())) {
					link.put((Player) event.getWhoClicked(), new ArrayList<Item>());
				}
				List<Item> list = link.get((Player) event.getWhoClicked());
				list.add(item);
				boolean finalIsIn = isIn;
				new BukkitRunnable() {
					public void run() {
						if (finalIsIn) {
							item.teleport(loc.clone().add(0.5, 1, 0.5));
						} else {
							item.teleport(event.getWhoClicked().getEyeLocation().add(0.0, -0.5, 0.0));
						}
						item.setVelocity(new Vector(0.0, 0.0, 0.0));
						item.setGravity(false);
						PacketManager.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
					}
				}.runTaskLater(InteractionVisualizer.plugin, 8);
				new BukkitRunnable() {
					public void run() {
						PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
						list.remove(item);
					}
				}.runTaskLater(InteractionVisualizer.plugin, 20);
			}						
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragEnderChest(InventoryDragEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
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
		
		Block block = playermap.get((Player) event.getWhoClicked());
		Location loc = block.getLocation();
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 26) {
				PacketManager.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				
				ItemStack itemstack = event.getOldCursor();
				if (itemstack != null) {
					if (itemstack.getType().equals(Material.AIR)) {
						itemstack = null;
					}
				}
				
				if (itemstack != null) {
					Item item = new Item(event.getWhoClicked().getEyeLocation());
					Vector offset = new Vector(0.0, 0.15, 0.0);
					Vector vector = loc.clone().add(0.5, 1, 0.5).toVector().subtract(event.getWhoClicked().getEyeLocation().clone().toVector()).multiply(0.15).add(offset);
					item.setVelocity(vector);
					PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
					item.setItemStack(itemstack);
					item.setCustomName(System.currentTimeMillis() + "");
					item.setPickupDelay(32767);
					item.setGravity(true);
					PacketManager.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
					if (!link.containsKey((Player) event.getWhoClicked())) {
						link.put((Player) event.getWhoClicked(), new ArrayList<Item>());
					}
					List<Item> list = link.get((Player) event.getWhoClicked());
					list.add(item);
					new BukkitRunnable() {
						public void run() {
							item.teleport(loc.clone().add(0.5, 1, 0.5));
							item.setVelocity(new Vector(0.0, 0.0, 0.0));
							item.setGravity(false);
							PacketManager.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
						}
					}.runTaskLater(InteractionVisualizer.plugin, 8);
					new BukkitRunnable() {
						public void run() {
							PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
							list.remove(item);
						}
					}.runTaskLater(InteractionVisualizer.plugin, 20);
				}
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseEnderChest(InventoryCloseEvent event) {
		if (!playermap.containsKey((Player) event.getPlayer())) {
			return;
		}
		
		if (!link.containsKey((Player) event.getPlayer())) {
			return;
		}
		
		List<Item> list = link.get((Player) event.getPlayer());
		Iterator<Item> itr = list.iterator();
		while (itr.hasNext()) {
			Item item = itr.next();
			PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
		}
		
		link.remove((Player) event.getPlayer());
		playermap.remove((Player) event.getPlayer());
	}
}
