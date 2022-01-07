package com.loohp.interactionvisualizer.blocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Rotatable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import com.loohp.interactionvisualizer.utils.JsonUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BannerDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public static final EntryKey KEY = new EntryKey("banner");
	
	public Map<Block, Map<String, Object>> bannerMap = new ConcurrentHashMap<>();
	private int checkingPeriod = 20;
	private int gcPeriod = 600;
	private boolean stripColorBlacklist;
	private Predicate<String> blacklist;
	
	public BannerDisplay() {
		onReload(new InteractionVisualizerReloadEvent());
	}
	
	@EventHandler
	public void onReload(InteractionVisualizerReloadEvent event) {
		checkingPeriod = InteractionVisualizer.plugin.getConfiguration().getInt("Blocks.Banner.CheckingPeriod");
		gcPeriod = InteractionVisualizerAPI.getGCPeriod();
		stripColorBlacklist = InteractionVisualizer.plugin.getConfiguration().getBoolean("Entities.Item.Options.Blacklist.StripColorWhenMatching");
		blacklist = InteractionVisualizer.plugin.getConfiguration().getStringList("Blocks.Banner.Options.Blacklist.List").stream().map(each -> {
			Pattern pattern = Pattern.compile(each);
			Predicate<String> predicate = str -> pattern.matcher(str).matches();
			return predicate;
		}).reduce(Predicate::or).orElse(s -> false);
	}
	
	@Override
	public EntryKey key() {
		return KEY;
	}

	@Override
	public int gc() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, Map<String, Object>>> itr = bannerMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) bannerMap.size() / (double) gcPeriod);
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
						bannerMap.remove(block);
						return;
					}
					if (!isBanner(block.getType())) {
						Map<String, Object> map = entry.getValue();
						if (map.get("1") instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) map.get("1");
							PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
						}
						bannerMap.remove(block);
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
				Set<Block> list = nearbyBanner();
				for (Block block : list) {
					if (bannerMap.get(block) == null && isActive(block.getLocation())) {
						if (isBanner(block.getType())) {
							Map<String, Object> map = new HashMap<>();
							map.put("Item", "N/A");
							map.putAll(spawnArmorStands(block));
							bannerMap.put(block, map);
						}
					}
				}
			});
			
			Iterator<Entry<Block, Map<String, Object>>> itr = bannerMap.entrySet().iterator();
			int count = 0;
			int maxper = (int) Math.ceil((double) bannerMap.size() / (double) checkingPeriod);
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
					if (!isBanner(block.getType())) {
						return;
					}
					String name = NMS.getInstance().getBannerCustomName(block);
					InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
						ArmorStand line1 = (ArmorStand) entry.getValue().get("1");
						if (name == null || name.equals("")) {
							if (!PlainTextComponentSerializer.plainText().serialize(line1.getCustomName()).equals("") || line1.isCustomNameVisible()) {
								line1.setCustomName("");
								line1.setCustomNameVisible(false);
								PacketManager.updateArmorStandOnlyMeta(line1);
							}
						} else {
							Component component;
							if (JsonUtils.isValid(name)) {
								try {
									component = GsonComponentSerializer.gson().deserialize(name);
								} catch (Throwable e) {
									component = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(name));
								}
							} else {
								component = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(name));
							}
							String matchingName = LegacyComponentSerializer.legacySection().serialize(component);
							if (stripColorBlacklist) {
								matchingName = ChatColorUtils.stripColor(matchingName);
							}
							if (blacklist.test(matchingName)) {
								if (!PlainTextComponentSerializer.plainText().serialize(line1.getCustomName()).equals("") || line1.isCustomNameVisible()) {
									line1.setCustomName("");
									line1.setCustomNameVisible(false);
									PacketManager.updateArmorStandOnlyMeta(line1);
								}
							} else {
								if (!line1.getCustomName().equals(component) || !line1.isCustomNameVisible()) {
									line1.setCustomName(component);
									line1.setCustomNameVisible(true);
									PacketManager.updateArmorStandOnlyMeta(line1);
								}
							}
						}
					});
				}, delay);
			}
		}, 0, checkingPeriod).getTaskId();
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBreakBanner(TileEntityRemovedEvent event) {
		Block block = event.getBlock();
		if (!bannerMap.containsKey(block)) {
			return;
		}

		Map<String, Object> map = bannerMap.get(block);
		if (map.get("1") instanceof ArmorStand) {
			ArmorStand stand = (ArmorStand) map.get("1");
			PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
		}
		bannerMap.remove(block);
	}
	
	public Set<Block> nearbyBanner() {
		return TileEntityManager.getTileEntites(TileEntityType.BANNER);
	}
	
	public boolean isActive(Location loc) {
		return PlayerLocationManager.hasPlayerNearby(loc);
	}
	
	public boolean isBanner(Material material) {
		TileEntityType type = TileEntity.getTileEntityType(material);
		return type != null && type.equals(TileEntityType.BANNER);
	}
	
	public boolean isWallBanner(Material material) {
		return material.name().contains("WALL");
	}
	
	@SuppressWarnings("deprecation")
	public Map<String, ArmorStand> spawnArmorStands(Block block) {
		Map<String, ArmorStand> map = new HashMap<>();
		
		if (isWallBanner(block.getType())) {
			ArmorStand line1 = new ArmorStand(block.getLocation().add(0.5, 0.0, 0.5));
			setStand(line1);
			
			map.put("1", line1);
			
			PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), line1);
		} else {
			Location origin = block.getLocation().add(0.5, 1.0, 0.5);
			Vector vector;
			if (InteractionVisualizer.version.isLegacy()) {				
				org.bukkit.material.Banner banner = (org.bukkit.material.Banner) block.getState().getData();
				vector = getDirection(banner.getFacing()).multiply(0.3125);
			} else {
				Rotatable rotate = (Rotatable) block.getBlockData();
				vector = getDirection(rotate.getRotation()).multiply(0.3125);
			}
			
			ArmorStand line1 = new ArmorStand(origin.clone().add(vector));
			setStand(line1);
			
			map.put("1", line1);
			
			PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), line1);
		}
		
		return map;
	}
	
	public Vector getDirection(BlockFace face) {
        Vector direction = new Vector(face.getModX(), face.getModY(), face.getModZ());
        if (face.getModX() != 0 || face.getModY() != 0 || face.getModZ() != 0) {
            direction.normalize();
        }
        return direction;
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
