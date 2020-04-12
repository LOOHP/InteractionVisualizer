package com.loohp.interactionvisualizer.Entities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolder.Item;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.PacketSending;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

public class VillagerDisplay implements Listener {
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onVillageTrade(InventoryClickEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
			return;
		}
		if (!(event.getView().getTopInventory() instanceof MerchantInventory)) {
			return;
		}
		if (event.getRawSlot() != 2) {
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
			if (!InventoryUtils.stillHaveSpace(event.getWhoClicked().getInventory(), event.getView().getItem(2).getType())) {
				return;
			}
		}
		
		Player player = (Player) event.getWhoClicked();
		MerchantInventory tradeinv = (MerchantInventory) event.getView().getTopInventory();
		if (!(tradeinv.getHolder() instanceof Villager)) {
			return;
		}
		Villager villager = (Villager) tradeinv.getHolder();
		Vector lift = new Vector(0.0, 0.20, 0.0);
		if (event.getView().getItem(0) != null) {
			if (!event.getView().getItem(0).getType().equals(Material.AIR)) {
				Item in = new Item(player.getEyeLocation());
				Vector vector = villager.getEyeLocation().add(0.5, -0.5, 0.5).toVector().subtract(player.getEyeLocation().toVector()).multiply(0.12).add(lift);
				in.setItemStack(event.getView().getItem(0));
				in.setGravity(true);
				in.setVelocity(vector);
				PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, in);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), in);
				
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), in), 12);
			}
		}
		
		new BukkitRunnable() {
			public void run() {
				if (event.getView().getItem(1) != null) {
					if (!event.getView().getItem(1).getType().equals(Material.AIR)) {
						Item in = new Item(player.getEyeLocation());
						Vector vector = villager.getEyeLocation().add(0.5, -0.5, 0.5).toVector().subtract(player.getEyeLocation().toVector()).multiply(0.12).add(lift);
						in.setItemStack(event.getView().getItem(1));
						in.setGravity(true);
						in.setVelocity(vector);
						PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, in);
						PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), in);
						
						Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), in), 12);
					}
				}
			}
		}.runTaskLater(InteractionVisualizer.plugin, 8);
				
		new BukkitRunnable() {
			public void run() {
				Item out = new Item(villager.getEyeLocation());
				Vector vector = player.getEyeLocation().add(0.5, -0.5, 0.5).toVector().subtract(villager.getEyeLocation().toVector()).multiply(0.10).add(lift);
				out.setItemStack(event.getCurrentItem());
				out.setGravity(true);
				out.setVelocity(vector);
				PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, out);
				PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), out);
				
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), out), 12);
			}
		}.runTaskLater(InteractionVisualizer.plugin, 40);
	}

}
