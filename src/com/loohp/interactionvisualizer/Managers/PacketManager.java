package com.loohp.interactionvisualizer.Managers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.EntityHolders.ItemFrame;
import com.loohp.interactionvisualizer.EntityHolders.VisualizerEntity;
import com.loohp.interactionvisualizer.Protocol.ServerPacketSender;

public class PacketManager implements Listener {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static List<String> exemptBlocks = InteractionVisualizer.exemptBlocks;
	
	public static ConcurrentHashMap<VisualizerEntity, List<Player>> active = new ConcurrentHashMap<VisualizerEntity, List<Player>>();
	public static ConcurrentHashMap<VisualizerEntity, Boolean> loaded = new ConcurrentHashMap<VisualizerEntity, Boolean>();
	private static ConcurrentHashMap<VisualizerEntity, Integer> cache = new ConcurrentHashMap<VisualizerEntity, Integer>();
	
	public static ConcurrentHashMap<Player, CopyOnWriteArrayList<VisualizerEntity>> playerStatus = new ConcurrentHashMap<Player, CopyOnWriteArrayList<VisualizerEntity>>();
	
	public static void run() {
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			Iterator<Entry<VisualizerEntity, Boolean>> itr = loaded.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<VisualizerEntity, Boolean> entry = itr.next();
				VisualizerEntity entity = entry.getKey();
				if (entity instanceof ArmorStand) {
					ArmorStand stand = (ArmorStand) entity;
					if (!PlayerRangeManager.hasPlayerNearby(stand.getLocation())) {
						continue;
					}
					if (entry.getValue()) {
						if (!plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(plugin, () -> {		
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
						Bukkit.getScheduler().runTask(plugin, () -> {
							if (!PlayerRangeManager.hasPlayerNearby(stand.getLocation())) {
								return;
							}
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(stand.getLocation().getBlock().getType())) {
								sendArmorStandSpawn(players, stand);
								updateArmorStand(stand);
								loaded.put(entity, true);
							}
						});
					}
				} else if (entity instanceof Item) {
					Item item = (Item) entity;
					if (!PlayerRangeManager.hasPlayerNearby(item.getLocation())) {
						continue;
					}
					if (entry.getValue()) {
						if (!plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(plugin, () -> {
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
						if (!plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(plugin, () -> {
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(item.getLocation().getBlock().getType())) {
								sendItemSpawn(players, item);
								updateItem(item);
								loaded.put(entity, true);
							}
						});
					}
				} else if (entity instanceof ItemFrame) {
					ItemFrame frame = (ItemFrame) entity;
					if (!PlayerRangeManager.hasPlayerNearby(frame.getLocation())) {
						continue;
					}
					if (entry.getValue()) {
						if (!plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(plugin, () -> {
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
						if (!plugin.isEnabled()) {
							return;
						}
						Bukkit.getScheduler().runTask(plugin, () -> {
							List<Player> players = active.get(entity);
							if (players == null) {
								return;
							}
							if (!isOccluding(frame.getLocation().getBlock().getType())) {
								sendItemFrameSpawn(players, frame);
								updateItemFrame(frame);
								loaded.put(entity, true);
							}
						});
					}
				}
				try {TimeUnit.MILLISECONDS.sleep(5);} catch (InterruptedException e) {e.printStackTrace();}
			}
			if (plugin.isEnabled()) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
			}
		});
	}
	
	private static boolean isOccluding(Material material) {
		if (exemptBlocks.contains(material.toString().toUpperCase())) {
			return false;
		}
		return material.isOccluding();
	}
	
	public static int update() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				List<VisualizerEntity> activeList = playerStatus.get(player);
				if (activeList == null) {
					continue;
				}
				
				List<Player> playerList = new LinkedList<Player>();
				playerList.add(player);
				
				for (VisualizerEntity entity : activeList) {
					if (entity.getWorld().equals(player.getWorld()) && entity.getLocation().distanceSquared(player.getLocation()) <= 4096) {
						if (active.containsKey(entity)) {
							if (loaded.get(entity)) {
								continue;
							}
						}
						
						if (entity instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) entity;
							removeArmorStand(playerList, stand, false);
						} else if (entity instanceof Item) {
							Item item = (Item) entity;
							removeItem(playerList, item, false);
						} else if (entity instanceof ItemFrame) {
							ItemFrame frame = (ItemFrame) entity;
							removeItemFrame(playerList, frame, false);
						}
					}
				}
				
				for (VisualizerEntity entity : active.keySet()) {
					if (entity.getWorld().equals(player.getWorld()) && entity.getLocation().distanceSquared(player.getLocation()) <= 4096) {
						if (activeList.contains(entity)) {
							continue;
						}
						if (!active.get(entity).contains(player)) {
							continue;
						}
						Boolean isLoaded = loaded.get(entity);
						if (isLoaded == null || !isLoaded) {
							continue;
						}
						
						if (entity instanceof ArmorStand) {
							ArmorStand stand = (ArmorStand) entity;
							sendArmorStandSpawn(playerList, stand);
							updateArmorStand(playerList, stand);
						} else if (entity instanceof Item) {
							Item item = (Item) entity;
							sendItemSpawn(playerList, item);
							updateItem(playerList, item);
						} else if (entity instanceof ItemFrame) {
							ItemFrame frame = (ItemFrame) entity;
							sendItemFrameSpawn(playerList, frame);
							updateItemFrame(playerList, frame);
						}
					}
				}
			}
		}, 0, 20).getTaskId();
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
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.sendHandMovement(playersInRange, entity);
		});
	}
	
	public static void sendArmorStandSpawn(List<Player> players, ArmorStand entity) {
		if (!active.containsKey(entity)) {
			active.put(entity, players);
			loaded.put(entity, true);
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.spawnArmorStand(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).add(entity));
		});
	}
	
	public static void updateArmorStand(ArmorStand entity) {
		List<Player> players = active.get(entity);
		if (players == null) {
			return;
		}
		updateArmorStand(players, entity);
	}
	
	public static void updateArmorStand(List<Player> players, ArmorStand entity) {
		updateArmorStand(players, entity, false);
	}
	
	public static void updateArmorStand(List<Player> players, ArmorStand entity, boolean bypasscache) {
		if (!bypasscache) {
			Integer lastCode = cache.get(entity);
			if (lastCode != null) {
				if (lastCode == entity.cacheCode()) {
					return;
				}
			}
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.updateArmorStand(playersInRange, entity);
		});
        
        cache.put(entity, entity.cacheCode());
	}
	
	public static void updateArmorStandOnlyMeta(ArmorStand entity) {
		List<Player> players = active.get(entity);
		if (players == null) {
			return;
		}
		updateArmorStandOnlyMeta(players, entity);
	}
	
	public static void updateArmorStandOnlyMeta(List<Player> players, ArmorStand entity) {
		updateArmorStandOnlyMeta(players, entity, false);
	}
	
	public static void updateArmorStandOnlyMeta(List<Player> players, ArmorStand entity, boolean bypasscache) {
		if (!bypasscache) {
			Integer lastCode = cache.get(entity);
			if (lastCode != null) {
				if (lastCode == entity.cacheCode()) {
					return;
				}
			}
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.updateArmorStandOnlyMeta(playersInRange, entity);
		});
        
        cache.put(entity, entity.cacheCode());
	}
	
	public static void removeArmorStand(List<Player> players, ArmorStand entity, boolean removeFromActive) {
		if (removeFromActive) {
			active.remove(entity);
			loaded.remove(entity);
			cache.remove(entity);
		}

		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.removeArmorStand(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).remove(entity));
		});
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

		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.spawnItem(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).add(entity));
		});
	}
	
	public static void updateItem(Item entity) {
		List<Player> players = active.get(entity);
		if (players == null) {
			return;
		}
		updateItem(players, entity);
	}
	
	public static void updateItem(List<Player> players, Item entity) {
		updateItem(players, entity, false);
	}
	
	public static void updateItem(List<Player> players, Item entity, boolean bypasscache) {		
		if (!bypasscache) {
			Integer lastCode = cache.get(entity);
			if (lastCode != null) {
				if (lastCode == entity.cacheCode()) {
					return;
				}
			}
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.updateItem(playersInRange, entity);
		});
		
		cache.put(entity, entity.cacheCode());
	}
	
	public static void removeItem(List<Player> players, Item entity, boolean removeFromActive) {
		if (entity.getItemStack().getType().equals(Material.AIR)) {
			return;
		}
		if (removeFromActive) {
			active.remove(entity);
			loaded.remove(entity);
			cache.remove(entity);
		}

		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.removeItem(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).remove(entity));
		});
	}
	
	public static void removeItem(List<Player> players, Item entity) {
		removeItem(players, entity, true);
	}
	
	public static void sendItemFrameSpawn(List<Player> players, ItemFrame entity) {
		if (!active.containsKey(entity)) {
			active.put(entity, players);
			loaded.put(entity, true);
		}

		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.spawnItemFrame(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).add(entity));
		});
	}
	
	public static void updateItemFrame(ItemFrame entity) {
		List<Player> players = active.get(entity);
		if (players == null) {
			return;
		}
		
		updateItemFrame(players, entity);
	}
	
	public static void updateItemFrame(List<Player> players , ItemFrame entity) {
		updateItemFrame(players, entity, false);
	}
	
	public static void updateItemFrame(List<Player> players , ItemFrame entity, boolean bypasscache) {
		if (!bypasscache) {
			Integer lastCode = cache.get(entity);
			if (lastCode != null) {
				if (lastCode == entity.cacheCode()) {
					return;
				}
			}
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.updateItemFrame(playersInRange, entity);
		});
        
        cache.put(entity, entity.cacheCode());
	}
	
	public static void removeItemFrame(List<Player> players, ItemFrame entity, boolean removeFromActive) {
		if (removeFromActive) {
			active.remove(entity);
			loaded.remove(entity);
			cache.remove(entity);
		}
		
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> playersInRange = players.stream().filter(each -> (each.getWorld().equals(entity.getWorld())) && (each.getLocation().distanceSquared(entity.getLocation()) <= 4096)).collect(Collectors.toList());
			ServerPacketSender.removeItemFrame(playersInRange, entity);
			playersInRange.forEach((each) -> playerStatus.get(each).remove(entity));
		});
	}
	
	public static void removeItemFrame(List<Player> players, ItemFrame entity) {
		removeItemFrame(players, entity, true);
	}
	
	public static void reset(Player theplayer) {
		Bukkit.getScheduler().runTask(plugin, () -> removeAll(theplayer));
		int delay = 10 + (int) Math.ceil((double) active.size() / 5.0);
		Bukkit.getScheduler().runTaskLater(plugin, () -> sendPlayerPackets(theplayer), delay);
	}
	
	public static void removeAll(Player theplayer) {
		playerStatus.put(theplayer, new CopyOnWriteArrayList<VisualizerEntity>());
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> player = new ArrayList<Player>();
			player.add(theplayer);
			int count = 0;
			int delay = 1;
			for (Entry<VisualizerEntity, List<Player>> entry : active.entrySet()) {
				count++;
				if (count > 5) {
					delay++;
					count = 0;
				}
				VisualizerEntity entity = entry.getKey();
				if (entity instanceof ArmorStand) {
					Bukkit.getScheduler().runTaskLater(plugin, () -> removeArmorStand(player, (ArmorStand) entity, false), delay);
				}
				if (entity instanceof Item) {
					Bukkit.getScheduler().runTaskLater(plugin, () -> removeItem(player, (Item) entity, false), delay);
				}
				if (entity instanceof ItemFrame) {
					Bukkit.getScheduler().runTaskLater(plugin, () -> removeItemFrame(player, (ItemFrame) entity, false), delay);
				}
			}
		});
	}
	
	public static void sendPlayerPackets(Player theplayer) {
		playerStatus.put(theplayer, new CopyOnWriteArrayList<VisualizerEntity>());
		if (!plugin.isEnabled()) {
			return;
		}
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			List<Player> player = new ArrayList<Player>();
			player.add(theplayer);
			int count = 0;
			int delay = 1;
			for (Entry<VisualizerEntity, List<Player>> entry : active.entrySet()) {
				VisualizerEntity entity = entry.getKey();
				if (entry.getValue().contains(theplayer)) {
					if (loaded.get(entity)) {
						count++;
						if (count > 5) {
							delay++;
							count = 0;
						}
						if (entity instanceof ArmorStand) {
							Bukkit.getScheduler().runTaskLater(plugin, () -> {
								sendArmorStandSpawn(player, (ArmorStand) entity);
								updateArmorStand(player, (ArmorStand) entity, true);
							}, delay);
						}
						if (entity instanceof Item) {
							Bukkit.getScheduler().runTaskLater(plugin, () -> {
								sendItemSpawn(player, (Item) entity);
								updateItem(player, (Item) entity, true);
							}, delay);	
						}
						if (entity instanceof ItemFrame) {
							Bukkit.getScheduler().runTaskLater(plugin, () -> {
								sendItemFrameSpawn(player, (ItemFrame) entity);
								updateItemFrame(player, (ItemFrame) entity, true);
							}, delay);
						}
					}
				}
			}
		});
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		playerStatus.put(event.getPlayer(), new CopyOnWriteArrayList<VisualizerEntity>());
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		playerStatus.put(event.getPlayer(), new CopyOnWriteArrayList<VisualizerEntity>());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		playerStatus.remove(event.getPlayer());
	}
}
