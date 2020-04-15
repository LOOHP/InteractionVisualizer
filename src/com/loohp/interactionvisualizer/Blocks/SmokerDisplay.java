package com.loohp.interactionvisualizer.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolder.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolder.Item;
import com.loohp.interactionvisualizer.Utils.InventoryUtils;
import com.loohp.interactionvisualizer.Utils.PacketSending;
import com.loohp.interactionvisualizer.Utils.VanishUtils;

public class SmokerDisplay implements Listener {
	
	public static ConcurrentHashMap<Block, HashMap<String, Object>> smokerMap = new ConcurrentHashMap<Block, HashMap<String, Object>>();
	
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
		
		if (event.getView().getTopInventory() == null) {
			return;
		}
		try {
			if (event.getView().getTopInventory().getLocation() == null) {
				return;
			}
		} catch (Exception e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.SMOKER)) {
			return;
		}
		
		Block block = event.getView().getTopInventory().getLocation().getBlock();
		
		if (!smokerMap.containsKey(block)) {
			return;
		}
		
		HashMap<String, Object> map = smokerMap.get(block);
		
		ItemStack itemstack = event.getCurrentItem();
		Location loc = block.getLocation();
		
		Player player = (Player) event.getWhoClicked();
		if (map.get("Item") instanceof String) {
			map.put("Item", new Item(block.getLocation().clone().add(0.5, 1.2, 0.5)));
		}
		Item item = (Item) map.get("Item");
		
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
			}
		}.runTaskLater(InteractionVisualizer.plugin, 8);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseSmoker(InventoryClickEvent event) {
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
		} catch (Exception e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.SMOKER)) {
			return;
		}
		
		if (event.getRawSlot() >= 0 && event.getRawSlot() <= 2) {
			PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onDragSmoker(InventoryDragEvent event) {
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
		} catch (Exception e) {
			return;
		}
		if (event.getView().getTopInventory().getLocation().getBlock() == null) {
			return;
		}
		if (!event.getView().getTopInventory().getLocation().getBlock().getType().equals(Material.SMOKER)) {
			return;
		}
		
		for (int slot : event.getRawSlots()) {
			if (slot >= 0 && slot <= 2) {
				PacketSending.sendHandMovement(InteractionVisualizer.getOnlinePlayers(), (Player) event.getWhoClicked());
				break;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakSmoker(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!smokerMap.containsKey(block)) {
			return;
		}

		HashMap<String, Object> map = smokerMap.get(block);
		if (map.get("Item") instanceof Item) {
			Item item = (Item) map.get("Item");
			PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
		}
		if (map.get("Stand") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("Stand");
			PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
		}
		smokerMap.remove(block);
	}
	
	public static int gc() {
		return new BukkitRunnable() {
			public void run() {
				Iterator<Entry<Block, HashMap<String, Object>>> itr = smokerMap.entrySet().iterator();
				int count = 0;
				int maxper = (int) Math.ceil((double) smokerMap.size() / (double) 600);
				int delay = 1;
				while (itr.hasNext()) {
					count++;
					if (count > maxper) {
						count = 0;
						delay++;
					}
					Entry<Block, HashMap<String, Object>> entry = itr.next();
					new BukkitRunnable() {
						public void run() {
							Block block = entry.getKey();
							if (!block.getType().equals(Material.SMOKER)) {
								HashMap<String, Object> map = entry.getValue();
								if (map.get("Item") instanceof Item) {
									Item item = (Item) map.get("Item");
									PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
									item.remove();
								}
								if (map.get("Stand") instanceof ArmorStand) {
									ArmorStand stand = (ArmorStand) map.get("Stand");
									PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
									stand.remove();
								}
								smokerMap.remove(block);
								return;
							}
							boolean active = false;
							if (isActive(block.getLocation())) {
								active = true;
								return;
							}
							if (active == false) {
								HashMap<String, Object> map = entry.getValue();
								if (map.get("Item") instanceof Item) {
									Item item = (Item) map.get("Item");
									PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
									item.remove();
								}
								if (map.get("Stand") instanceof ArmorStand) {
									ArmorStand stand = (ArmorStand) map.get("Stand");
									PacketSending.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
									stand.remove();
								}
								smokerMap.remove(block);
								return;
							}
						}
					}.runTaskLater(InteractionVisualizer.plugin, delay);
				}
			}
		}.runTaskTimerAsynchronously(InteractionVisualizer.plugin, 0, 600).getTaskId();
	}
	
	public static int run() {		
		return new BukkitRunnable() {
			public void run() {
				new BukkitRunnable() {
					public void run() {
						for (Player player : InteractionVisualizer.getOnlinePlayers()) {
							List<Block> list = nearbySmoker(player.getLocation());
							for (Block block : list) {
								if (!smokerMap.containsKey(block)) {
									HashMap<String, Object> map = new HashMap<String, Object>();
									map.put("Item", "N/A");
									map.putAll(spawnArmorStands(block));
									smokerMap.put(block, map);
								}
							}
						}
					}
				}.runTask(InteractionVisualizer.plugin);
				
				Iterator<Entry<Block, HashMap<String, Object>>> itr = smokerMap.entrySet().iterator();
				int count = 0;
				int maxper = (int) Math.ceil((double) smokerMap.size() / (double) 20);
				int delay = 1;
				while (itr.hasNext()) {
					Entry<Block, HashMap<String, Object>> entry = itr.next();
					
					count++;
					if (count > maxper) {
						count = 0;
						delay++;
					}
					new BukkitRunnable() {
						public void run() {
							Block block = entry.getKey();
							if (!block.getType().equals(Material.SMOKER)) {
								return;
							}
							org.bukkit.block.Smoker smoker = (org.bukkit.block.Smoker) block.getState();
							
							Inventory inv = smoker.getInventory();
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
									item = new Item(block.getLocation().clone().add(0.5, 1.0, 0.5));
									item.setItemStack(itemstack);
									item.setVelocity(new Vector(0, 0, 0));
									item.setPickupDelay(32767);
									item.setGravity(false);
									entry.getValue().put("Item", item);
									PacketSending.sendItemSpawn(InteractionVisualizer.itemDrop, item);
									PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
								} else {
									entry.getValue().put("Item", "N/A");
								}
							} else {
								item = (Item) entry.getValue().get("Item");
								if (itemstack != null) {
									if (!item.getItemStack().equals(itemstack)) {
										item.setItemStack(itemstack);
										PacketSending.updateItem(InteractionVisualizer.getOnlinePlayers(), item);
									}
									item.setPickupDelay(32767);
									item.setGravity(false);
								} else {
									entry.getValue().put("Item", "N/A");
									PacketSending.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
									item.remove();
								}
							}
		
							if (hasItemToCook(smoker)) {
								ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
								if (hasFuel(smoker)) {
									int time = smoker.getCookTime();
									int max = smoker.getCookTimeTotal();
									String symbol = "";
									double percentagescaled = (double) time / (double) max * 10.0;
									double i = 1;
									for (i = 1; i < percentagescaled; i = i + 1) {
										symbol = symbol + "§e\u258e";
									}
									i = i - 1;
									if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.33) {
										symbol = symbol + "§7\u258e";
									} else if ((percentagescaled - i) > 0 && (percentagescaled - i) < 0.67) {
										symbol = symbol + "§7\u258e";
									} else if ((percentagescaled - i) > 0) {
										symbol = symbol + "§e\u258e";
									}
									for (i = 10 - 1; i >= percentagescaled; i = i - 1) {
										symbol = symbol + "§7\u258e";
									}
									
									int left = inv.getItem(0).getAmount() - 1;
									if (left > 0) {
										symbol = symbol + " §7+" + left;
									}
									stand.setCustomNameVisible(true);
									stand.setCustomName(symbol);
									PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
								} else {
									stand.setCustomNameVisible(false);
									stand.setCustomName("");
									PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
								}
							} else {					
								ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
								stand.setCustomNameVisible(false);
								stand.setCustomName("");
								PacketSending.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
							}
						}
					}.runTaskLater(InteractionVisualizer.plugin, delay);
				}
			}
		}.runTaskTimerAsynchronously(InteractionVisualizer.plugin, 0, 20).getTaskId();		
	}
	
	public static boolean hasItemToCook(org.bukkit.block.Smoker smoker) {
		Inventory inv = smoker.getInventory();
		if (inv.getItem(0) != null) {
			if (!inv.getItem(0).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasFuel(org.bukkit.block.Smoker smoker) {
		if (smoker.getBurnTime() > 0) {
			return true;
		}
		Inventory inv = smoker.getInventory();
		if (inv.getItem(1) != null) {
			if (!inv.getItem(1).getType().equals(Material.AIR)) {
				return true;
			}
		}
		return false;
	}
	
	public static List<Block> nearbySmoker(Location loc) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		List<Block> blocks = new ArrayList<Block>();
		
		World world = loc.getWorld();
		int chunkX = loc.getChunk().getX();
		int chunkZ = loc.getChunk().getZ();
		
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ));
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ - 1));
		chunks.add(world.getChunkAt(chunkX, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX, chunkZ));
		chunks.add(world.getChunkAt(chunkX, chunkZ - 1));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ - 1));
		
		for (Chunk chunk : chunks) {
			for (BlockState state : chunk.getTileEntities()) {
				if (state.getBlock().getType().equals(Material.SMOKER)) {
					blocks.add(state.getBlock());
				}
			}
		}
		return blocks;
	}
	
	public static boolean isActive(Location loc) {
		List<Chunk> chunks = new ArrayList<Chunk>();
		
		World world = loc.getWorld();
		int chunkX = loc.getChunk().getX();
		int chunkZ = loc.getChunk().getZ();
		
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ));
		chunks.add(world.getChunkAt(chunkX + 1, chunkZ - 1));
		chunks.add(world.getChunkAt(chunkX, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX, chunkZ));
		chunks.add(world.getChunkAt(chunkX, chunkZ - 1));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ + 1));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ));
		chunks.add(world.getChunkAt(chunkX - 1, chunkZ - 1));
		
		for (Chunk chunk : chunks) {
			for (Entity entity : chunk.getEntities()) {
				if (entity instanceof Player) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static HashMap<String, ArmorStand> spawnArmorStands(Block block) {
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
		
		PacketSending.sendArmorStandSpawn(InteractionVisualizer.holograms, slot1);
		
		return map;
	}
	
	public static void setStand(ArmorStand stand) {
		stand.setBasePlate(false);
		stand.setMarker(true);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setSilent(true);
		stand.setInvulnerable(true);
		stand.setVisible(false);
		stand.setCustomName("");
		stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
	}

}
