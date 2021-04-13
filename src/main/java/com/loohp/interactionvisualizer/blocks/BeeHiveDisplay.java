package com.loohp.interactionvisualizer.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;

public class BeeHiveDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, Map<String, Object>> beehiveMap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private String honeyLevelCharacter = "";
	private String emptyColor = "&7";
	private String filledColor = "&e";
	private String noCampfireColor = "&c";
	private String beeCountText = "&e{Current}&6/{Max}";
	
	public BeeHiveDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.BeeHive.CheckingPeriod");
		gcPeriod = InteractionVisualizer.plugin.getConfig().getInt("GarbageCollector.Period");
		honeyLevelCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeHive.Options.HoneyLevelCharacter"));
		emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeHive.Options.EmptyColor"));
		filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeHive.Options.FilledColor"));
		noCampfireColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeHive.Options.NoCampfireColor"));
		beeCountText = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeHive.Options.BeeCountText"));
	}
	
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = beehiveMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beehiveMap.size() / (double) gcPeriod);
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
						if (map.get("0") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("0");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beehiveMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.BEEHIVE)) {
						Map<String, Object> map = entry.getValue();
						if (map.get("0") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("0");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beehiveMap.remove(block);
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
				List<Block> list = nearbyBeehive();
				for (Block block : list) {
					if (beehiveMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.BEEHIVE)) {
							Map<String, Object> map = new HashMap<>();
							map.putAll(spawnArmorStands(block));
							beehiveMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = beehiveMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beehiveMap.size() / (double) checkingPeriod);
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
					updateBlock(block);
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBeeEnterBeehive(EntityEnterBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBeeLeaveBeehive(EntityChangeBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInteractBeehive(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getClickedBlock();
		if (block != null) {
			Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakBeehive(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!beehiveMap.containsKey(block)) {
			return;
		}

		Map<String, Object> map = beehiveMap.get(block);
		if (map.get("0") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("0");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		beehiveMap.remove(block);
	}
	
	public void updateBlock(Block block) {
		if (!isActive(block.getLocation())) {
			return;
		}
		if (!block.getType().equals(Material.BEEHIVE)) {
			return;
		}
		if (!beehiveMap.containsKey(block)) {
			return;
		}
		
		Map<String, Object> map = beehiveMap.get(block);
		
		org.bukkit.block.Beehive beehiveState = (org.bukkit.block.Beehive) block.getState();
		org.bukkit.block.data.type.Beehive beehiveData = (org.bukkit.block.data.type.Beehive) block.getBlockData();
		
		Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
			ArmorStand line0 = (ArmorStand) map.get("0");
			ArmorStand line1 = (ArmorStand) map.get("1");
			
			String str0 = "";
			for (int i = 0; i < beehiveData.getHoneyLevel(); i++) {
				str0 += (beehiveState.isSedated() ? filledColor : noCampfireColor) + honeyLevelCharacter;
			}
			for (int i = beehiveData.getHoneyLevel(); i < beehiveData.getMaximumHoneyLevel(); i++) {
				str0 += emptyColor + honeyLevelCharacter;
			}	
			String str1 = beeCountText.replace("{Current}", beehiveState.getEntityCount() + "").replace("{Max}", beehiveState.getMaxEntities() + "");
			
			if (!line0.getCustomName().toPlainText().equals(str0)) {
				line0.setCustomName(str0);
				line0.setCustomNameVisible(true);
				PacketManager.updateArmorStandOnlyMeta(line0);
			}
			if (!line1.getCustomName().toPlainText().equals(str1)) {
				line1.setCustomName(str1);
				line1.setCustomNameVisible(true);
				PacketManager.updateArmorStandOnlyMeta(line1);
			}
		});
	}
	
	public List<Block> nearbyBeehive() {
		return TileEntityManager.getTileEntites(TileEntityType.BEEHIVE);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<String, ArmorStand>();
		Location origin = block.getLocation();	
		
		BlockData blockData = block.getState().getBlockData();
		BlockFace facing = ((Directional) blockData).getFacing();			
		Location target = block.getRelative(facing).getLocation();
		Vector direction = target.toVector().subtract(origin.toVector()).multiply(0.7);
		
		Location loc0 = block.getLocation().clone().add(direction).add(0.5, 0.25, 0.5);
		ArmorStand line0 = new ArmorStand(loc0.clone());
		setStand(line0);
		
		Location loc1 = block.getLocation().clone().add(direction).add(0.5, 0, 0.5);
		ArmorStand line1 = new ArmorStand(loc1.clone());
		setStand(line1);
		
		map.put("0", line0);
		map.put("1", line1);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line0);
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line1);
		
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

}
