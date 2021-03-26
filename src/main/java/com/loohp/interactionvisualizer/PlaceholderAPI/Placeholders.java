package com.loohp.interactionvisualizer.PlaceholderAPI;

import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
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
    public boolean persist() {
        return true;
    }
    
    @Override
    public String getRequiredPlugin() {
        return InteractionVisualizer.plugin.getName();
    }
	
	@Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier){
  
        if (identifier.equals("itemstand")) {
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND).contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	Map<Modules, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get(Modules.ITEMSTAND)) {
        		return "enabled";
        	}
        	return "disabled";
        }

        if (identifier.equals("itemdrop")) {
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP).contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	Map<Modules, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get(Modules.ITEMDROP)) {
        		return "enabled";
        	}
        	return "disabled";
        }
        
        if (identifier.equals("hologram")) {
        	if (offlineplayer.isOnline()) {
        		Player player = offlineplayer.getPlayer();
        		if (InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM).contains(player)) {
        			return "enabled";
        		}
        		return "disabled";
        	}
        	Map<Modules, Boolean> map = Database.getPlayerInfo(offlineplayer.getUniqueId());
        	if (map.get(Modules.HOLOGRAM)) {
        		return "enabled";
        	}
        	return "disabled";
        }

        return null;
    }

}
