package com.loohp.interactionvisualizer.Utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import com.loohp.interactionvisualizer.Managers.MaterialManager;

public class MaterialUtils {
	
	private static Set<Material> tools = new HashSet<Material>();
	private static Set<Material> standing = new HashSet<Material>();
	private static Set<Material> lowblocks = new HashSet<Material>();
	private static Set<Material> blockexceptions = new HashSet<Material>();
	private static Set<Material> nonSolid = new HashSet<Material>();
	
	public enum MaterialMode {
		TOOL("Tool"),
		STANDING("Standing"),
		LOWBLOCK("LowBlock"),
		ITEM("Item"),
		BLOCK("Block");
		
		String name;
		
		MaterialMode (String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public static MaterialMode getModeFromName(String name) {
			for (MaterialMode mode : MaterialMode.values()) {
				if (mode.toString().equalsIgnoreCase(name)) {
					return mode;
				}
			}
			return null;
		}
	}

	public static void setup() {
		tools.clear();
		blockexceptions.clear();
		standing.clear();
		lowblocks.clear();
		nonSolid.clear();
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("Tools")) {
			try {tools.add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("BlockExceptions")) {
			try {blockexceptions.add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("Standing")) {
			try {standing.add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (String material : MaterialManager.getMaterialConfig().getStringList("LowBlocks")) {
			try {lowblocks.add(Material.valueOf(material));} catch (Exception e) {}
		}
		
		for (Material material : Material.values()) {
			if (!material.isBlock()) {
				continue;
			}
			if (!material.isSolid()) {
				nonSolid.add(material);
			}
		}
	}
	
	public static MaterialMode getMaterialType(Material material) {
		if (tools.contains(material)) {
			return MaterialMode.TOOL;
		}
		if (standing.contains(material)) {
			return MaterialMode.STANDING;
		}
		if (lowblocks.contains(material)) {
			return MaterialMode.LOWBLOCK;
		}
		if (blockexceptions.contains(material)) {
			return MaterialMode.ITEM;
		}
		if (material.isBlock()) {
			return MaterialMode.BLOCK;
		}
		return MaterialMode.ITEM;
	}

	public static Set<Material> getNonSolidSet() {
		return nonSolid;
	} 

}
