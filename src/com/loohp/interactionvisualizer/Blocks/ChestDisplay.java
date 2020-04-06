package com.loohp.interactionvisualizer.Blocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.EntityCreator;
import com.loohp.interactionvisualizer.Utils.PacketSending;

public class ChestDisplay implements Listener {
	
	public static Scoreboard scoreboard = InteractionVisualizer.scoreboard;
	public static ConcurrentHashMap<Block, List<Item>> link = new ConcurrentHashMap<Block, List<Item>>();
	
	@EventHandler
	public void onUseChest(InventoryClickEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.CHEST)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		Location loc = block.getLocation();
		
		Chest chest = (Chest) block.getState();
		InventoryHolder holder = chest.getInventory().getHolder();
		if (holder instanceof DoubleChest) {
			return;
		}
		
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
		
		if (isMove == true) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
			if (itemstack != null) {
				Item item = (Item) EntityCreator.create(loc.clone().add(0.5, 1, 0.5), EntityType.DROPPED_ITEM);
				PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
				item.setItemStack(itemstack);
				if (isIn == true) {
					scoreboard.getTeam("ChestIn").addEntry(item.getUniqueId().toString());
				} else {
					scoreboard.getTeam("ChestOut").addEntry(item.getUniqueId().toString());
				}
				item.setVelocity(new Vector(0, 0, 0));
				item.setPickupDelay(32767);
				item.setGravity(false);
				item.setGlowing(true);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
				if (!link.containsKey(block)) {
					link.put(block, new ArrayList<Item>());
				}
				List<Item> list = link.get(block);
				list.add(item);
				new BukkitRunnable() {
					public void run() {
						PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
						list.remove(item);
						item.remove();
					}
				}.runTaskLaterAsynchronously(InteractionVisualizer.plugin, 40);
			}						
		}
	}
	
	@EventHandler
	public void onDragChest(InventoryDragEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.CHEST)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		Location loc = block.getLocation();
		
		Chest chest = (Chest) block.getState();
		InventoryHolder holder = chest.getInventory().getHolder();
		if (holder instanceof DoubleChest) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 26) {
				PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				
				ItemStack itemstack = event.getOldCursor();
				if (itemstack != null) {
					if (itemstack.getType().equals(Material.AIR)) {
						itemstack = null;
					}
				}
				
				if (itemstack != null) {
					Item item = (Item) EntityCreator.create(loc.clone().add(0.5, 1, 0.5), EntityType.DROPPED_ITEM);
					PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
					item.setItemStack(itemstack);
					scoreboard.getTeam("ChestIn").addEntry(item.getUniqueId().toString());
					item.setCustomName(System.currentTimeMillis() + "");
					item.setVelocity(new Vector(0, 0, 0));
					item.setPickupDelay(32767);
					item.setGravity(false);
					item.setGlowing(true);
					PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
					if (!link.containsKey(block)) {
						link.put(block, new ArrayList<Item>());
					}
					List<Item> list = link.get(block);
					list.add(item);
					new BukkitRunnable() {
						public void run() {
							PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
							list.remove(item);
							item.remove();
						}
					}.runTaskLaterAsynchronously(InteractionVisualizer.plugin, 40);
				}
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseChest(InventoryCloseEvent event) {
		if (event.getView().getTopInventory() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation() == null) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!link.containsKey(block)) {
			return;
		}
		List<Item> list = link.get(block);
		Iterator<Item> itr = list.iterator();
		while (itr.hasNext()) {
			Item item = itr.next();
			PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
			item.remove();
		}
		
		link.remove(block);
	}
}
