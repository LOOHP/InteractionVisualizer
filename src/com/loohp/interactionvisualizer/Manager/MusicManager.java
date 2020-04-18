package com.loohp.interactionvisualizer.Manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.loohp.interactionvisualizer.InteractionVisualizer;

import net.md_5.bungee.api.ChatColor;

public class MusicManager {

	public static FileConfiguration config;
	public static File file;

	public static void setup() {
		if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
			InteractionVisualizer.plugin.getDataFolder().mkdir();
		}
		file = new File(InteractionVisualizer.plugin.getDataFolder(), "music.yml");
		if (!file.exists()) {
			try {
				InputStream in = InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml");
	            Files.copy(in, file.toPath());
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "The music.yml file has been created");
			} catch (IOException e) {
				Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not create the music.yml file");
			}
		}
        
        config = YamlConfiguration.loadConfiguration(file);
        saveConfig();
	}

	public static FileConfiguration getMusicConfig() {
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
	}
}