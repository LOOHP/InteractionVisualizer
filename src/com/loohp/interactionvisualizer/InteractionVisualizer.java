package com.loohp.interactionvisualizer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.EntityHolders.VisualizerEntity;
import com.loohp.interactionvisualizer.Managers.CustomBlockDataManager;
import com.loohp.interactionvisualizer.Managers.EffectManager;
import com.loohp.interactionvisualizer.Managers.EnchantmentManager;
import com.loohp.interactionvisualizer.Managers.LangManager;
import com.loohp.interactionvisualizer.Managers.MaterialManager;
import com.loohp.interactionvisualizer.Managers.MusicManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.Managers.SoundManager;
import com.loohp.interactionvisualizer.Managers.TaskManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager;
import com.loohp.interactionvisualizer.Metrics.Charts;
import com.loohp.interactionvisualizer.Metrics.Metrics;
import com.loohp.interactionvisualizer.PlaceholderAPI.Placeholders;
import com.loohp.interactionvisualizer.Protocol.WatchableCollection;
import com.loohp.interactionvisualizer.Updater.Updater;
import com.loohp.interactionvisualizer.Utils.LegacyInstrumentUtils;
import com.loohp.interactionvisualizer.Utils.LegacyRecordsUtils;

import net.md_5.bungee.api.ChatColor;

public class InteractionVisualizer extends JavaPlugin {
	
	public static Plugin plugin = null;
	public static ProtocolManager protocolManager;
	public static FileConfiguration config;
	
	public static String version = "";
	public static int metaversion = 0;
	
	public static Boolean openinv = false;
	public static Boolean vanish = false;
	public static Boolean cmi = false;
	public static Boolean ess3 = false;
	
	public static List<Player> itemStand = new CopyOnWriteArrayList<Player>();
	public static List<Player> itemDrop = new CopyOnWriteArrayList<Player>();
	public static List<Player> holograms = new CopyOnWriteArrayList<Player>();
	
	public static List<String> exemptBlocks = new ArrayList<String>();
	
	public static World defaultworld;
	public static Location defaultlocation;
	
	public static boolean itemStandEnabled = true;
	public static boolean itemDropEnabled = true;
	public static boolean hologramsEnabled = true;
	
	public static Integer furnaceChecking = 20;
	public static Integer blastfurnaceChecking = 20;
	public static Integer smokerChecking = 20;
	public static Integer brewingstandChecking = 20;
	public static Integer beaconChecking = 20;
	public static Integer jukeboxChecking = 20;
	
	public static Integer gcPeriod = 600;
	
	public static Integer tileEntityChunkPerTick = 9;
	
	public static boolean UpdaterEnabled = true;
	
