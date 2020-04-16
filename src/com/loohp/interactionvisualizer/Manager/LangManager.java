package com.loohp.interactionvisualizer.Manager;

import java.io.File;
import java.io.IOException;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.JarUtils;

public class LangManager {
	
	public static File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Lang");
	
	public static void generate() {
		try {
			if (LangFolder.exists()) {
				FileUtils.cleanDirectory(LangFolder);
			}
			
			JarUtils.copyFolderFromJar("Lang", InteractionVisualizer.plugin.getDataFolder(), JarUtils.CopyOption.REPLACE_IF_EXIST);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
