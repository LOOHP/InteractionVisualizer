/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.utils;

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
