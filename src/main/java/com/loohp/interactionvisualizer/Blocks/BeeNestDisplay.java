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
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.API.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.API.Events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager.TileEntityType;
import com.loohp.interactionvisualizer.Utils.ChatColorUtils;

public class BeeNestDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, HashMap<String, Object>> beenestMap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private String honeyLevelCharacter = "";
	private String emptyColor = "&7";
	private String filledColor = "&e";
	private String noCampfireColor = "&c";
	private String beeCountText = "&e{Current}&6/{Max}";
	
	public BeeNestDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.BeeNest.CheckingPeriod");
		gcPeriod = InteractionVisualizer.plugin.getConfig().getInt("GarbageCollector.Period");
		honeyLevelCharacter = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeNest.Options.HoneyLevelCharacter"));
		emptyColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeNest.Options.EmptyColor"));
		filledColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeNest.Options.FilledColor"));
		noCampfireColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeNest.Options.NoCampfireColor"));
		beeCountText = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.BeeNest.Options.BeeCountText"));
	}
	
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, HashMap<String, Object>>> itr = beenestMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beenestMap.size() / (double) gcPeriod);
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
						if (map.get("0") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("0");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beenestMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.BEE_NEST)) {
						HashMap<String, Object> map = entry.getValue();
						if (map.get("0") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("0");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beenestMap.remove(block);
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
				List<Block> list = nearbyBeenest();
				for (Block block : list) {
					if (beenestMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.BEE_NEST)) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							map.putAll(spawnArmorStands(block));
							beenestMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, HashMap<String, Object>>> itr = beenestMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beenestMap.size() / (double) checkingPeriod);
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
					updateBlock(block);
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBeeEnterBeenest(EntityEnterBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBeeLeaveBeenest(EntityChangeBlockEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInteractBeenest(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getClickedBlock();
		if (block != null) {
			Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> updateBlock(block), 1);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakBeenest(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!beenestMap.containsKey(block)) {
			return;
		}

		HashMap<String, Object> map = beenestMap.get(block);
		if (map.get("0") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("0");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		beenestMap.remove(block);
	}
	
	public void updateBlock(Block block) {
		if (!isActive(block.getLocation())) {
			return;
		}
		if (!block.getType().equals(Material.BEE_NEST)) {
			return;
		}
		if (!beenestMap.containsKey(block)) {
			return;
		}

		HashMap<String, Object> map = beenestMap.get(block);
		
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
	
	public List<Block> nearbyBeenest() {
		return TileEntityManager.getTileEntites(TileEntityType.BEE_NEST);
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
