package com.loohp.interactionvisualizer.placeholderAPI;

import org.bukkit.OfflinePlayer;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;

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
		return "2.0.0";
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
  
        if (identifier.startsWith("itemstand_")) {
        	EntryKey entry = new EntryKey(identifier.substring(10));
        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
	    		if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMSTAND, entry)) {
	    			return "enabled";
	    		}
	    		return "disabled";
        	}
        	return "invalid";
        }

        if (identifier.startsWith("itemdrop_")) {
        	EntryKey entry = new EntryKey(identifier.substring(9));
        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
	        	if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMDROP, entry)) {
	    			return "enabled";
	    		}
	    		return "disabled";
        	}
        	return "invalid";
        }
        
        if (identifier.startsWith("hologram_")) {
        	EntryKey entry = new EntryKey(identifier.substring(9));
        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
	        	if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.HOLOGRAM, entry)) {
	    			return "enabled";
	    		}
	    		return "disabled";
        	}
        	return "invalid";
        }

        return null;
    }

}
