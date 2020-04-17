package com.loohp.interactionvisualizer.Manager;

import java.io.File;
import java.io.IOException;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.JarUtils;

public class LangManager {
	
	public static File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Lang");
	
	public static void generate() {
		try {
			if (LangFolder.exists()) {
				File[] files = LangFolder.listFiles();
				for (File file : files) {
					if (!file.isDirectory()) {
						file.delete();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			JarUtils.copyFolderFromJar("Lang", InteractionVisualizer.plugin.getDataFolder(), JarUtils.CopyOption.REPLACE_IF_EXIST);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
