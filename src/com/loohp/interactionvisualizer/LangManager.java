package com.loohp.interactionvisualizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class LangManager {
	
	public static File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Lang");
	
	public static void generate() {
		
		if (!LangFolder.exists()) {
			LangFolder.mkdir();
		}
		
		String DefaultLang = "en_US.yml";
        File file = new File(LangFolder, DefaultLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/" + DefaultLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		String SpanishLang = "es_ES.yml";
        file = new File(LangFolder, SpanishLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/" + SpanishLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String ChineseSimplifiedLang = "zh_CN.yml";
        file = new File(LangFolder, ChineseSimplifiedLang);
        if (file.exists()) {
        	file.delete();
        }
        try (InputStream in = InteractionVisualizer.class.getClassLoader().getResourceAsStream("Lang/" + ChineseSimplifiedLang)) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
