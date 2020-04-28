package com.loohp.interactionvisualizer.PlaceholderAPI;

import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Database.Database;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Placeholders extends PlaceholderExpansion {

	@Override
	public String getAuthor() {
		return String.join(", ", InteractionVisualizer.plugin.getDescription().getAuthors());
	}

	@Override
	public String getIdentifier() {
		return "interactionvisualizer";
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier){
  
        if(identifier.equals("itemstand")){
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizer.itemStand.contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	HashMap<String, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get("itemstand")) {
        		return "enabled";
        	}
        	return "disabled";
        }

        if(identifier.equals("itemdrop")){
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizer.itemDrop.contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	HashMap<String, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get("itemdrop")) {
        		return "enabled";
        	}
        	return "disabled";
        }
        
        if(identifier.equals("hologram")){
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizer.holograms.contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	HashMap<String, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get("hologram")) {
        		return "enabled";
        	}
        	return "disabled";
        }

        return null;
    }

}
