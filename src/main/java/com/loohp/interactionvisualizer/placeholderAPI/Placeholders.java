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
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {
  
        if (identifier.startsWith("itemstand_")) {
        	String entryType = identifier.substring(10);
        	if (entryType.equals("all")) {
        		if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMSTAND)) {
        			return "enabled";
        		} else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMSTAND)) {
        			return "partly enabled";
        		}
        		return "disabled";
        	} else {
        		EntryKey entry = new EntryKey(entryType);
	        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
		    		if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMSTAND, entry)) {
		    			return "enabled";
		    		}
		    		return "disabled";
	        	}
        	}
        	return "invalid";
        }

        if (identifier.startsWith("itemdrop_")) {
        	String entryType = identifier.substring(9);
        	if (entryType.equals("all")) {
        		if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMDROP)) {
        			return "enabled";
        		} else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMDROP)) {
        			return "partly enabled";
        		}
        		return "disabled";
        	} else {
	        	EntryKey entry = new EntryKey(entryType);
	        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
		        	if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMDROP, entry)) {
		    			return "enabled";
		    		}
		    		return "disabled";
	        	}
        	}
        	return "invalid";
        }
        
        if (identifier.startsWith("hologram_")) {
        	String entryType = identifier.substring(9);
        	if (entryType.equals("all")) {
        		if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.HOLOGRAM)) {
        			return "enabled";
        		} else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.HOLOGRAM)) {
        			return "partly enabled";
        		}
        		return "disabled";
        	} else {
	        	EntryKey entry = new EntryKey(entryType);
	        	if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
		        	if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.HOLOGRAM, entry)) {
		    			return "enabled";
		    		}
		    		return "disabled";
	        	}
        	}
        	return "invalid";
        }

        return null;
    }

}
