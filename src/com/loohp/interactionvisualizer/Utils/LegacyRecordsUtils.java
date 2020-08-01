package com.loohp.interactionvisualizer.Utils;

import java.util.HashMap;

public class LegacyRecordsUtils {
	
	public static HashMap<String, String> mapping = new HashMap<String, String>();
	
	static {
		mapping.put("GOLD_RECORD", "MUSIC_DISC_13");
		mapping.put("GREEN_RECORD", "MUSIC_DISC_CAT");
		mapping.put("RECORD_3", "MUSIC_DISC_BLOCKS");
		mapping.put("RECORD_4", "MUSIC_DISC_CHIRP");
		mapping.put("RECORD_5", "MUSIC_DISC_FAR");
		mapping.put("RECORD_6", "MUSIC_DISC_MALL");
		mapping.put("RECORD_7", "MUSIC_DISC_MELLOHI");
		mapping.put("RECORD_8", "MUSIC_DISC_STAL");
		mapping.put("RECORD_9", "MUSIC_DISC_STRAD");
		mapping.put("RECORD_10", "MUSIC_DISC_WARD");
		mapping.put("RECORD_11", "MUSIC_DISC_11");
		mapping.put("RECORD_12", "MUSIC_DISC_WAIT");
	}

	public static String translateFromLegacy(String legacyName) {
		return mapping.get(legacyName);
	}
}
