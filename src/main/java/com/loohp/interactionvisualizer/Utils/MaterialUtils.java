package com.loohp.interactionvisualizer.utils;

import java.util.Set;

import org.bukkit.Material;

import com.loohp.interactionvisualizer.managers.MaterialManager;

public class MaterialUtils {
	
	public static enum MaterialMode {
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
	
	public static MaterialMode getMaterialType(Material material) {
		if (MaterialManager.getTools().contains(material)) {
			return MaterialMode.TOOL;
		}
		if (MaterialManager.getStanding().contains(material)) {
			return MaterialMode.STANDING;
		}
		if (MaterialManager.getLowblocks().contains(material)) {
			return MaterialMode.LOWBLOCK;
		}
		if (MaterialManager.getBlockexceptions().contains(material)) {
			return MaterialMode.ITEM;
		}
		if (material.isBlock()) {
			return MaterialMode.BLOCK;
		}
		return MaterialMode.ITEM;
	}

	public static Set<Material> getNonSolidSet() {
		return MaterialManager.getNonSolid();
	} 

}
