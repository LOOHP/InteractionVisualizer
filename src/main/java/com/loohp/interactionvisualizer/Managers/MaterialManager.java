package com.loohp.interactionvisualizer.Managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.loohp.interactionvisualizer.InteractionVisualizer;

import net.md_5.bungee.api.ChatColor;

public class MaterialManager {

	public static FileConfiguration config;
	public static File file;
	
	private static Set<Material> tools = new HashSet<Material>();
	private static Set<Material> standing = new HashSet<Material>();
	private static Set<Material> lowblocks = new HashSet<Material>();
	private static Set<Material> blockexceptions = new HashSet<Material>();
	private static Set<Material> nonSolid = new HashSet<Material>();

	public static void setup() {
		if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
			InteractionVisualizer.plugin.getDataFolder().mkdir();
		}
		file = new File(InteractionVisualizer.plugin.getDataFolder(), "material.yml");
		if (!file.exists()) {
			try {
				InputStream in = InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("material.yml");
	            Files.copy(in, file.toPath());
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "The material.yml file has been created");
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not create the material.yml file");
			}
		}
        
        config = YamlConfiguration.loadConfiguration(file);
        reload();
        saveConfig();
	}

	public static FileConfiguration getMaterialConfig() {
		return config;
	}

	public static void saveConfig() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(file);
		reload();
	}
	
	public static void reload() {
		getTools().clear();
		getBlockexceptions().clear();
		getStanding().clear();
		getLowblocks().clear();
		getNonSolid().clear();
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("Tools")) {
			try {getTools().add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("BlockExceptions")) {
			try {getBlockexceptions().add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("Standing")) {
			try {getStanding().add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("LowBlocks")) {
			try {getLowblocks().add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (Material material : Material.values()) {
			if (!material.isBlock()) {
				continue;
			}
			if (!material.isSolid()) {
				getNonSolid().add(material);
			}
		}
	}

	public static Set<Material> getTools() {
		return tools;
	}

	public static void setTools(Set<Material> tools) {
		MaterialManager.tools = tools;
	}

	public static Set<Material> getStanding() {
		return standing;
	}

	public static void setStanding(Set<Material> standing) {
		MaterialManager.standing = standing;
	}

	public static Set<Material> getLowblocks() {
		return lowblocks;
	}

	public static void setLowblocks(Set<Material> lowblocks) {
		MaterialManager.lowblocks = lowblocks;
	}

	public static Set<Material> getBlockexceptions() {
		return blockexceptions;
	}

	public static void setBlockexceptions(Set<Material> blockexceptions) {
		MaterialManager.blockexceptions = blockexceptions;
	}

	public static Set<Material> getNonSolid() {
		return nonSolid;
	}

	public static void setNonSolid(Set<Material> nonSolid) {
		MaterialManager.nonSolid = nonSolid;
	}
}
