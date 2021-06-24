package com.loohp.interactionvisualizer.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.api.events.TileEntityRemovedEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.SoundManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import com.loohp.interactionvisualizer.utils.InventoryUtils;
import com.loohp.interactionvisualizer.utils.LegacyFacingUtils;
import com.loohp.interactionvisualizer.utils.MCVersion;
import com.loohp.interactionvisualizer.utils.VanishUtils;

import net.md_5.bungee.api.ChatColor;

public class FurnaceDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, Map<String, Object>> furnaceMap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private String progressBarCharacter = "";
	private String emptyColor = "&7";
	private String filledColor = "&e";
	private String noFuelColor = "&c";
	private int progressBarLength = 10;
	private String amountPending = " &7+{Amount}";
	
	public FurnaceDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Furnace.CheckingPeriod");
		gcPeriod = InteractionVisualizerAPI.getGCPeriod();
		progressBarCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Furnace.Options.ProgressBarCharacter"));
		emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Furnace.Options.EmptyColor"));
		filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Furnace.Options.FilledColor"));
		noFuelColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Furnace.Options.NoFuelColor"));
		progressBarLength = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Furnace.Options.ProgressBarLength");
		amountPending = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Blocks.Furnace.Options.AmountPending"));
	}
	
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = furnaceMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) furnaceMap.size() / (double) gcPeriod);
			int delay = 1;
			while (itr.hasNext()) {
				count++;
				if (count > maxper) {
					count = 0;
					delay++;
				}
				Entry<Block, Map<String, Object>> entry = itr.next();
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
					Block block = entry.getKey();
					if (!isActive(block.getLocation())) {
						Map<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
						}
						if (map.get("Stand") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("Stand");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						furnaceMap.remove(block);
						return;
					}
					if (!isFurnace(block.getType())) {
						Map<String, Object> map = entry.getValue();
						if (map.get("Item") instanceof Item) {
							Item item = (Item) map.get("Item");
							PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
						}
						if (map.get("Stand") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("Stand");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						furnaceMap.remove(block);
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
				Set<Block> list = nearbyFurnace();
				for (Block block : list) {
					if (furnaceMap.get(block) == null && isActive(block.getLocation())) {
						if (isFurnace(block.getType())) {
							Map<String, Object> map = new HashMap<>();
							map.put("Item", "N/A");
							map.putAll(spawnArmorStands(block));
							furnaceMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = furnaceMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) furnaceMap.size() / (double) checkingPeriod);
			int delay = 1;
			while (itr.hasNext()) {
				Entry<Block, Map<String, Object>> entry = itr.next();
				
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
					if (!isFurnace(block.getType())) {
						return;
					}					
					org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) block.getState();
					
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						Inventory inv = furnace.getInventory();
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
								item = new Item(furnace.getLocation().clone().add(0.5, 1.0, 0.5));
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
						if (hasItemToCook(furnace)) {
							int time = furnace.getCookTime();
							int max = 10 * 20;
							if (!InteractionVisualizer.version.isLegacy() && !InteractionVisualizer.version.equals(MCVersion.V1_13) && !InteractionVisualizer.version.equals(MCVersion.V1_13_1)) {
								max = furnace.getCookTimeTotal();
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
							if (hasFuel(furnace)) {
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
	public void onFurnace(InventoryClickEvent event) {
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
		if (!isFurnace(event.getView().getTopInventory().getLocation().getBlock().getType())) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!furnaceMap.containsKey(block)) {
			return;
		}
		
		Map<String, Object> map = furnaceMap.get(block);
		
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
	public void onUseFurnace(InventoryClickEvent event) {
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
		if (!isFurnace(event.getView().getTopInventory().getLocation().getBlock().getType())) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 2) {
			PacketManager.sendHandMovement(InteractionVisualizerAPI.getPlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragFurnace(InventoryDragEvent event) {
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
		if (!isFurnace(event.getView().getTopInventory().getLocation().getBlock().getType())) {
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
	public void onBreakFurnace(TileEntityRemovedEvent event) {
		Block block = event.getBlock();
		if (!furnaceMap.containsKey(block)) {
			return;
		}
		Map<String, Object> map = furnaceMap.get(block);
		if (map.get("Item") instanceof Item) {
			Item item = (Item) map.get("Item");
			PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
		}
		if (map.get("Stand") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("Stand");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		furnaceMap.remove(block);
	}
	
	public boolean hasItemToCook(org.bukkit.block.Furnace furnace) {
		Inventory inv = furnace.getInventory();
		if (inv.getItem(0) != null) {
			if (!inv.getItem(0).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasFuel(org.bukkit.block.Furnace furnace) {
		if (furnace.getBurnTime() > 0) {
			return true;
		}
		Inventory inv = furnace.getInventory();
		if (inv.getItem(1) != null) {
			if (!inv.getItem(1).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<Block> nearbyFurnace() {
		return TileEntityManager.getTileEntites(TileEntityType.FURNACE);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<>();
		Location origin = block.getLocation();	
	
		BlockFace facing = null;
		if (!InteractionVisualizer.version.isLegacy()) {
			BlockData blockData = block.getState().getBlockData();
			facing = ((Directional) blockData).getFacing();	
		} else {
			facing = LegacyFacingUtils.getFacing(block);
		}
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
		stand.setSilent(true);
		stand.setVisible(false);
		stand.setCustomName("");
		stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
	}
	
	public boolean isFurnace(String material) {
		if (material.toUpperCase().equals("FURNACE")) {
			return true;
		}
		if (material.toUpperCase().equals("BURNING_FURNACE")) {
			return true;
		}
		return false;
	}
	
	public boolean isFurnace(Material material) {
		return isFurnace(material.toString());
	}

}
