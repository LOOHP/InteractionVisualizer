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
	
	/**
	<b>Only use if this is a native built-in display</b>
	*/
	@Deprecated
	default EntryKey registerNative() {
		return key();
	}

}
