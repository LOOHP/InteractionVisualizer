package com.loohp.interactionvisualizer.Manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class LangManager {
	
	public static File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Lang");
	
	public static void generate() {
		String path = "Lang";
		int lastslash = InteractionVisualizer.class.getProtectionDomain().getCodeSource().getLocation().getPath().lastIndexOf("/");
		if (lastslash == -1) {
			lastslash = InteractionVisualizer.class.getProtectionDomain().getCodeSource().getLocation().getPath().lastIndexOf("\\");
		}
		File jarFile = new File(InteractionVisualizer.plugin.getDataFolder().getParent() + "/" + InteractionVisualizer.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(lastslash));
		
		try {
			if (LangFolder.exists()) {
				FileUtils.cleanDirectory(LangFolder);
			}

			JarFile jar = new JarFile(jarFile);
			Enumeration<JarEntry> entries = jar.entries();
			while(entries.hasMoreElements()) {
				String name = entries.nextElement().getName();
			    if (name.matches("^" + path + "[\\/\\\\]..*")) {
			    	InputStream in = InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream(name);
			    	File out = new File(InteractionVisualizer.plugin.getDataFolder().getPath() + "/" + name);
			    	out.getParentFile().mkdirs();
		            Files.copy(in, out.toPath());
			    }
			}
			jar.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