	@Override
	public void onEnable() {
		plugin = getServer().getPluginManager().getPlugin("InteractionVisualizer");
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		if (getServer().getPluginManager().getPlugin("OpenInv") != null) {
			hookMessage("OpenInv");
			openinv = true;
		}
		if (getServer().getPluginManager().getPlugin("SuperVanish") != null || getServer().getPluginManager().getPlugin("PremiumVanish") != null) {
			hookMessage("SuperVanish/PremiumVanish");
			vanish = true;
		}
		if (getServer().getPluginManager().getPlugin("CMI") != null) {
			hookMessage("CMI");
			cmi = true;
		}
		if (getServer().getPluginManager().getPlugin("Essentials") != null) {
			hookMessage("Essentials");
			ess3 = true;
		}
		//EntityCreator.setup();
		
		int pluginId = 7024;

		Metrics metrics = new Metrics(this, pluginId);
		
		String packageName = getServer().getClass().getPackage().getName();
		
		if (packageName.contains("1_15_R1")) {
	    	version = "1.15";
	    	metaversion = 3;
	    } else if (packageName.contains("1_14_R1")) {
	    	version = "1.14";
	    	metaversion = 2;
	    } else if (packageName.contains("1_13_R2")) {
	    	version = "1.13.1";
	    	metaversion = 1;
	    } else if (packageName.contains("1_13_R1")) {
	    	version = "1.13";
	    	metaversion = 1;
	    } else if (packageName.contains("1_12_R1")) {
	    	version = "legacy1.12";
	    	metaversion = 0;
	    } else if (packageName.contains("1_11_R1")) {
	    	version = "legacy1.11";
	    	metaversion = 0;
	    } else if (packageName.contains("1_10_R1")) {
	    	version = "legacy1.10";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_9_R2")) {
	    	version = "legacy1.9.4";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_9_R1")) {
	    	version = "legacy1.9";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R3")) {
	    	version = "OLDlegacy1.8.4";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R2")) {
	    	version = "OLDlegacy1.8.3";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R1")) {
	    	version = "OLDlegacy1.8";
	    	unsupportedMessage();
	    } else {
	    	unsupportedMessage();
	    }
		
		plugin.getConfig().options().copyDefaults(true);
		config = plugin.getConfig();
		plugin.saveConfig();
		loadConfig();
		
		defaultworld = getServer().getWorlds().get(0);
		defaultlocation = new Location(defaultworld, 0, 0, 0);
		if (!version.contains("legacy") && !version.equals("1.13") && !version.equals("1.13.1")) {
			defaultworld.setChunkForceLoaded(0, 0, true);
		}
		
		if (config.getBoolean("Options.DownloadLanguageFiles")) {
			getServer().getScheduler().runTaskAsynchronously(this, () -> LangManager.generate());
		}
		WatchableCollection.setup();
		SoundManager.setup();
		EnchantmentManager.setup();
		EffectManager.setup();
		MusicManager.setup();
		Database.setup();
		CustomBlockDataManager.setup();
		TaskManager.run();
		TileEntityManager.run();
		PlayerLocationManager.run();
		CustomBlockDataManager.intervalSaveToFile();
		PacketManager.run();
		PlayerLocationManager.updateLocation();
		
		MaterialManager.setup();
		
		if (version.contains("legacy")) {
			LegacyRecordsUtils.setup();
			LegacyInstrumentUtils.setup();
		}
		
		getCommand("interactionvisualizer").setExecutor(new Commands());
		
		TaskManager.setup();
		
		Charts.registerCharts(metrics);
		
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new Placeholders().register();
		}
		
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getNearbyEntities(new Location(world, 0, 0, 0), 2, 2, 2)) {
				if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
					entity.remove();
				}
			}
		}
		
		exemptBlocks.add("CRAFTING_TABLE");
		exemptBlocks.add("WORKBENCH");
		exemptBlocks.add("LOOM");
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] InteractionVisualizer has been enabled!");
		
		Bukkit.getScheduler().runTask(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PacketManager.playerStatus.put(player, Collections.newSetFromMap(new ConcurrentHashMap<VisualizerEntity, Boolean>()));
				
				Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, () -> {
					if (!Database.playerExists(player)) {
						Database.createPlayer(player);
					}
					Database.loadPlayer(player, true);
				});
			}
		});
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
			if (UpdaterEnabled) {
				String version = Updater.checkUpdate();
				if (version.equals("latest")) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] You are running the latest version: " + plugin.getDescription().getVersion() + "!");
				} else {
					Updater.sendUpdateMessage(Bukkit.getConsoleSender(), version);
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.hasPermission("interactionvisualizer.update")) {
							Updater.sendUpdateMessage(player, version);
						}
					}
				}
			}
		}, 100);
		
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY_LIVING)) {
		        	return;
		        }
		        
		        PacketContainer packet = event.getPacket();
		        if (packet.getIntegers().read(1) != 1) {
		        	return;
		        }
		        
		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Spawn Packet to " + event.getPlayer().getName() + " " + packet.getDoubles().read(0) + " " + packet.getDoubles().read(1) + " " + packet.getDoubles().read(2));
		    }
		});
		/*
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_METADATA) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
		        	return;
		        }
		        
		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Update Packet to " + event.getPlayer().getName());
		    }
		});
		
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_DESTROY) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_DESTROY)) {
		        	return;
		        }
		        
		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Remove Packet to " + event.getPlayer().getName());
		    }
		});
		
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ANIMATION) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ANIMATION)) {
		        	return;
		        }
		        
		        Bukkit.getConsoleSender().sendMessage("Sending Animation Packet to " + event.getPlayer().getName());
		    }
		});
		*/
	}
	
	@Override
	public void onDisable() {
		CustomBlockDataManager.save();
		
		if (!Bukkit.getOnlinePlayers().isEmpty()) {
			getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractionVisualizer] Plugin reload detected, attempting to despawn all visual entities. If anything went wrong, please restart! (Reloads are always not recommended)");
			int [] entityIdArray = new int[PacketManager.active.size()];
			int i = 0;
			for (Entry<VisualizerEntity, List<Player>> entry : PacketManager.active.entrySet()) {
				entityIdArray[i] = entry.getKey().getEntityId();
				i++;
			}
			
			PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
			packet1.getIntegerArrays().write(0, entityIdArray);
			
			try {
				for (Player player : Bukkit.getOnlinePlayers()) {
					protocolManager.sendServerPacket(player, packet1);
				}
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] InteractionVisualizer has been disabled!");
	}
	
	public static void loadConfig() {
		itemStandEnabled = config.getBoolean("Modules.ItemStand.Enabled");
		itemDropEnabled = config.getBoolean("Modules.ItemDrop.Enabled");
		hologramsEnabled = config.getBoolean("Modules.Hologram.Enabled");
		
		furnaceChecking = config.getInt("Blocks.Furnace.CheckingPeriod");
		blastfurnaceChecking = config.getInt("Blocks.BlastFurnace.CheckingPeriod");
		smokerChecking = config.getInt("Blocks.Smoker.CheckingPeriod");
		brewingstandChecking = config.getInt("Blocks.BrewingStand.CheckingPeriod");
		beaconChecking = config.getInt("Blocks.Beacon.CheckingPeriod");
		jukeboxChecking = config.getInt("Blocks.JukeBox.CheckingPeriod");
		
		gcPeriod = config.getInt("GarbageCollector.Period");
		
		tileEntityChunkPerTick = config.getInt("TileEntityUpdate.ChunksPerTick");
		
		UpdaterEnabled = plugin.getConfig().getBoolean("Options.Updater");
	}
	
	public static List<Player> getOnlinePlayers() {
		return new ArrayList<Player>(Bukkit.getOnlinePlayers());
	}
	
	private static void unsupportedMessage() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	}
	
	private static void hookMessage(String pluginName) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractionVisualizer] InteractionVisualizer has hooked into " + pluginName + "!");
	}
	
}
