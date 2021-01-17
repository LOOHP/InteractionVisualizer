package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.API.VisualizerInteractDisplay;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.ObjectHolders.EnchantmentTableBundle;
import com.loohp.interactionvisualizer.Utils.CustomMapUtils;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

public class EnchantmentTableDisplay extends VisualizerInteractDisplay implements Listener {
	
	public Map<Block, EnchantmentTableBundle> openedETable = new ConcurrentHashMap<Block, EnchantmentTableBundle>();
	public Map<Player, Block> playermap = new ConcurrentHashMap<Player, Block>();
	
	@Override
	public void process(Player player) {		
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
			if (!InteractionVisualizer.version.isLegacy()) {
				if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTING_TABLE")) {
					return;
				}
			} else {
				if (!player.getOpenInventory().getTopInventory().getLocation().getBlock().getType().toString().toUpperCase().equals("ENCHANTMENT_TABLE")) {
					return;
				}
			}
			InventoryView view = player.getOpenInventory();
			Block block = view.getTopInventory().getLocation().getBlock();
			playermap.put(player, block);
		}
		
		InventoryView view = player.getOpenInventory();
		Block block = playermap.get(player);
		if (!openedETable.containsKey(block)) {
			openedETable.put(block, new EnchantmentTableBundle(player, block, InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP)));
		}
		EnchantmentTableBundle etb = openedETable.get(block);
		
		if (!etb.getEnchanter().equals(player)) {
			return;
		}
		
		ItemStack itemstack = view.getItem(0) != null && !view.getItem(0).getType().equals(Material.AIR) ? view.getItem(0).clone() : null;

		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> etb.setItemStack(itemstack), 2);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onEnchant(EnchantItemEvent event) {
		if (VanishUtils.isVanished(event.getEnchanter())) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getEnchantBlock();
		Player player = event.getEnchanter();
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			if (!player.getOpenInventory().getTopInventory().getType().equals(InventoryType.ENCHANTING)) {
				return;
			}
			ItemStack itemstack = player.getOpenInventory().getItem(0).clone();
			Map<Enchantment, Integer> enchantsAdded = new HashMap<Enchantment, Integer>();
			if (itemstack.getType().equals(Material.ENCHANTED_BOOK)) {
				enchantsAdded.putAll(((EnchantmentStorageMeta) itemstack.getItemMeta()).getStoredEnchants());
			}
			enchantsAdded.putAll(itemstack.getEnchantments());
			
			enchantsAdded = CustomMapUtils.sortMapByValueReverse(enchantsAdded);
			
			EnchantmentTableBundle etb = openedETable.get(block);
			if (etb == null || !etb.getEnchanter().equals(player)) {
				return;
			}
			
			etb.playEnchantAnimation(enchantsAdded, event.getExpLevelCost(), itemstack);
		}, 2);
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
		
		if (!playermap.containsKey((Player) event.getWhoClicked())) {
			return;
		}
		
		Block block = playermap.get((Player) event.getWhoClicked());
		Player player = (Player) event.getWhoClicked();
		
		EnchantmentTableBundle etb = openedETable.get(block);
		if (etb == null || !etb.getEnchanter().equals(player)) {
			return;
		}
		
		ItemStack itemstack = event.getCurrentItem().clone();		
		int slot = event.getRawSlot();
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			if (player.getOpenInventory().getItem(slot) == null || (itemstack.isSimilar(player.getOpenInventory().getItem(slot)) && itemstack.getAmount() == player.getOpenInventory().getItem(slot).getAmount())) {
				return;
			}
		
			etb.playPickUpAnimation(itemstack);
			
		}, 1);
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseEnchantmentTable(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		
		if (!playermap.containsKey(player)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 1) {
			PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragEnchantmentTable(InventoryDragEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		Player player = (Player) event.getWhoClicked();
		
		if (!playermap.containsKey(player)) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 1) {
				PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), player);
				break;
			}
		}
	}
	
	@EventHandler
	public void onCloseEnchantmentTable(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		
		if (!playermap.containsKey(player)) {
			return;
		}
		
		Block block = playermap.remove(player);
		
		ItemStack itemstack = event.getView().getItem(0) != null ? (!event.getView().getItem(0).getType().equals(Material.AIR) ? event.getView().getItem(0).clone() : null) : null;
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			EnchantmentTableBundle etb = openedETable.get(block);
			if (etb == null || !etb.getEnchanter().equals(player)) {
				return;
			}
			
			etb.playPickUpAnimationAndRemove(itemstack, openedETable);
		}, 1);
	}
}
