package com.loohp.interactionvisualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.Manager.CustomBlockDataManager;
import com.loohp.interactionvisualizer.Manager.EffectManager;
import com.loohp.interactionvisualizer.Manager.EnchantmentManager;
import com.loohp.interactionvisualizer.Manager.LangManager;
import com.loohp.interactionvisualizer.Manager.MusicManager;
import com.loohp.interactionvisualizer.Manager.PlayerRangeManager;
import com.loohp.interactionvisualizer.Manager.TaskManager;
import com.loohp.interactionvisualizer.Manager.TileEntityManager;
import com.loohp.interactionvisualizer.Metrics.Charts;
import com.loohp.interactionvisualizer.Metrics.Metrics;
import com.loohp.interactionvisualizer.PlaceholderAPI.PlaceholderAPI;
import com.loohp.interactionvisualizer.Updater.Updater;
import com.loohp.interactionvisualizer.Utils.LegacyRecordsUtils;
import com.loohp.interactionvisualizer.Utils.MaterialUtils;

import net.md_5.bungee.api.ChatColor;

public class InteractionVisualizer extends JavaPlugin {
	
	public static Plugin plugin = null;
	public static ProtocolManager protocolManager;
	public static FileConfiguration config;
	
	public static String version = "";
	
	public static boolean openinv = false;
	public static boolean vanish = false;
	public static boolean cmi = false;
	public static boolean ess3 = false;
	
	public static List<Player> itemStand = new CopyOnWriteArrayList<Player>();
	public static List<Player> itemDrop = new CopyOnWriteArrayList<Player>();
	public static List<Player> holograms = new CopyOnWriteArrayList<Player>();
	
	public static List<String> exemptBlocks = new ArrayList<String>();
	
	public static boolean itemStandEnabled = true;
	public static boolean itemDropEnabled = true;
	public static boolean hologramsEnabled = true;
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
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
		LangManager.generate();
		
		int pluginId = 7024;

		Metrics metrics = new Metrics(this, pluginId);
		
		String packageName = getServer().getClass().getPackage().getName();
		
		if (packageName.contains("1_15_R1") == true) {
	    	version = "1.15";
	    } else if (packageName.contains("1_14_R1") == true) {
	    	version = "1.14";
	    } else if (packageName.contains("1_13_R2") == true) {
	    	version = "1.13.1";
	    } else if (packageName.contains("1_13_R1") == true) {
	    	version = "1.13";
	    } else if (packageName.contains("1_12_R1") == true) {
	    	version = "legacy1.12";
	    } else if (packageName.contains("1_11_R1") == true) {
	    	version = "legacy1.11";
	    } else if (packageName.contains("1_10_R1") == true) {
	    	version = "legacy1.10";
	    } else if (packageName.contains("1_9_R2") == true) {
	    	version = "legacy1.9.4";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    	unsupportedMessage();
	    } else if (packageName.contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    	unsupportedMessage();
	    } else {
	    	unsupportedMessage();
	    }
		
		plugin.getConfig().options().copyDefaults(true);
		config = plugin.getConfig();
		plugin.saveConfig();
		loadConfig();
		
		EnchantmentManager.setup();
		EffectManager.setup();
		MusicManager.setup();
		Database.setup();
		CustomBlockDataManager.setup();
		TaskManager.run();
		TileEntityManager.run();
		PlayerRangeManager.run();
		CustomBlockDataManager.intervalSaveToFile();
		
		MaterialUtils.setup();
		
		if (version.contains("legacy")) {
			LegacyRecordsUtils.setup();
		}
		
		getCommand("interactionvisualizer").setExecutor(new Commands());
		
		TaskManager.setup();
		
		Charts.registerCharts(metrics);
		
		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PlaceholderAPI().register();
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
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "InteractionVisualizer has been enabled!");
	}
	
	@Override
	public void onDisable() {
		CustomBlockDataManager.save();
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
		return new ArrayList<Player>(Bukkit.getOnlinePlayers());
	}
	
	private static void unsupportedMessage() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	}
	
	private static void hookMessage(String pluginName) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractionVisualizer] InteractionVisualizer has hooked into " + pluginName + "!");
	}
	
}
