package com.loohp.interactionvisualizer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.Utils.Updater;

import net.md_5.bungee.api.ChatColor;

public class InteractionVisualizer extends JavaPlugin {
	
	public static Plugin plugin = null;
	public static ProtocolManager protocolManager;
	public static Scoreboard scoreboard;
	public static FileConfiguration config;
	
	public static String version = "";
	
	public static List<Player> itemStand = new ArrayList<Player>();
	public static List<Player> itemDrop = new ArrayList<Player>();
	public static List<Player> holograms = new ArrayList<Player>();
	
	public static List<Player> onlinePlayers = new ArrayList<Player>();
	
	public static boolean itemStandEnabled = true;
	public static boolean itemDropEnabled = true;
	public static boolean hologramsEnabled = true;
	
	public static boolean UpdaterEnabled = true;
	public static int UpdaterTaskID = -1;
	
	@Override
	public void onEnable() {
		plugin = getServer().getPluginManager().getPlugin("InteractionVisualizer");
		scoreboard = getServer().getScoreboardManager().getMainScoreboard();
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		plugin.getConfig().options().copyDefaults(true);
		config = plugin.getConfig();
		plugin.saveConfig();
		loadConfig();
		
		Database.setup();
		
		getCommand("interactionvisualizer").setExecutor(new Commands());
		
		if (scoreboard.getTeam("ChestIn") == null) {
			scoreboard.registerNewTeam("ChestIn");			
		}
		scoreboard.getTeam("ChestIn").setColor(org.bukkit.ChatColor.GREEN);
		
		if (scoreboard.getTeam("ChestOut") == null) {
			scoreboard.registerNewTeam("ChestOut");
		}
		scoreboard.getTeam("ChestOut").setColor(org.bukkit.ChatColor.RED);
		
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
	    } else if (getServer().getClass().getPackage().getName().contains("1_9_R1") == true) {
	    	version = "legacy1.9";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R3") == true) {
	    	version = "OLDlegacy1.8.4";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R2") == true) {
	    	version = "OLDlegacy1.8.3";
	    } else if (getServer().getClass().getPackage().getName().contains("1_8_R1") == true) {
	    	version = "OLDlegacy1.8";
	    } else {
	    	getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
	    }
		
		TaskManager.setup();
		TaskManager.load();
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "InteractionVisualizer has been enabled!");
	}
	
	@Override
	public void onDisable() {
		scoreboard.getTeam("ChestIn").unregister();
		scoreboard.getTeam("ChestOut").unregister();
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

}
