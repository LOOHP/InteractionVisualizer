package com.loohp.interactionvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.Metrics.Charts;
import com.loohp.interactionvisualizer.Metrics.Metrics;
import com.loohp.interactionvisualizer.Updater.Updater;
import com.loohp.interactionvisualizer.Utils.MaterialUtils;

import net.md_5.bungee.api.ChatColor;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

public class InteractionVisualizer extends JavaPlugin {
	
	public static Plugin plugin = null;
	public static ProtocolManager protocolManager;
	public static FileConfiguration config;
	
	public static String version = "";
	
	public static boolean openinv = false;
	public static boolean vanish = false;
	public static boolean cmi = false;
	public static boolean ess3 = false;
	
	public static List<Chunk> chunksGoneOver = new ArrayList<Chunk>();
	
	public static List<Player> itemStand = new CopyOnWriteArrayList<Player>();
	public static List<Player> itemDrop = new CopyOnWriteArrayList<Player>();
	public static List<Player> holograms = new CopyOnWriteArrayList<Player>();
	
	public static List<Player> onlinePlayers = new CopyOnWriteArrayList<Player>();
	
	public static boolean itemStandEnabled = true;
	public static boolean itemDropEnabled = true;
	public static boolean hologramsEnabled = true;
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
	public static BlockingQueue<Chunk> chunkupdatequeue = new LinkedBlockingQueue<Chunk>(); 
	
	@Override
	public void onEnable() {
		plugin = getServer().getPluginManager().getPlugin("InteractionVisualizer");
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		if (getServer().getPluginManager().getPlugin("OpenInv") != null) {
			openinv = true;
		}
		if (getServer().getPluginManager().isPluginEnabled("SuperVanish") || getServer().getPluginManager().isPluginEnabled("PremiumVanish")) {
			vanish = true;
		}
		if (getServer().getPluginManager().getPlugin("CMI") != null) {
			cmi = true;
		}
		if (getServer().getPluginManager().getPlugin("Essentials") != null) {
			ess3 = true;
		}
		
		LangManager.generate();
		
		int pluginId = 7024;

		Metrics metrics = new Metrics(this, pluginId);
		
		if (getServer().getClass().getPackage().getName().contains("1_15_R1") == true) {
	    	version = "1.15";
	    } else if (getServer().getClass().getPackage().getName().contains("1_14_R1") == true) {
	    	version = "1.14";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R2") == true) {
	    	version = "1.13.1";
	    } else if (getServer().getClass().getPackage().getName().contains("1_13_R1") == true) {
	    	version = "1.13";
	    } else if (getServer().getClass().getPackage().getName().contains("1_12_R1") == true) {
	    	version = "legacy1.12";
	    } else if (getServer().getClass().getPackage().getName().contains("1_11_R1") == true) {
	    	version = "legacy1.11";
	    } else if (getServer().getClass().getPackage().getName().contains("1_10_R1") == true) {
	    	version = "legacy1.10";
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R2") == true) {
	    	version = "legacy1.9.4";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    }
		
		plugin.getConfig().options().copyDefaults(true);
		config = plugin.getConfig();
		plugin.saveConfig();
		loadConfig();
		
		EnchantmentManager.setup();
		Database.setup();
		
		MaterialUtils.setup();
		
		getCommand("interactionvisualizer").setExecutor(new Commands());
		
		TaskManager.setup();
		TaskManager.load();
		
		Charts.registerCharts(metrics);
		
		for (World world : Bukkit.getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				for (Entity entity : chunk.getEntities()) {
					if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
						LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
						for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
							LightAPI.updateChunk(info, LightType.BLOCK);
						}
						entity.remove();
					}
				}
				chunksGoneOver.add(chunk);
			}
		}
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> chunkQueue(), 100);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "InteractionVisualizer has been enabled!");
	}
	
	@Override
	public void onDisable() {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "InteractionVisualizer has been disabled!");
	}
	
	public static void loadConfig() {
		itemStandEnabled = config.getBoolean("Modules.ItemStand.Enabled");
		itemDropEnabled = config.getBoolean("Modules.ItemDrop.Enabled");
		hologramsEnabled = config.getBoolean("Modules.Hologram.Enabled");
		
		if (UpdaterTaskID >= 0) {
			Bukkit.getScheduler().cancelTask(UpdaterTaskID);
		}
		UpdaterEnabled = plugin.getConfig().getBoolean("Options.Updater");
		if (UpdaterEnabled == true) {
			Updater.updaterInterval();
		}
	}
	
	public static List<Player> getOnlinePlayers() {
		onlinePlayers.clear();
		onlinePlayers.addAll(Bukkit.getOnlinePlayers());
		return onlinePlayers;
	}
	
	public static void chunkQueue() {
		int next = 1;
		if (chunkupdatequeue.size() == 0) {
			return;
		}
		try {
			Chunk chunk = chunkupdatequeue.poll(2, TimeUnit.MILLISECONDS);
			if (chunk != null) {
				if (!chunksGoneOver.contains(chunk)) {
					int delay = 1;
					int count = 0;
					int maxper = (int) Math.ceil((double) chunk.getEntities().length / (double) 5);
					for (Entity eachEntity : chunk.getEntities()) {
						count++;
						if (count > maxper) {
							count = 0;
							delay++;
						}
						UUID uuid = eachEntity.getUniqueId();
						new BukkitRunnable() {
							public void run() {
								if (Bukkit.getEntity(uuid) == null) {
									return;
								}
								Entity entity = Bukkit.getEntity(uuid);
								if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
									LightAPI.deleteLight(entity.getLocation(), LightType.BLOCK, false);
									for (ChunkInfo info : LightAPI.collectChunks(entity.getLocation(), LightType.BLOCK, 15)) {
										LightAPI.updateChunk(info, LightType.BLOCK);
									}
									entity.remove();
								}
							}
						}.runTaskLater(InteractionVisualizer.plugin, delay);
					}
					next = next + delay;
					chunksGoneOver.add(chunk);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> chunkQueue(), next);
	}

}
