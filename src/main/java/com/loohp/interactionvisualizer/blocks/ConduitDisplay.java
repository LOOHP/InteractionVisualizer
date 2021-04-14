package com.loohp.interactionvisualizer.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.EulerAngle;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.api.events.TileEntityRemovedEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.DynamicVisualizerEntity.PathType;
import com.loohp.interactionvisualizer.entityholders.SurroundingPlaneArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;

import net.md_5.bungee.api.ChatColor;

public class ConduitDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, Map<String, Object>> conduitMap = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Block, float[]> placemap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	
	public ConduitDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.Conduit.CheckingPeriod");
		gcPeriod = InteractionVisualizerAPI.getGCPeriod();
	}
		
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = conduitMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) conduitMap.size() / (double) gcPeriod);
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
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("2") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("2");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						conduitMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.CONDUIT)) {
						Map<String, Object> map = entry.getValue();
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("2") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("2");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						conduitMap.remove(block);
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
				Set<Block> list = nearbyConduit();
				for (Block block : list) {
					if (conduitMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.CONDUIT)) {
							HashMap<String, Object> map = new HashMap<>();
							map.put("Item", "N/A");
							map.putAll(spawnArmorStands(block));
							conduitMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = conduitMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) conduitMap.size() / (double) checkingPeriod);
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
					if (!block.getType().equals(Material.CONDUIT)) {
						return;
					}
					
					int amount = getFrameAmount(block);
					
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						String arrow = "\u27f9";
						String square = "\u2b1b";
						ArmorStand line1 = (ArmorStand) entry.getValue().get("1");
						ArmorStand line2 = (ArmorStand) entry.getValue().get("2");
						
						int range = getRange(amount);
						ChatColor color = range > 0 ? ChatColor.AQUA : ChatColor.YELLOW;
							
						String one = color + square + amount + " " + arrow + " " + range + "m";
						if (!line1.getCustomName().toPlainText().equals(one) || !line1.isCustomNameVisible()) {
							line1.setCustomName(one);
							line1.setCustomNameVisible(true);
							PacketManager.updateArmorStandOnlyMeta(line1);
						}
						if (range < 96) {
							if (!line2.getCustomName().toPlainText().equals("") || line2.isCustomNameVisible()) {
								line2.setCustomName("");
								line2.setCustomNameVisible(false);
								PacketManager.updateArmorStandOnlyMeta(line2);
							}
						} else {
							String damage = ChatColor.AQUA + "4(" + ChatColor.RED + "\u2665\u2665" + ChatColor.AQUA + ") / 2s";
							if (!line2.getCustomName().toPlainText().equals(damage) || !line2.isCustomNameVisible()) {
								line2.setCustomName(damage);
								line2.setCustomNameVisible(true);
								PacketManager.updateArmorStandOnlyMeta(line2);
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlaceConduit(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlockPlaced();
		if (conduitMap.containsKey(block)) {
			return;
		}

		if (!block.getType().equals(Material.CONDUIT)) {
			return;
		}
		
		placemap.put(block, new float[]{event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch()});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakConduit(TileEntityRemovedEvent event) {
		Block block = event.getBlock();
		if (!conduitMap.containsKey(block)) {
			return;
		}

		Map<String, Object> map = conduitMap.get(block);
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("2") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("2");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		conduitMap.remove(block);
	}
	
	public Set<Block> nearbyConduit() {
		return TileEntityManager.getTileEntites(TileEntityType.CONDUIT);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<>();
		Location origin = block.getLocation().add(0.5, 0.001, 0.5);
		
		SurroundingPlaneArmorStand line1 = new SurroundingPlaneArmorStand(origin.clone().add(0.0, 0.28, 0.0), 0.4, PathType.CIRCLE);
		setStand(line1);
		SurroundingPlaneArmorStand line2 = new SurroundingPlaneArmorStand(origin.clone(), 0.4, PathType.CIRCLE);
		setStand(line2);
		
		map.put("1", line1);
		map.put("2", line2);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line1);
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line2);
		
		return map;
	}
	
	public void setStand(ArmorStand stand) {
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
	
	public int getFrameAmount(Block block) {
		List<Block> blocks = new ArrayList<>();
		Block up = block.getRelative(BlockFace.UP, 2);
		Block down = block.getRelative(BlockFace.DOWN, 2);
		Block north = block.getRelative(BlockFace.NORTH, 2);
		Block east = block.getRelative(BlockFace.EAST, 2);
		Block south = block.getRelative(BlockFace.SOUTH, 2);
		Block west = block.getRelative(BlockFace.WEST, 2);
		blocks.add(up);
		blocks.add(up.getRelative(BlockFace.NORTH, 1));
		blocks.add(up.getRelative(BlockFace.EAST, 1));
		blocks.add(up.getRelative(BlockFace.SOUTH, 1));
		blocks.add(up.getRelative(BlockFace.WEST, 1));
		blocks.add(down);
		blocks.add(down.getRelative(BlockFace.NORTH, 1));
		blocks.add(down.getRelative(BlockFace.EAST, 1));
		blocks.add(down.getRelative(BlockFace.SOUTH, 1));
		blocks.add(down.getRelative(BlockFace.WEST, 1));
		blocks.add(north);
		blocks.add(north.getRelative(BlockFace.UP, 1));
		blocks.add(north.getRelative(BlockFace.UP, 2));
		blocks.add(north.getRelative(BlockFace.DOWN, 1));
		blocks.add(north.getRelative(BlockFace.DOWN, 2));
		blocks.add(north.getRelative(BlockFace.EAST, 1));
		blocks.add(north.getRelative(BlockFace.WEST, 1));
		blocks.add(south);
		blocks.add(south.getRelative(BlockFace.UP, 1));
		blocks.add(south.getRelative(BlockFace.UP, 2));
		blocks.add(south.getRelative(BlockFace.DOWN, 1));
		blocks.add(south.getRelative(BlockFace.DOWN, 2));
		blocks.add(south.getRelative(BlockFace.EAST, 1));
		blocks.add(south.getRelative(BlockFace.WEST, 1));
		blocks.add(east);
		blocks.add(east.getRelative(BlockFace.UP, 1));
		blocks.add(east.getRelative(BlockFace.UP, 2));
		blocks.add(east.getRelative(BlockFace.DOWN, 1));
		blocks.add(east.getRelative(BlockFace.DOWN, 2));
		blocks.add(east.getRelative(BlockFace.NORTH, 1));
		blocks.add(east.getRelative(BlockFace.NORTH, 2));
		blocks.add(east.getRelative(BlockFace.SOUTH, 1));
		blocks.add(east.getRelative(BlockFace.SOUTH, 2));
		blocks.add(west);
		blocks.add(west.getRelative(BlockFace.UP, 1));
		blocks.add(west.getRelative(BlockFace.UP, 2));
		blocks.add(west.getRelative(BlockFace.DOWN, 1));
		blocks.add(west.getRelative(BlockFace.DOWN, 2));
		blocks.add(west.getRelative(BlockFace.NORTH, 1));
		blocks.add(west.getRelative(BlockFace.NORTH, 2));
		blocks.add(west.getRelative(BlockFace.SOUTH, 1));
		blocks.add(west.getRelative(BlockFace.SOUTH, 2));
		
		int amount = 0;
		for (Block frame : blocks) {
			Material type = frame.getType();
			if (type.equals(Material.PRISMARINE) || type.equals(Material.DARK_PRISMARINE) || type.equals(Material.PRISMARINE_BRICKS) || type.equals(Material.SEA_LANTERN)) {
				amount++;
			}
		}
		
		return amount;
	}
	
	public int getRange(int amount) {
		if (amount < 16) {
			return 0;
		}
		return amount / 7 * 16;
	}

}
