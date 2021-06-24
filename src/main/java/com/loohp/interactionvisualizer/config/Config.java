package com.loohp.interactionvisualizer.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;

import com.loohp.interactionvisualizer.utils.FileUtils;

public class Config {
	
	private static final Map<String, Config> CONFIGS = new HashMap<>();
	
	public static Config getConfig(String id) {
		return CONFIGS.get(id);
	}
	
	public static void reloadConfigs() {
		for (Config config : CONFIGS.values()) {
			config.reload();
		}
	}
	
	public static void saveConfigs() {
		for (Config config : CONFIGS.values()) {
			config.save();
		}
	}
	
	public static Config loadConfig(String id, File file, InputStream ifNotFound, InputStream def, CommentType... refreshCommentType) {
		try {
			if (CONFIGS.containsKey(id)) {
				throw new IllegalArgumentException("Duplicate config id");
			}
			
			if (!file.exists()) {
				FileUtils.copy(ifNotFound, file);
			}
			
			Config config = new Config(file, def, refreshCommentType);
			CONFIGS.put(id, config);
			return config;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Config loadConfig(String id, File file) {
		if (getConfig(id) != null) {
			throw new IllegalArgumentException("Duplicate config id");
		}
		if (!file.exists()) {
			try (PrintWriter pw = new PrintWriter(file)) {
				pw.println();
				pw.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		Config config = new Config(file);
		CONFIGS.put(id, config);
		return config;
	}
	
	public static boolean unloadConfig(String id, boolean save) {
		Config config = CONFIGS.remove(id);
		if (config != null) {
			if (save) {
				config.save();
			}
			return true;
		} else {
			return false;
		}
	}
	
	private File file;
	private YamlFile defConfig;
	private YamlFile config;
	
	private Config(File file, InputStream def, CommentType... refreshCommentType) {
		this.file = file;
		
		defConfig = YamlFile.loadConfiguration(def, true);
		config = YamlFile.loadConfiguration(file, true);
		
		for (String path : defConfig.getValues(true).keySet()) {
			if (config.contains(path)) {
				for (CommentType commentType : refreshCommentType) {
					config.setComment(path, defConfig.getComment(path, commentType), commentType);
				}
			} else if (!defConfig.isConfigurationSection(path)) {
				config.set(path, defConfig.get(path));
				for (CommentType commentType : CommentType.values()) {
					config.setComment(path, defConfig.getComment(path, commentType), commentType);
				}
			}
		}
		
		save();
	}
	
	private Config(File file) {
		config = YamlFile.loadConfiguration(file, true);
		save();
	}
	
	public void save() {
		try {
			config.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reload() {
		config = YamlFile.loadConfiguration(file, true);
	}
	
	public YamlFile getConfiguration() {
		return config;
	}

}
