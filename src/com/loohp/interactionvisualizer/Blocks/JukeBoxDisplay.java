package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.Managers.MusicManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager.TileEntityType;
import com.loohp.interactionvisualizer.Utils.ChatColorUtils;
import com.loohp.interactionvisualizer.Utils.LegacyRecordsUtils;

import net.md_5.bungee.api.ChatColor;

public class JukeBoxDisplay implements Listener {
	
	public static ConcurrentHashMap<Block, HashMap<String, Object>> jukeboxMap = new ConcurrentHashMap<Block, HashMap<String, Object>>();
	private static Integer checkingPeriod = InteractionVisualizer.jukeboxChecking;
	private static Integer gcPeriod = InteractionVisualizer.gcPeriod;
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakJukeBox(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!jukeboxMap.containsKey(block)) {
			return;
		}

		HashMap<String, Object> map = jukeboxMap.get(block);
		if (map.get("Item") instanceof Item) {
			Item item = (Item) map.get("Item");
			PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
		}
		jukeboxMap.remove(block);
	}
	
	public static int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, HashMap<String, Object>>> itr = jukeboxMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) jukeboxMap.size() / (double) gcPeriod);
			int delay = 1;
			while (itr.hasNext()) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				Entry<Block, HashMap<String, Object>> entry = itr.next();
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
					Block block = entry.getKey();
					boolean active = false;
					if (isActive(block.getLocation())) {
						active = true;
					}
					if (active == false) {
						HashMap<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
						}
						jukeboxMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.JUKEBOX)) {
						HashMap<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
						}
						jukeboxMap.remove(block);
						return;
					}
				}, delay);
			}
		}, 0, gcPeriod).getTaskId();
	}
	
	public static int run() {		
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
				List<Block> list = nearbyJukeBox();
				for (Block block : list) {
					if (jukeboxMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.JUKEBOX)) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("Item", "N/A");
							jukeboxMap.put(block, map);
						}
					}
				}
			});				
			
			Iterator<Entry<Block, HashMap<String, Object>>> itr = jukeboxMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) jukeboxMap.size() / (double) checkingPeriod);
			int delay = 1;
			while (itr.hasNext()) {
				Entry<Block, HashMap<String, Object>> entry = itr.next();
				
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
					Block block = entry.getKey();
					if (!isActive(block.getLocation())) {
						return;
					}
					if (!block.getType().equals(Material.JUKEBOX)) {
						return;
					}
					org.bukkit.block.Jukebox jukebox = (org.bukkit.block.Jukebox) block.getState();
					
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						ItemStack itemstack = InteractionVisualizer.version.isLegacy() ? (jukebox.getPlaying() == null ? null : (jukebox.getPlaying().equals(Material.AIR) ? null : new ItemStack(jukebox.getPlaying(), 1))) : (jukebox.getRecord() == null ? null : (jukebox.getRecord().getType().equals(Material.AIR) ? null : jukebox.getRecord().clone()));
						
						Item item = null;
						if (entry.getValue().get("Item") instanceof String) {
							if (itemstack != null) {
								String disc = InteractionVisualizer.version.isLegacy() ? LegacyRecordsUtils.translateFromLegacy(jukebox.getPlaying().toString().toUpperCase()) : jukebox.getPlaying().toString().toUpperCase();
								String text = getColor(disc) + (!itemstack.getItemMeta().hasDisplayName() ? ChatColorUtils.translateAlternateColorCodes('&', MusicManager.getMusicConfig().getString("Discs." + disc)) : itemstack.getItemMeta().getDisplayName());
								
								item = new Item(jukebox.getLocation().clone().add(0.5, 1.0, 0.5));
								item.setItemStack(itemstack);
								item.setVelocity(new Vector(0, 0, 0));
								item.setPickupDelay(32767);
								item.setGravity(false);
								item.setCustomName(text);
								item.setCustomNameVisible(true);
								entry.getValue().put("Item", item);
								PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
								PacketManager.updateItem(item);
							} else {
								entry.getValue().put("Item", "N/A");
							}
						} else {
							item = (Item) entry.getValue().get("Item");
							if (itemstack != null) {
								if (!item.getItemStack().equals(itemstack)) {
									item.setItemStack(itemstack);
									String disc = InteractionVisualizer.version.isLegacy() ? LegacyRecordsUtils.translateFromLegacy(jukebox.getPlaying().toString().toUpperCase()) : jukebox.getPlaying().toString().toUpperCase();
									String text = getColor(disc) + (!itemstack.getItemMeta().hasDisplayName() ? MusicManager.getMusicConfig().getString("Discs." + disc) : itemstack.getItemMeta().getDisplayName());
									
									item.setCustomName(text);
									item.setCustomNameVisible(true);
									PacketManager.updateItem(item);
								}
							} else {
								entry.getValue().put("Item", "N/A");
								PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	public static List<Block> nearbyJukeBox() {
		return TileEntityManager.getTileEntites(TileEntityType.JUKEBOX);
	}
	
	public static boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public static ChatColor getColor(String material) {
		switch (material) {
		case "MUSIC_DISC_11":
			return ChatColor.WHITE;
		case "MUSIC_DISC_13":
			return ChatColor.GOLD;
		case "MUSIC_DISC_BLOCKS":
			return ChatColor.RED;
		case "MUSIC_DISC_CAT":
			return ChatColor.GREEN;
		case "MUSIC_DISC_CHIRP":
			return ChatColor.DARK_RED;
		case "MUSIC_DISC_FAR":
			return ChatColor.GREEN;
		case "MUSIC_DISC_MALL":
			return ChatColor.BLUE;
		case "MUSIC_DISC_MELLOHI":
			return ChatColor.LIGHT_PURPLE;
		case "MUSIC_DISC_STAL":
			return ChatColor.WHITE;
		case "MUSIC_DISC_STRAD":
			return ChatColor.WHITE;
		case "MUSIC_DISC_WAIT":
			return ChatColor.AQUA;
		case "MUSIC_DISC_WARD":
			return ChatColor.DARK_GREEN;
		case "MUSIC_DISC_PIGSTEP":
			return ChatColor.GOLD;
		default:
			return ChatColor.WHITE;
		}
	}

}
