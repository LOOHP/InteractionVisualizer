package com.loohp.interactionvisualizer.managers;

import java.io.File;

import org.simpleyaml.configuration.file.FileConfiguration;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.config.Config;

public class MusicManager {

	public static final String MUSIC_CONFIG_ID = "music";

	public static void setup() {
		if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
			InteractionVisualizer.plugin.getDataFolder().mkdir();
		}
		Config.loadConfig(MUSIC_CONFIG_ID, new File(InteractionVisualizer.plugin.getDataFolder(), "music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), true);
	}

	public static FileConfiguration getMusicConfig() {
		return Config.getConfig(MUSIC_CONFIG_ID).getConfiguration();
	}

	public static void saveConfig() {
		Config.getConfig(MUSIC_CONFIG_ID).save();
	}

	public static void reloadConfig() {
		Config.getConfig(MUSIC_CONFIG_ID).reload();
	}
}
