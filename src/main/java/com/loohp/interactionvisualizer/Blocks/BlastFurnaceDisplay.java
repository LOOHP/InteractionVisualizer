package com.loohp.interactionvisualizer.Blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.API.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.API.Events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.Managers.SoundManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.Utils.ChatColorUtils;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.MCVersion;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

import net.md_5.bungee.api.ChatColor;

public class BlastFurnaceDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, HashMap<String, Object>> blastfurnaceMap = new ConcurrentHashMap<Block, HashMap<String, Object>>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private String progressBarCharacter = "";
	private String emptyColor = "&7";
	private String filledColor = "&e";
	private String noFuelColor = "&c";
	private int progressBarLength = 10;
	private String amountPending = " &7+{Amount}";
	
	public BlastFurnaceDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.BlastFurnace.CheckingPeriod");
		gcPeriod = InteractionVisualizer.plugin.getConfig().getInt("GarbageCollector.Period");
		progressBarCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BlastFurnace.Options.ProgressBarCharacter"));
		emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BlastFurnace.Options.EmptyColor"));
		filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BlastFurnace.Options.FilledColor"));
		noFuelColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BlastFurnace.Options.NoFuelColor"));
		progressBarLength = InteractionVisualizer.plugin.getConfig().getInt("Blocks.BlastFurnace.Options.ProgressBarLength");
		amountPending = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BlastFurnace.Options.AmountPending"));
	}
	
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, HashMap<String, Object>>> itr = blastfurnaceMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) blastfurnaceMap.size() / (double) gcPeriod);
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
					if (!isActive(block.getLocation())) {
						HashMap<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
						}
						if (map.get("Stand") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("Stand");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						blastfurnaceMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.BLAST_FURNACE)) {
						HashMap<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
						}
						if (map.get("Stand") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("Stand");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						blastfurnaceMap.remove(block);
						return;
					}
				}, delay);
			}
		}, 0, gcPeriod).getTaskId();
	}
	
	@Override
	public int run() {		
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
				List<Block> list = nearbyBlastFurnace();
				for (Block block : list) {
					if (blastfurnaceMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.BLAST_FURNACE)) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.put("Item", "N/A");
							map.putAll(spawnArmorStands(block));
							blastfurnaceMap.put(block, map);
						}
					}
				}
			});				
			
			Iterator<Entry<Block, HashMap<String, Object>>> itr = blastfurnaceMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) blastfurnaceMap.size() / (double) checkingPeriod);
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
					if (!block.getType().equals(Material.BLAST_FURNACE)) {
						return;
					}
					org.bukkit.block.BlastFurnace blastfurnace = (org.bukkit.block.BlastFurnace) block.getState();
					
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						Inventory inv = blastfurnace.getInventory();
						ItemStack itemstack = inv.getItem(0);
						if (itemstack != null) {
							if (itemstack.getType().equals(Material.AIR)) {
								itemstack = null;
							}
						}
						
						if (itemstack == null) {
							itemstack = inv.getItem(2);
							if (itemstack != null) {
								if (itemstack.getType().equals(Material.AIR)) {
									itemstack = null;
								}
							}
						}
						
						Item item = null;
						if (entry.getValue().get("Item") instanceof String) {
							if (itemstack != null) {
								item = new Item(blastfurnace.getLocation().clone().add(0.5, 1.0, 0.5));
								item.setItemStack(itemstack);
								item.setVelocity(new Vector(0, 0, 0));
								item.setPickupDelay(32767);
								item.setGravity(false);
								entry.getValue().put("Item", item);
								PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), item);
								PacketManager.updateItem(item);
							} else {
								entry.getValue().put("Item", "N/A");
							}
						} else {
							item = (Item) entry.getValue().get("Item");
							if (itemstack != null) {
								if (!item.getItemStack().equals(itemstack)) {
									item.setItemStack(itemstack);
									PacketManager.updateItem(item);
								}
							} else {
								entry.getValue().put("Item", "N/A");
								PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
							}
						}
	
						ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
						if (hasItemToCook(blastfurnace)) {
							int time = blastfurnace.getCookTime();
							int max = 10 * 20;
							if (!InteractionVisualizer.version.isLegacy() && !InteractionVisualizer.version.equals(MCVersion.V1_13) && !InteractionVisualizer.version.equals(MCVersion.V1_13_1)) {
								max = blastfurnace.getCookTimeTotal();
							}
							String symbol = "";
							double percentagescaled = (double) time / (double) max * (double) progressBarLength;
							double i = 1;
							for (i = 1; i < percentagescaled; i++) {
								symbol = symbol + filledColor + progressBarCharacter;
							}
							i = i - 1;
							if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.33) {
								symbol += emptyColor + progressBarCharacter;
							} else if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.67) {
								symbol += emptyColor + progressBarCharacter;
							} else if ((percentagescaled - i) > 0) {
								symbol += filledColor + progressBarCharacter;
							}
							for (i = progressBarLength - 1; i >= percentagescaled; i--) {
								symbol += emptyColor + progressBarCharacter;
							}
							
							int left = inv.getItem(0).getAmount() - 1;
							if (left > 0) {
								symbol += amountPending.replace("{Amount}", left + "");
							}
							if (symbol.contains("{CompletedAmount}")) {
								symbol = symbol.replace("{CompletedAmount}", (inv.getItem(2) == null ? 0 : inv.getItem(2).getAmount()) + "");
							}
							if (hasFuel(blastfurnace)) {
								if (!stand.getCustomName().toPlainText().equals(symbol) || !stand.isCustomNameVisible()) {
									stand.setCustomNameVisible(true);
									stand.setCustomName(symbol);
									PacketManager.updateArmorStandOnlyMeta(stand);
								}
							} else {
								symbol = noFuelColor + ChatColor.stripColor(symbol);
								if (!stand.getCustomName().toPlainText().equals(symbol) || !stand.isCustomNameVisible()) {
									stand.setCustomNameVisible(true);
									stand.setCustomName(symbol);
									PacketManager.updateArmorStandOnlyMeta(stand);
								}
							}
						} else {					
							if (!stand.getCustomName().toPlainText().equals("") || stand.isCustomNameVisible()) {
								stand.setCustomNameVisible(false);
								stand.setCustomName("");
								PacketManager.updateArmorStandOnlyMeta(stand);
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlastFurnace(InventoryClickEvent event) {
		if (VanishUtils.isVanished((Player) event.getWhoClicked())) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		if (event.getRawSlot() != 0 && event.getRawSlot() != 2) {
			return;
		}
		if (event.getCurrentItem() == null) {
			return;
		}
		if (event.getCurrentItem().getType().equals(Material.AIR)) {
			return;
		}
		if (event.getRawSlot() == 2) {
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
		if (event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD)) {
			if (event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) != null && !event.getWhoClicked().getInventory().getItem(event.getHotbarButton()).getType().equals(Material.AIR)) {
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
		} catch (Exception | AbstractMethodError e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.BLAST_FURNACE)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!blastfurnaceMap.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = blastfurnaceMap.get(block);
		
		int slot = event.getRawSlot();
		ItemStack itemstack = event.getCurrentItem().clone();
		Location loc = block.getLocation();	
		Player player = (Player) event.getWhoClicked();
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			
			if (player.getOpenInventory().getItem(slot) == null || (itemstack.isSimilar(player.getOpenInventory().getItem(slot)) && itemstack.getAmount() == player.getOpenInventory().getItem(slot).getAmount())) {
				return;
			}
			
			if (map.get("Item") instanceof String) {
				map.put("Item", new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
			}
			Item item = (Item) map.get("Item");
			
			map.put("Item", "N/A");
			
			item.setItemStack(itemstack);
			item.setLocked(true);
			
			Vector lift = new Vector(0.0, 0.15, 0.0);
			Vector pickup = player.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(loc.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
			item.setVelocity(pickup);
			item.setGravity(true);
			item.setPickupDelay(32767);
			PacketManager.updateItem(item);
			
			Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
				SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP));
				PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
			}, 8);
		}, 1);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseBlastFurnace(InventoryClickEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.BLAST_FURNACE)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 2) {
			PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragBlastFurnace(InventoryDragEvent event) {
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
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.BLAST_FURNACE)) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 2) {
				PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
				break;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakBlastFurnace(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!blastfurnaceMap.containsKey(block)) {
			return;
		}

		HashMap<String, Object> map = blastfurnaceMap.get(block);
		if (map.get("Item") instanceof Item) {
			Item item = (Item) map.get("Item");
			PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
		}
		if (map.get("Stand") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("Stand");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		blastfurnaceMap.remove(block);
	}
	
	public boolean hasItemToCook(org.bukkit.block.BlastFurnace blastfurnace) {
		Inventory inv = blastfurnace.getInventory();
		if (inv.getItem(0) != null) {
			if (!inv.getItem(0).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasFuel(org.bukkit.block.BlastFurnace blastfurnace) {
		if (blastfurnace.getBurnTime() > 0) {
			return true;
		}
		Inventory inv = blastfurnace.getInventory();
		if (inv.getItem(1) != null) {
			if (!inv.getItem(1).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Block> nearbyBlastFurnace() {
		return TileEntityManager.getTileEntites(TileEntityType.BLAST_FURNACE);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public HashMap<String, ArmorStand> spawnArmorStands(Block block) {
		HashMap<String, ArmorStand> map = new HashMap<String, ArmorStand>();
		Location origin = block.getLocation();	
		
		BlockData blockData = block.getState().getBlockData();
		BlockFace facing = ((Directional) blockData).getFacing();			
		Location target = block.getRelative(facing).getLocation();
		Vector direction = target.toVector().subtract(origin.toVector()).multiply(0.7);
		
		Location loc = block.getLocation().clone().add(direction).add(0.5, 0.2, 0.5);
		ArmorStand slot1 = new ArmorStand(loc.clone());
		setStand(slot1);
		
		map.put("Stand", slot1);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), slot1);
		
		return map;
	}
	
	public void setStand(ArmorStand stand) {
		stand.setBasePlate(false);
		stand.setMarker(true);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setInvulnerable(true);
		stand.setVisible(false);
		stand.setSilent(true);
		stand.setCustomName("");
		stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
	}

}
