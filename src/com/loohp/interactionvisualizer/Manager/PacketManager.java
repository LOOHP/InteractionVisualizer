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
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Holder.ArmorStand;
import com.loohp.interactionvisualizer.Holder.Item;
import com.loohp.interactionvisualizer.Holder.ItemFrame;

public class PacketManager implements Listener {
	
	private static ProtocolManager protocolManager = InteractionVisualizer.protocolManager;
	private static String version = InteractionVisualizer.version;
	private static List<String> exemptBlocks = InteractionVisualizer.exemptBlocks;
	
	public static ConcurrentHashMap<Object, List<Player>> active = new ConcurrentHashMap<Object, List<Player>>();
	public static ConcurrentHashMap<Object, Boolean> loaded = new ConcurrentHashMap<Object, Boolean>();
	
	public static int run() {
		return new BukkitRunnable() {
			public void run() {
				Iterator<Entry<Object, Boolean>> itr = loaded.entrySet().iterator();
				int delay = (int) Math.floor(1900.0 / (double) loaded.size());
				while (itr.hasNext()) {
					Entry<Object, Boolean> entry = itr.next();
					Object entity = entry.getKey();
					if (entry.getKey() instanceof ArmorStand) {
						ArmorStand stand = (ArmorStand) entity;
						if (entry.getValue()) {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (isOccluding(stand.getLocation().getBlock().getType())) {
									removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand, false);
									loaded.put(entity, false);
								}
							});
						} else {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (!isOccluding(stand.getLocation().getBlock().getType())) {
									sendArmorStandSpawn(active.get(entity), stand);
									updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
									loaded.put(entity, true);
								}
							});
						}
					} else if (entry.getKey() instanceof Item) {
						Item item = (Item) entity;
						if (entry.getValue()) {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (isOccluding(item.getLocation().getBlock().getType())) {
									removeItem(InteractionVisualizer.getOnlinePlayers(), item, false);
									loaded.put(entity, false);
								}
							});
						} else {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (!isOccluding(item.getLocation().getBlock().getType())) {
									sendItemSpawn(active.get(entity), item);
									updateItem(InteractionVisualizer.getOnlinePlayers(), item);
									loaded.put(entity, true);
								}
							});
						}
					} else if (entry.getKey() instanceof ItemFrame) {
						ItemFrame frame = (ItemFrame) entity;
						if (entry.getValue()) {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (isOccluding(frame.getLocation().getBlock().getType())) {
									removeItemFrame(InteractionVisualizer.getOnlinePlayers(), frame, false);
									loaded.put(entity, false);
								}
							});
						} else {
							Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
								if (!isOccluding(frame.getLocation().getBlock().getType())) {
									sendItemFrameSpawn(active.get(entity), frame);
									updateItemFrame(InteractionVisualizer.getOnlinePlayers(), frame);
									loaded.put(entity, true);
								}
							});
						}
					}
					try {
						TimeUnit.MILLISECONDS.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskTimerAsynchronously(InteractionVisualizer.plugin, 0, 40).getTaskId();
	}
	
	private static boolean isOccluding(Material material) {
		if (exemptBlocks.contains(material.toString().toUpperCase())) {
			return false;
		}
		return material.isOccluding();
	}
	
	/*
	public static void sendLightUpdate(List<Player> players, Location location, int lightLevel, int subchunkbitmask, List<byte[]> bytearray) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.LIGHT_UPDATE);
		int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
		int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
		
		packet.getIntegers().write(0, chunkX);
		packet.getIntegers().write(1, chunkZ);
		packet.getIntegers().write(2, 0);
		packet.getIntegers().write(3, subchunkbitmask);
		packet.getIntegers().write(4, 0);
		packet.getIntegers().write(5, ~subchunkbitmask);
		packet.getModifier().write(6, new ArrayList<byte[]>());
		packet.getModifier().write(7, bytearray);
		
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
		packet.getModifier().writeDefaults();
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getIntegers().write(1, 0);
		try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

		packet.getIntegers().write(0, entity.getEntityId());
		if (!version.contains("legacy")) {
			packet.getIntegers().write(1, 1);
		} else {
			packet.getIntegers().write(1, 30);
		}
		packet.getIntegers().write(2, 0);
		packet.getIntegers().write(3, 0);
		packet.getIntegers().write(4, 0);
		
		packet.getDoubles().write(0, entity.getLocation().getX());
		packet.getDoubles().write(1, entity.getLocation().getY());
		packet.getDoubles().write(2, entity.getLocation().getZ());

		packet.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Yaw
		packet.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F)); //Pitch
		packet.getBytes().write(2, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Head
		
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
		packet.getIntegers()
    		//Entity ID
    		.write(0, entity.getEntityId())
    		//Velocity x
            .write(1, (int) (entity.getVelocity().getX() * 8000))
	        //Velocity y
	        .write(2, (int) (entity.getVelocity().getY() * 8000))
	        //Velocity z
	        .write(3, (int) (entity.getVelocity().getZ() * 8000));
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
		packet.getIntegers().write(0, entity.getEntityId());

        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateArmorStand(List<Player> players, ArmorStand entity) {
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
		packet.getIntegers().write(0, entity.getEntityId());

        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}       
        
        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getItemSlots().write(0, ItemSlot.MAINHAND);
		packet.getItemModifier().write(0, entity.getItemInMainHand());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getItemSlots().write(0, ItemSlot.HEAD);
		packet.getItemModifier().write(0, entity.getHelmet());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, entity.getEntityId());
        packet.getDoubles().write(0, entity.getLocation().getX());
		packet.getDoubles().write(1, entity.getLocation().getY());
		packet.getDoubles().write(2, entity.getLocation().getZ());
		packet.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		packet.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
		try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
		packet.getIntegers()
    		//Entity ID
    		.write(0, entity.getEntityId())
    		//Velocity x
            .write(1, (int) (entity.getVelocity().getX() * 8000))
	        //Velocity y
	        .write(2, (int) (entity.getVelocity().getY() * 8000))
	        //Velocity z
	        .write(3, (int) (entity.getVelocity().getZ() * 8000));
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        //Location
        Location location = entity.getLocation();

        //and add data based on packet class in NMS  (global scope variable)
        //Reference: https://wiki.vg/Protocol#Spawn_Object
        packet.getIntegers()
            //Entity ID
            .write(0, entity.getEntityId())
            //Velocity x
            .write(1, (int) (entity.getVelocity().getX() * 8000))
	        //Velocity y
	        .write(2, (int) (entity.getVelocity().getY() * 8000))
	        //Velocity z
	        .write(3, (int) (entity.getVelocity().getZ() * 8000))
            //Pitch
            .write(4, (int) (entity.getLocation().getPitch() * 256.0F / 360.0F))
            //Yaw
            .write(5, (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));

        if (InteractionVisualizer.version.equals("1.13") || InteractionVisualizer.version.equals("1.13.1") || InteractionVisualizer.version.contains("legacy")) {
            packet.getIntegers().write(6, 2);
            //int data to mark
            packet.getIntegers().write(7, 1);
        } else {
            //EntityType
            packet.getEntityTypeModifier().write(0, entity.getType());
            //int data to mark
            packet.getIntegers().write(6, 1);
        }
        //UUID
        packet.getUUIDs().write(0, entity.getUniqueId());
        //Location
        packet.getDoubles()
            //X
            .write(0, location.getX())
            //Y
            .write(1, location.getY())
            //Z
            .write(2, location.getZ());
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers()
            //Entity ID
            .write(0, entity.getEntityId())
            //Velocity x
            .write(1, (int) (entity.getVelocity().getX() * 8000))
	        //Velocity y
	        .write(2, (int) (entity.getVelocity().getY() * 8000))
	        //Velocity z
	        .write(3, (int) (entity.getVelocity().getZ() * 8000));
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateItem(List<Player> players, Item entity) {
		if (entity.getItemStack().getType().equals(Material.AIR)) {
			return;
		}
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
		packet.getIntegers().write(0, entity.getEntityId());

        //List<DataWatcher$Item> Type are more complex
        //Create a DataWatcher
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, entity.getEntityId());
        packet.getDoubles().write(0, entity.getLocation().getX());
		packet.getDoubles().write(1, entity.getLocation().getY());
		packet.getDoubles().write(2, entity.getLocation().getZ());
		packet.getBytes().write(0, (byte)(int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		packet.getBytes().write(1, (byte)(int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
		try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet.getIntegers()
        	//Entity ID
        	.write(0, entity.getEntityId())
        	//Velocity x
	        .write(1, (int) (entity.getVelocity().getX() * 8000))
	        //Velocity y
	        .write(2, (int) (entity.getVelocity().getY() * 8000))
	        //Velocity z
	        .write(3, (int) (entity.getVelocity().getZ() * 8000));
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        //Location
        Location location = entity.getLocation();

        //and add data based on packet class in NMS  (global scope variable)
        //Reference: https://wiki.vg/Protocol#Spawn_Object
        packet.getIntegers()
            //Entity ID
            .write(0, entity.getEntityId())
            //Velocity x
            .write(1, 0)
            //Velocity y
            .write(2, 0)
            //Velocity z
            .write(3, 0)
            //Pitch
            .write(4, (int) (entity.getPitch() * 256.0F / 360.0F))
            //Yaw
            .write(5, (int) (entity.getYaw() * 256.0F / 360.0F));

        if (InteractionVisualizer.version.equals("1.13") || InteractionVisualizer.version.equals("1.13.1") || InteractionVisualizer.version.contains("legacy")) {
            packet.getIntegers().write(6, 33);
            //int data to mark
            packet.getIntegers().write(7, getItemFrameData(entity));
        } else {
            //EntityType
            packet.getEntityTypeModifier().write(0, entity.getType());
            //int data to mark
            packet.getIntegers().write(6, getItemFrameData(entity));
        }
        //UUID
        packet.getUUIDs().write(0, entity.getUniqueId());
        //Location
        packet.getDoubles()
            //X
            .write(0, location.getX())
            //Y
            .write(1, location.getY())
            //Z
            .write(2, location.getZ());
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        //Entity ID
		packet.getIntegers().write(0, entity.getEntityId());

        //List<DataWatcher$Item> Type are more complex
        //Create a DataWatcher
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());
        try {
        	for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		packet.getIntegerArrays().write(0, new int[]{entity.getEntityId()});
		try {
			for (Player player : players) {
				protocolManager.sendServerPacket(player, packet);
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
		for (Entry<Object, List<Player>> entry : active.entrySet()) {
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
		for (Entry<Object, List<Player>> entry : active.entrySet()) {
			Object entity = entry.getKey();
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
