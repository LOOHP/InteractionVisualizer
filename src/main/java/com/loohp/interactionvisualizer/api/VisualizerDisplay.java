package com.loohp.interactionvisualizer.api;

import com.loohp.interactionvisualizer.objectholders.EntryKey;

public interface VisualizerDisplay {
	
	/**
	Return an EntryKey to identify this display type
	*/
	public EntryKey key();
	
	default void register() {
		InteractionVisualizerAPI.getPreferenceManager().registerEntry(key());
	}

}
