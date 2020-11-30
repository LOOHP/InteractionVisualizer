package com.loohp.interactionvisualizer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
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
import com.loohp.interactionvisualizer.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;

public class InteractionVisualizer extends JavaPlugin {
	
	public static InteractionVisualizer plugin = null;
	public static ProtocolManager protocolManager;
	public static FileConfiguration config;
	
	public static MCVersion version;
	public static Integer metaversion = 0;
	
	public static Boolean openinv = false;
	public static Boolean vanish = false;
	public static Boolean cmi = false;
	public static Boolean ess3 = false;
	
	public static List<Player> itemStand = new CopyOnWriteArrayList<Player>();
	public static List<Player> itemDrop = new CopyOnWriteArrayList<Player>();
	public static List<Player> holograms = new CopyOnWriteArrayList<Player>();
	
	public static Set<String> exemptBlocks = new HashSet<String>();
	
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
	
	public static Double playerPickupYOffset = 0.0;
	
	public static Integer gcPeriod = 600;
	
	public static Integer tileEntityChunkPerTick = 9;
	public static Boolean loadTileEntitiesAsync = true;
	
	public static Boolean handMovementEnabled = true;
	
	public static Integer lightUpdatePeriod = 10;
	
	public static boolean UpdaterEnabled = true;
	
	public enum Modules {
		ITEMSTAND,
		ITEMDROP,
		HOLOGRAM;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
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
		
		int pluginId = 7024;

		Metrics metrics = new Metrics(this, pluginId);
		
		version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());
		
		switch (version) {
		case V1_16_4:
		case V1_16_2:
		case V1_16:
		case V1_15:
			metaversion = 3;
			break;
		case V1_14:
			metaversion = 2;
			break;
		case V1_13_1:
		case V1_13:
			metaversion = 1;
			break;
		case V1_12:
		case V1_11:
			metaversion = 0;
			break;
		default:
			unsupportedMessage();
			break;
		}
		
		getConfig().options().copyDefaults(true);
		config = getConfig();
		saveConfig();
		reloadConfig();
		
		defaultworld = getServer().getWorlds().get(0);
		defaultlocation = new Location(defaultworld, 0, 0, 0);
		if (!version.isLegacy() && !version.equals(MCVersion.V1_13) && !version.equals(MCVersion.V1_13_1)) {
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
		exemptBlocks.add("SMITHING_TABLE");
		
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
				if (!version.equals("latest")) {
					Updater.sendUpdateMessage(Bukkit.getConsoleSender(), version);
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.hasPermission("interactionvisualizer.update")) {
							Updater.sendUpdateMessage(player, version);
						}
					}
				}
			}
		}, 100);
		/*
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
			int[] entityIdArray = PacketManager.active.keySet().stream().mapToInt(each -> each.getEntityId()).toArray();
			
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
	
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		config = getConfig();
		
		itemStandEnabled = config.getBoolean("Modules.ItemStand.Enabled");
		itemDropEnabled = config.getBoolean("Modules.ItemDrop.Enabled");
		hologramsEnabled = config.getBoolean("Modules.Hologram.Enabled");
		
		furnaceChecking = config.getInt("Blocks.Furnace.CheckingPeriod");
		blastfurnaceChecking = config.getInt("Blocks.BlastFurnace.CheckingPeriod");
		smokerChecking = config.getInt("Blocks.Smoker.CheckingPeriod");
		brewingstandChecking = config.getInt("Blocks.BrewingStand.CheckingPeriod");
		beaconChecking = config.getInt("Blocks.Beacon.CheckingPeriod");
		jukeboxChecking = config.getInt("Blocks.JukeBox.CheckingPeriod");
		
		playerPickupYOffset = config.getDouble("Settings.PickupAnimationPlayerYOffset");
		
		gcPeriod = config.getInt("GarbageCollector.Period");
		
		tileEntityChunkPerTick = config.getInt("TileEntityUpdate.ChunksPerTick");
		loadTileEntitiesAsync = config.getBoolean("TileEntityUpdate.LoadTileEntitiesAsync");
		
		handMovementEnabled = config.getBoolean("Settings.UseHandSwingAnimation");
		
		lightUpdatePeriod = config.getInt("LightUpdate.Period");
		
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
