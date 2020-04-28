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
	
	public static String getMaterialType(Material material) {
		if (tools.contains(material)) {
			return "Tool";
		}
		if (standing.contains(material)) {
			return "Standing";
		}
		if (lowblocks.contains(material)) {
			return "LowBlock";
		}
		if (blockexceptions.contains(material)) {
			return "Item";
		}
		if (material.isBlock()) {
			return "Block";
		}
		return "Item";
	}

	public static Set<Material> getNonSolidSet() {
		return nonSolid;
	} 

}
