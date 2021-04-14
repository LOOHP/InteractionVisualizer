package com.loohp.interactionvisualizer.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.EulerAngle;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.DynamicVisualizerEntity.PathType;
import com.loohp.interactionvisualizer.entityholders.SurroundingPlaneArmorStand;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatComponentUtils;
import com.loohp.interactionvisualizer.utils.RomanNumberUtils;
import com.loohp.interactionvisualizer.utils.TranslationUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class BeaconDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, Map<String, Object>> beaconMap = new ConcurrentHashMap<>();
	public ConcurrentHashMap<Block, float[]> placemap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	
	public BeaconDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfig().getInt("Blocks.Beacon.CheckingPeriod");
		gcPeriod = InteractionVisualizer.plugin.getConfig().getInt("GarbageCollector.Period");
	}
		
	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = beaconMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beaconMap.size() / (double) gcPeriod);
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
						if (map.get("3") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("3");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beaconMap.remove(block);
						return;
					}
					if (!block.getType().equals(Material.BEACON)) {
						Map<String, Object> map = entry.getValue();
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("2") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("2");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						if (map.get("3") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("3");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						beaconMap.remove(block);
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
				List<Block> list = nearbyBeacon();
				for (Block block : list) {
					if (beaconMap.get(block) == null && isActive(block.getLocation())) {
						if (block.getType().equals(Material.BEACON)) {
							HashMap<String, Object> map = new HashMap<>();
							map.put("Item", "N/A");
							map.putAll(spawnArmorStands(block));
							beaconMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = beaconMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) beaconMap.size() / (double) checkingPeriod);
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
					if (!block.getType().equals(Material.BEACON)) {
						return;
					}
					org.bukkit.block.Beacon beacon = (org.bukkit.block.Beacon) block.getState();
					Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
						String arrow = "\u27f9";
						String up = "\u25b2";
						ChatColor color = getBeaconColor(block);
						ArmorStand line1 = (ArmorStand) entry.getValue().get("1");
						ArmorStand line2 = (ArmorStand) entry.getValue().get("2");
						ArmorStand line3 = (ArmorStand) entry.getValue().get("3");
							
						String one = color + up + beacon.getTier() + " " + arrow + " " + getRange(beacon.getTier()) + "m";
						if (beacon.getTier() == 0) {
							if (!line1.getCustomName().toPlainText().equals("") || line1.isCustomNameVisible()) {
								line1.setCustomName("");
								line1.setCustomNameVisible(false);
								PacketManager.updateArmorStandOnlyMeta(line1);
							}
							if (!line2.getCustomName().toPlainText().equals(one) || !line2.isCustomNameVisible()) {
								line2.setCustomName(one);
								line2.setCustomNameVisible(true);
								PacketManager.updateArmorStandOnlyMeta(line2);
							}
							if (!line3.getCustomName().toPlainText().equals("") || line3.isCustomNameVisible()) {
								line3.setCustomName("");
								line3.setCustomNameVisible(false);
								PacketManager.updateArmorStandOnlyMeta(line3);
							}
						} else {
							BaseComponent primaryEffectText = null;
							BaseComponent secondaryEffectText = null;
							if (beacon.getPrimaryEffect() != null) {
								TranslatableComponent effectTrans = new TranslatableComponent(TranslationUtils.getEffect(beacon.getPrimaryEffect().getType()));
								effectTrans.setColor(color);
								TextComponent levelText = new TextComponent(" " + color + RomanNumberUtils.toRoman(beacon.getPrimaryEffect().getAmplifier() + 1));
								effectTrans.addExtra(levelText);
								primaryEffectText = effectTrans;
							}
							if (beacon.getSecondaryEffect() != null) {
								TranslatableComponent effectTrans = new TranslatableComponent(TranslationUtils.getEffect(beacon.getSecondaryEffect().getType()));
								effectTrans.setColor(color);
								TextComponent levelText = new TextComponent(" " + color + RomanNumberUtils.toRoman(beacon.getSecondaryEffect().getAmplifier() + 1));
								effectTrans.addExtra(levelText);
								secondaryEffectText = effectTrans;
							}
							if (secondaryEffectText == null) {
								if (!line1.getCustomName().toPlainText().equals("") || line1.isCustomNameVisible()) {
									line1.setCustomName("");
									line1.setCustomNameVisible(false);
									PacketManager.updateArmorStandOnlyMeta(line1);
								}
								if (!line2.getCustomName().toPlainText().equals(one) || !line2.isCustomNameVisible()) {
									line2.setCustomName(one);
									line2.setCustomNameVisible(true);
									PacketManager.updateArmorStandOnlyMeta(line2);
								}
								if (primaryEffectText == null) {
									if (!line3.getCustomName().toPlainText().equals("") || line3.isCustomNameVisible()) {
										line3.setCustomName("");
										line3.setCustomNameVisible(false);
										PacketManager.updateArmorStandOnlyMeta(line3);
									}
								} else {
									if (!ChatComponentUtils.areSimilar(line3.getCustomName(), primaryEffectText, true) || !line3.isCustomNameVisible()) {
										line3.setCustomName(primaryEffectText);
										line3.setCustomNameVisible(true);
										PacketManager.updateArmorStandOnlyMeta(line3);
									}
								}
							} else {
								if (!line1.getCustomName().toPlainText().equals(one) || !line1.isCustomNameVisible()) {
									line1.setCustomName(one);
									line1.setCustomNameVisible(true);
									PacketManager.updateArmorStandOnlyMeta(line1);
								}
								if (primaryEffectText == null) {
									if (!line2.getCustomName().toPlainText().equals("") || line2.isCustomNameVisible()) {
										line2.setCustomName("");
										line2.setCustomNameVisible(false);
										PacketManager.updateArmorStandOnlyMeta(line2);
									}
								} else {
									if (!ChatComponentUtils.areSimilar(line2.getCustomName(), primaryEffectText, true) || !line2.isCustomNameVisible()) {
										line2.setCustomName(primaryEffectText);
										line2.setCustomNameVisible(true);
										PacketManager.updateArmorStandOnlyMeta(line2);
									}
								}
								if (!ChatComponentUtils.areSimilar(line3.getCustomName(), secondaryEffectText, true) || !line3.isCustomNameVisible()) {
									line3.setCustomName(secondaryEffectText);
									line3.setCustomNameVisible(true);
									PacketManager.updateArmorStandOnlyMeta(line3);
								}
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlaceBeacon(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlockPlaced();
		if (beaconMap.containsKey(block)) {
			return;
		}

		if (!block.getType().equals(Material.BEACON)) {
			return;
		}
		
		placemap.put(block, new float[]{event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch()});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakBeacon(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!beaconMap.containsKey(block)) {
			return;
		}

		Map<String, Object> map = beaconMap.get(block);
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("2") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("2");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		if (map.get("3") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("3");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		beaconMap.remove(block);
	}
	
	public List<Block> nearbyBeacon() {
		return TileEntityManager.getTileEntites(TileEntityType.BEACON);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<>();
		Location origin = block.getLocation().add(0.5, 0.25, 0.5);
		
		SurroundingPlaneArmorStand line1 = new SurroundingPlaneArmorStand(origin.clone().add(0.0, 0.25, 0.0), 0.7, PathType.SQUARE);
		setStand(line1);
		SurroundingPlaneArmorStand line2 = new SurroundingPlaneArmorStand(origin.clone(), 0.7, PathType.SQUARE);
		setStand(line2);
		SurroundingPlaneArmorStand line3 = new SurroundingPlaneArmorStand(origin.clone().add(0.0, -0.25, 0.0), 0.7, PathType.SQUARE);
		setStand(line3);
		
		map.put("1", line1);
		map.put("2", line2);
		map.put("3", line3);
		
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line1);
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line2);
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), line3);
		
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
	
	public ChatColor getBeaconColor(Block block) {
		Block glass = block.getRelative(BlockFace.UP);
		if (!InteractionVisualizer.version.isLegacy()) {
			switch (glass.getType()) {
			case ORANGE_STAINED_GLASS:
			case ORANGE_STAINED_GLASS_PANE:
				return ChatColor.GOLD;
			case MAGENTA_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS_PANE:
				return ChatColor.LIGHT_PURPLE;
			case LIGHT_BLUE_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS_PANE:
				return ChatColor.AQUA;
			case YELLOW_STAINED_GLASS:
			case YELLOW_STAINED_GLASS_PANE:
				return ChatColor.YELLOW;
			case LIME_STAINED_GLASS:
			case LIME_STAINED_GLASS_PANE:
				return ChatColor.GREEN;
			case PINK_STAINED_GLASS:
			case PINK_STAINED_GLASS_PANE:
				return ChatColor.LIGHT_PURPLE;
			case GRAY_STAINED_GLASS:
			case GRAY_STAINED_GLASS_PANE:
				return ChatColor.DARK_GRAY;
			case LIGHT_GRAY_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
				return ChatColor.GRAY;
			case CYAN_STAINED_GLASS:
			case CYAN_STAINED_GLASS_PANE:
				return ChatColor.DARK_AQUA;
			case PURPLE_STAINED_GLASS:
			case PURPLE_STAINED_GLASS_PANE:
				return ChatColor.DARK_PURPLE;
			case BLUE_STAINED_GLASS:
			case BLUE_STAINED_GLASS_PANE:
				return ChatColor.BLUE;
			case BROWN_STAINED_GLASS:
			case BROWN_STAINED_GLASS_PANE:
				return ChatColor.GOLD;
			case GREEN_STAINED_GLASS:
			case GREEN_STAINED_GLASS_PANE:
				return ChatColor.DARK_GREEN;
			case RED_STAINED_GLASS:
			case RED_STAINED_GLASS_PANE:
				return ChatColor.RED;
			case BLACK_STAINED_GLASS:
			case BLACK_STAINED_GLASS_PANE:
			case GLASS:
			case GLASS_PANE:
			case WHITE_STAINED_GLASS:
			case WHITE_STAINED_GLASS_PANE:
			default:
				return ChatColor.WHITE;
			}
		} else {
			if (!glass.getType().name().toUpperCase().contains("GLASS")) {
				return ChatColor.WHITE;
			}
			@SuppressWarnings("deprecation")
			DyeColor color = DyeColor.getByWoolData(glass.getData());
			switch (color.toString().toUpperCase()) {
			case "BLACK":
				return ChatColor.WHITE;
			case "BLUE":
				return ChatColor.BLUE;
			case "BROWN":
				return ChatColor.GOLD;
			case "CYAN":
				return ChatColor.DARK_AQUA;
			case "GRAY":
				return ChatColor.DARK_GRAY;
			case "GREEN":
				return ChatColor.DARK_GREEN;
			case "LIGHT_BLUE":
				return ChatColor.AQUA;
			case "SILVER":
			case "LIGHT_GRAY":
				return ChatColor.GRAY;
			case "LIME":
				return ChatColor.GREEN;
			case "MAGENTA":
				return ChatColor.LIGHT_PURPLE;
			case "ORANGE":
				return ChatColor.GOLD;
			case "PINK":
				return ChatColor.LIGHT_PURPLE;
			case "PURPLE":
				return ChatColor.DARK_PURPLE;
			case "RED":
				return ChatColor.RED;
			case "WHITE":
				return ChatColor.WHITE;
			case "YELLOW":
				return ChatColor.YELLOW;
			default:
				return ChatColor.WHITE;			
			}
		}
	}
	
	public int getRange(int tier) {
		switch (tier) {
		case 0:
			return 0;
		case 1:
			return 20;
		case 2:
			return 30;
		case 3:
			return 40;
		case 4:
			return 50;
		default:
			return 0;
		}
	}

}
