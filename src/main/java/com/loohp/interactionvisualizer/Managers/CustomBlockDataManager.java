package com.loohp.interactionvisualizer.Managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loohp.interactionvisualizer.InteractionVisualizer;

import net.md_5.bungee.api.ChatColor;

public class CustomBlockDataManager {

	private static File file;
    private static JSONObject json;
    private static JSONParser parser = new JSONParser();
    private static Plugin plugin = InteractionVisualizer.plugin;
    private static File blockDataBackupFolder = new File(InteractionVisualizer.plugin.getDataFolder().getPath() + "/Backup", "blockdata");
    
    public static void intervalSaveToFile() {
    	Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
    		save();
    	}, 200, 1200);
    }

    public synchronized static void setup() {
        try {
        	if (!plugin.getDataFolder().exists()) {
        		plugin.getDataFolder().mkdir();
    		}
    		file = new File(plugin.getDataFolder(), "blockdata.json");
        	if (!file.exists()) {
        	    PrintWriter pw = new PrintWriter(file, "UTF-8");
        	    pw.print("{");
        	    pw.print("}");
        	    pw.flush();
        	    pw.close();
        	} else {
        		String fileName = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'_blockdata.json'").format(new Date()).replace(":", ";");
        		blockDataBackupFolder.mkdirs();
                File outputfile = new File(blockDataBackupFolder, fileName);
                try (InputStream in = new FileInputStream(file)) {
                    Files.copy(in, outputfile.toPath());
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] Failed to make backup for blockdata.json");
                }
        	}
        	if (blockDataBackupFolder.exists()) {
        		for (File file : blockDataBackupFolder.listFiles()) {
        			try {
	        			String fileName = file.getName();
	        			if (fileName.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}_.*_blockdata\\.json$")) {
	        				Date timestamp = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'_blockdata.json'").parse(fileName.replace(";", ":"));
	        				if ((System.currentTimeMillis() - timestamp.getTime()) > 2592000000L) {
								Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractionVisualizer] Removing Backup/blockdata/" + fileName + " as it is from 30 days ago.");
								file.delete();						
							}
	        			}
        			} catch (Exception ignore) {}
        		}
        	}
        	InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        	json = (JSONObject) parser.parse(reader);
        	reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
	public synchronized static boolean save() {
        try {
        	JSONObject toSave = json;
        
        	TreeMap<String, Object> treeMap = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        	treeMap.putAll(toSave);
        	
        	Gson g = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = g.toJson(treeMap);
            
            PrintWriter clear = new PrintWriter(file);
            clear.print("");
            clear.close();
            
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(prettyJsonString);
            writer.flush();
            writer.close();

            return true;
        } catch (Exception e) {
        	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] Error while saving blockdata.json, retrying..");
        	setup();
        	return false;
        }
    }
    
    public static JSONObject getJsonObject() {
    	return json;
    }
   
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getBlock(String key) {   
    	Object obj = json.get(key);
    	if (obj == null) {
    		return null;
    	}
    	JSONObject value = (JSONObject) obj;
    	Map<String, Object> map = new HashMap<>();
		Iterator<String> itr = value.keySet().iterator();
    	while (itr.hasNext()) {
    		String keykey = itr.next();
    		map.put(keykey, value.get(keykey));
    	}
    	return map;
    }
    
    @SuppressWarnings("unchecked")
	public static void setBlock(String key, Map<String, Object> map) {
    	Object obj = json.get(key);
    	JSONObject value = (obj != null) ? (JSONObject) obj : new JSONObject();
    	for (Entry<String, Object> entry : map.entrySet()) {
    		value.put(entry.getKey(), entry.getValue());
    	}
    	json.put(key, value);
    }
    
    public static void removeBlock(String key) {
    	json.remove(key);
    }
    
    public static boolean contains(String key) {   	
    	if (json.containsKey(key)) {
    		return true;
    	}
    	return false;
    }
    
	public static String locKey(Location loc) {
		return loc.getWorld().getName() + "_" + loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ();
	}
	
	public static Location keyLoc(String key) {
		String[] breakdown = key.split("_");
		String worldString = "";
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < (breakdown.length - 3); i = i + 1) {
			list.add(breakdown[i]);
		}
		worldString = String.join("_", list);
		World world = Bukkit.getWorld(worldString);
		int x = Integer.parseInt(breakdown[breakdown.length - 3]);
		int y = Integer.parseInt(breakdown[breakdown.length - 2]);
		int z = Integer.parseInt(breakdown[breakdown.length - 1]);
		Location loc = new Location(world, x, y, z);
		return loc;
	}
    
}
