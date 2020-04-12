package com.loohp.interactionvisualizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class LangManager {
	
	public static File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Lang");
	public static File LangConfigFolder = new File(LangFolder, "Config");
	public static File LangEnchFolder = new File(LangFolder, "Enchantment");
	
	public static void generate() {
		
		if (LangFolder.exists()) {
			File[] files = LangFolder.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				}
			}
		}
		LangFolder.mkdir();
		if (!LangConfigFolder.exists()) {
			LangConfigFolder.mkdir();
		}
		if (!LangEnchFolder.exists()) {
			LangEnchFolder.mkdir();
		}
		
		String DefaultLang = "en_US.yml";
        File file = new File(LangConfigFolder, DefaultLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Config/" + DefaultLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(LangEnchFolder, DefaultLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Enchantment/" + DefaultLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		String SpanishLang = "es_ES.yml";
        file = new File(LangConfigFolder, SpanishLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Config/" + SpanishLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String ChineseSimplifiedLang = "zh_CN.yml";
        file = new File(LangConfigFolder, ChineseSimplifiedLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Config/" + ChineseSimplifiedLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(LangEnchFolder, ChineseSimplifiedLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Enchantment/" + ChineseSimplifiedLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String FrenchLang = "fr_FR.yml";
        file = new File(LangEnchFolder, FrenchLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/Enchantment/" + FrenchLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
