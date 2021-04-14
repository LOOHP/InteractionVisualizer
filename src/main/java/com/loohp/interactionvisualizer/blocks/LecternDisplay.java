package com.loohp.interactionvisualizer.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.api.events.TileEntityRemovedEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import com.loohp.interactionvisualizer.utils.MCVersion;
import com.loohp.interactionvisualizer.utils.NBTUtils;

public class LecternDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, Map<String, Object>> lecternMap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private String format1 = "";
	private String format2 = "";
	
	public LecternDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.Lectern.CheckingPeriod");
		gcPeriod = InteractionVisualizerAPI.getGCPeriod();
		format1 = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.Lectern.Options.Line1"));
		format2 = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfig().getString("Blocks.Lectern.Options.Line2"));
	}
	
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = lecternMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) lecternMap.size() / (double) gcPeriod);
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
						lecternMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.LECTERN)) {
						Map<String, Object> map = entry.getValue();
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("2") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("2");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						lecternMap.remove(block);
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
				Set<Block> list = nearbyLectern();
				for (Block block : list) {
					if (lecternMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.LECTERN)) {
							HashMap<String, Object> map = new HashMap<>();
							map.putAll(spawnArmorStands(block));
							lecternMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = lecternMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) lecternMap.size() / (double) checkingPeriod);
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
					if (!block.getType().equals(Material.LECTERN)) {
						return;
					}
					org.bukkit.block.Lectern lectern = (org.bukkit.block.Lectern) block.getState();
					
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						Inventory inv = lectern.getInventory();
						ItemStack itemstack = inv.getItem(0);
						if (itemstack != null) {
							if (itemstack.getType().equals(Material.AIR)) {
								itemstack = null;
							}
						}
						
						ArmorStand stand1 = (ArmorStand) entry.getValue().get("1");
						ArmorStand stand2 = (ArmorStand) entry.getValue().get("2");
						if (itemstack != null && itemstack.getType().equals(Material.WRITTEN_BOOK) && itemstack.hasItemMeta() && itemstack.getItemMeta() != null && itemstack.getItemMeta() instanceof BookMeta) {							
							BookMeta meta = (BookMeta) itemstack.getItemMeta();							
							
							String line1;
							String line2;
							
							if (InteractionVisualizer.version.isNewerOrEqualTo(MCVersion.V1_15)) {
								line1 = format1
										.replace("{Title}", meta.getTitle() == null ? "" : NBTUtils.getString(itemstack, "title"))
										.replace("{Author}", meta.getAuthor() == null ? "" : NBTUtils.getString(itemstack, "author"))
										.replace("{Page}", lectern.getPage() + "");
								line2 = format2
										.replace("{Title}", meta.getTitle() == null ? "" : meta.getTitle())
										.replace("{Author}", meta.getAuthor() == null ? "" : meta.getAuthor())
										.replace("{Page}", lectern.getPage() + "");
							} else {
								line1 = format1
										.replace("{Title}", meta.getTitle() == null ? "" : meta.getTitle())
										.replace("{Author}", meta.getAuthor() == null ? "" : meta.getAuthor())
										.replace("{Page}", lectern.getPage() + "");
								line2 = format2
										.replace("{Title}", meta.getTitle() == null ? "" : meta.getTitle())
										.replace("{Author}", meta.getAuthor() == null ? "" : meta.getAuthor())
										.replace("{Page}", lectern.getPage() + "");
							}
							
							if (!stand1.getCustomName().toPlainText().equals(line1) || !stand1.isCustomNameVisible()) {
								stand1.setCustomNameVisible(true);
								stand1.setCustomName(line1);
								PacketManager.updateArmorStandOnlyMeta(stand1);
							}
							if (!stand2.getCustomName().toPlainText().equals(line2) || !stand2.isCustomNameVisible()) {
								stand2.setCustomNameVisible(true);
								stand2.setCustomName(line2);
								PacketManager.updateArmorStandOnlyMeta(stand2);
							}
						} else {					
							if (!stand1.getCustomName().toPlainText().equals("") || stand1.isCustomNameVisible()) {
								stand1.setCustomNameVisible(false);
								stand1.setCustomName("");
								PacketManager.updateArmorStandOnlyMeta(stand1);
							}
							if (!stand2.getCustomName().toPlainText().equals("") || stand2.isCustomNameVisible()) {
								stand2.setCustomNameVisible(false);
								stand2.setCustomName("");
								PacketManager.updateArmorStandOnlyMeta(stand2);
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakLectern(TileEntityRemovedEvent event) {
		Block block = event.getBlock();
		if (!lecternMap.containsKey(block)) {
			return;
		}

		Map<String, Object> map = lecternMap.get(block);
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("2") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("2");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		lecternMap.remove(block);
	}
	
	public Set<Block> nearbyLectern() {
		return TileEntityManager.getTileEntites(TileEntityType.LECTERN);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<>();
	
		Location origin = block.getLocation();
		BlockData blockData = block.getState().getBlockData();
		BlockFace facing = ((Directional) blockData).getFacing();			
		Location target = block.getRelative(facing).getLocation();
		Vector direction = target.toVector().subtract(origin.toVector()).multiply(0.2);
		
		Location loc = origin.clone().add(direction).add(0.5, 1.301, 0.5);
		ArmorStand slot1 = new ArmorStand(loc.clone());
		setStand(slot1);
		ArmorStand slot2 = new ArmorStand(loc.clone().add(0, -0.3, 0));
		setStand(slot2);
		
		map.put("1", slot1);
		map.put("2", slot2);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), slot1);
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), slot2);
		
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
