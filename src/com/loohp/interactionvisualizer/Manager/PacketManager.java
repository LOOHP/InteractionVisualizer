package com.loohp.interactionvisualizer.Manager;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Holder.ArmorStand;
import com.loohp.interactionvisualizer.Holder.Item;
import com.loohp.interactionvisualizer.Holder.ItemFrame;
import com.loohp.interactionvisualizer.Holder.VisualizerEntity;

public class PacketManager implements Listener {
	
	private static ProtocolManager protocolManager = InteractionVisualizer.protocolManager;
	private static String version = InteractionVisualizer.version;
	private static List<String> exemptBlocks = InteractionVisualizer.exemptBlocks;
	
	public static ConcurrentHashMap<VisualizerEntity, List<Player>> active = new ConcurrentHashMap<VisualizerEntity, List<Player>>();
	public static ConcurrentHashMap<VisualizerEntity, Boolean> loaded = new ConcurrentHashMap<VisualizerEntity, Boolean>();
	
	public static void run() {
		if (!InteractionVisualizer.plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<VisualizerEntity, Boolean>> itr = loaded.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<VisualizerEntity, Boolean> entry = itr.next();
				VisualizerEntity entity = entry.getKey();
				if (entry.getKey() instanceof ArmorStand) {
					ArmorStand stand = (ArmorStand) entity;
					if (entry.getValue()) {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(stand.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (isOccluding(stand.getLocation().getBlock().getType())) {
								removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand, false);
								loaded.put(entity, false);
							}
						});
					} else {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(stand.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(stand.getLocation().getBlock().getType())) {
								sendArmorStandSpawn(players, stand);
								updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
								loaded.put(entity, true);
							}
						});
					}
				} else if (entry.getKey() instanceof Item) {
					Item item = (Item) entity;
					if (entry.getValue()) {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(item.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (isOccluding(item.getLocation().getBlock().getType())) {
								removeItem(InteractionVisualizer.getOnlinePlayers(), item, false);
								loaded.put(entity, false);
							}
						});
					} else {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(item.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(item.getLocation().getBlock().getType())) {
								sendItemSpawn(players, item);
								updateItem(InteractionVisualizer.getOnlinePlayers(), item);
								loaded.put(entity, true);
							}
						});
					}
				} else if (entry.getKey() instanceof ItemFrame) {
					ItemFrame frame = (ItemFrame) entity;
					if (entry.getValue()) {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(frame.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (isOccluding(frame.getLocation().getBlock().getType())) {
								removeItemFrame(InteractionVisualizer.getOnlinePlayers(), frame, false);
								loaded.put(entity, false);
							}
						});
					} else {
						if (!InteractionVisualizer.plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(frame.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(frame.getLocation().getBlock().getType())) {
								sendItemFrameSpawn(players, frame);
								updateItemFrame(InteractionVisualizer.getOnlinePlayers(), frame);
								loaded.put(entity, true);
							}
						});
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (InteractionVisualizer.plugin.isEnabled()) {
				Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> run(), 1);
			}
		});
	}
	
	private static boolean isOccluding(Material material) {
		if (exemptBlocks.contains(material.toString().toUpperCase())) {
			return false;
		}
		return material.isOccluding();
	}
	/*
	public static void sendLightUpdate(List<Player> players, Location location, int skysubchunkbitmask, List<byte[]> skybytearray, int blocksubchunkbitmask, List<byte[]> blockbytearray) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.LIGHT_UPDATE);
		int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
		int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
		
		packet.getIntegers().write(0, chunkX);
		packet.getIntegers().write(1, chunkZ);
		packet.getIntegers().write(2, skysubchunkbitmask);
		packet.getIntegers().write(3, blocksubchunkbitmask);
		packet.getIntegers().write(4, ~skysubchunkbitmask);
		packet.getIntegers().write(5, ~blocksubchunkbitmask);
		packet.getModifier().write(6, skybytearray);
		packet.getModifier().write(7, blockbytearray);
		
		try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	*/
	public static void sendHandMovement(List<Player> players, Player entity) {
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
		packet1.getModifier().writeDefaults();
		packet1.getIntegers().write(0, entity.getEntityId());
		packet1.getIntegers().write(1, 0);
		
		try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendArmorStandSpawn(List<Player> players, ArmorStand entity) {
		if (!active.containsKey(entity)) {
			active.put(entity, players);
			loaded.put(entity, true);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
		packet1.getIntegers().write(0, entity.getEntityId());
		if (!version.contains("legacy")) {
			packet1.getIntegers().write(1, 1);
		} else {
			packet1.getIntegers().write(1, 30);
		}
		packet1.getIntegers().write(2, (int) (entity.getVelocity().getX() * 8000));
		packet1.getIntegers().write(3, (int) (entity.getVelocity().getY() * 8000));
		packet1.getIntegers().write(4, (int) (entity.getVelocity().getZ() * 8000));		
		packet1.getDoubles().write(0, entity.getLocation().getX());
		packet1.getDoubles().write(1, entity.getLocation().getY());
		packet1.getDoubles().write(2, entity.getLocation().getZ());
		packet1.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Yaw
		packet1.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F)); //Pitch
		packet1.getBytes().write(2, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Head
		
		PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet2.getIntegers().write(0, entity.getEntityId());	
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
				protocolManager.sendServerPacket(player, packet2);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArmorStand(List<Player> players, ArmorStand entity) {
		players = active.get(entity);
		if (players == null) {
			return;
		}
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getDoubles().write(0, entity.getLocation().getX());
		packet1.getDoubles().write(1, entity.getLocation().getY());
		packet1.getDoubles().write(2, entity.getLocation().getZ());
		packet1.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		packet1.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
			
		PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet2.getIntegers().write(0, entity.getEntityId());	
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet3.getIntegers().write(0, entity.getEntityId());
        packet3.getItemSlots().write(0, ItemSlot.MAINHAND);
        packet3.getItemModifier().write(0, entity.getItemInMainHand());

        PacketContainer packet4 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet4.getIntegers().write(0, entity.getEntityId());
        packet4.getItemSlots().write(0, ItemSlot.HEAD);
        packet4.getItemModifier().write(0, entity.getHelmet());
		/*
		PacketContainer packet5 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
		packet5.getIntegers().write(0, entity.getEntityId());
		packet5.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
		packet5.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
		packet5.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));
		*/
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
				protocolManager.sendServerPacket(player, packet2);
				protocolManager.sendServerPacket(player, packet3);
				protocolManager.sendServerPacket(player, packet4);
				//protocolManager.sendServerPacket(player, packet5);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArmorStand(List<Player> players, ArmorStand entity, boolean onlymetadata) {
		if (!onlymetadata) {
			updateArmorStand(players, entity);
			return;
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet1.getIntegers().write(0, entity.getEntityId());	
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeArmorStand(List<Player> players, ArmorStand entity, boolean removeFromActive) {
		if (removeFromActive) {
			active.remove(entity);
			active.remove(entity);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet1.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeArmorStand(List<Player> players, ArmorStand entity) {
		removeArmorStand(players, entity, true);
	}
	
	public static void sendItemSpawn(List<Player> players, Item entity) {
		if (!active.containsKey(entity)) {
			active.put(entity, players);
			loaded.put(entity, true);
		}
		
		if (entity.getItemStack().getType().equals(Material.AIR)) {
			return;
		}
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
        packet1.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
        packet1.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));
        packet1.getIntegers().write(4, (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
        packet1.getIntegers().write(5, (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        if (InteractionVisualizer.version.equals("1.13") || InteractionVisualizer.version.equals("1.13.1") || InteractionVisualizer.version.contains("legacy")) {
            packet1.getIntegers().write(6, 2);
            packet1.getIntegers().write(7, 1);
        } else {
            packet1.getEntityTypeModifier().write(0, entity.getType());
            packet1.getIntegers().write(6, 1);
        }
        packet1.getUUIDs().write(0, entity.getUniqueId());
        Location location = entity.getLocation();
        packet1.getDoubles().write(0, location.getX());
        packet1.getDoubles().write(1, location.getY());
        packet1.getDoubles().write(2, location.getZ());
		
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateItem(List<Player> players, Item entity) {
		players = active.get(entity);
		if (players == null) {
			return;
		}
		if (entity.getItemStack().getType().equals(Material.AIR)) {
			return;
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet2.getIntegers().write(0, entity.getEntityId());
        packet2.getDoubles().write(0, entity.getLocation().getX());
        packet2.getDoubles().write(1, entity.getLocation().getY());
        packet2.getDoubles().write(2, entity.getLocation().getZ());
        packet2.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet2.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
		
		PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
		packet3.getIntegers().write(0, entity.getEntityId());
		packet3.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
		packet3.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
		packet3.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));
		
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
				protocolManager.sendServerPacket(player, packet2);
				protocolManager.sendServerPacket(player, packet3);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeItem(List<Player> players, Item entity, boolean removeFromActive) {
		if (entity.getItemStack().getType().equals(Material.AIR)) {
			return;
		}
		if (removeFromActive) {
			active.remove(entity);
			loaded.remove(entity);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet1.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeItem(List<Player> players, Item entity) {
		removeItem(players, entity, true);
	}
	
	public static void sendItemFrameSpawn(List<Player> players, ItemFrame entity) {
		if (!active.containsKey(entity)) {
			active.put(entity, players);
			loaded.put(entity, true);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getIntegers().write(1, 0);
        packet1.getIntegers().write(2, 0);
        packet1.getIntegers().write(3, 0);
        packet1.getIntegers().write(4, (int) (entity.getPitch() * 256.0F / 360.0F));
        packet1.getIntegers().write(5, (int) (entity.getYaw() * 256.0F / 360.0F));
        if (InteractionVisualizer.version.equals("1.13") || InteractionVisualizer.version.equals("1.13.1") || InteractionVisualizer.version.contains("legacy")) {
            packet1.getIntegers().write(6, 33);
            packet1.getIntegers().write(7, getItemFrameData(entity));
        } else {
            packet1.getEntityTypeModifier().write(0, entity.getType());
            packet1.getIntegers().write(6, getItemFrameData(entity));
        }
        packet1.getUUIDs().write(0, entity.getUniqueId());
        Location location = entity.getLocation();
        packet1.getDoubles().write(0, location.getX());
        packet1.getDoubles().write(1, location.getY());
        packet1.getDoubles().write(2, location.getZ());
        
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static int getItemFrameData(ItemFrame frame) {
		switch (frame.getAttachedFace()) {
		case DOWN:
			return 0;
		case UP:
			return 1;
		case NORTH:
			return 2;
		case SOUTH:
			return 3;
		case WEST:
			return 4;
		case EAST:
			return 5;
		default:
			return 0;	
		}
	}
	
	public static void updateItemFrame(List<Player> players, ItemFrame entity) {
		players = active.get(entity);
		if (players == null) {
			return;
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeItemFrame(List<Player> players, ItemFrame entity, boolean removeFromActive) {
		if (removeFromActive) {
			active.remove(entity);
			loaded.remove(entity);
		}
		
		PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet1.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet1);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void removeItemFrame(List<Player> players, ItemFrame entity) {
		removeItemFrame(players, entity, true);
	}
	
	public static void removeAll(Player theplayer) {
		List<Player> player = new ArrayList<Player>();
		player.add(theplayer);
		for (Entry<VisualizerEntity, List<Player>> entry : active.entrySet()) {
			Object entity = entry.getKey();
			if (entity instanceof ArmorStand) {
				removeArmorStand(player, (ArmorStand) entity, false);
			}
			if (entity instanceof Item) {
				removeItem(player, (Item) entity, false);
			}
			if (entity instanceof ItemFrame) {
				removeItemFrame(player, (ItemFrame) entity, false);
			}
		}
	}
	
	public static void sendPlayerPackets(Player theplayer) {
		List<Player> player = new ArrayList<Player>();
		player.add(theplayer);
		for (Entry<VisualizerEntity, List<Player>> entry : active.entrySet()) {
			VisualizerEntity entity = entry.getKey();
			if (entry.getValue().contains(theplayer)) {
				if (loaded.get(entity)) {
					if (entity instanceof ArmorStand) {
						sendArmorStandSpawn(player, (ArmorStand) entity);
						updateArmorStand(player, (ArmorStand) entity);
					}
					if (entity instanceof Item) {
						sendItemSpawn(player, (Item) entity);
						updateItem(player, (Item) entity);
					}
					if (entity instanceof ItemFrame) {
						sendItemFrameSpawn(player, (ItemFrame) entity);
						updateItemFrame(player, (ItemFrame) entity);
					}
				}
			}
		}
	}

}
