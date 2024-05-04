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

public class LegacyInstrumentUtils {

    private static final HashMap<String, String> mapping = new HashMap<>();

    static {
        mapping.put("WOOD", "BASS_GUITAR");
        mapping.put("LOG", "BASS_GUITAR");
        mapping.put("LOG_2", "BASS_GUITAR");
        mapping.put("NOTE_BLOCK", "BASS_GUITAR");
        mapping.put("BOOKSHELF", "BASS_GUITAR");
        mapping.put("ACACIA_STAIRS", "BASS_GUITAR");
        mapping.put("BIRCH_WOOD_STAIRS", "BASS_GUITAR");
        mapping.put("DARK_OAK_STAIRS", "BASS_GUITAR");
        mapping.put("JUNGLE_WOOD_STAIRS", "BASS_GUITAR");
        mapping.put("SPRUCE_WOOD_STAIRS", "BASS_GUITAR");
        mapping.put("WOOD_STAIRS", "BASS_GUITAR");
        mapping.put("CHEST", "BASS_GUITAR");
        mapping.put("WORKBENCH", "BASS_GUITAR");
        mapping.put("SIGN", "BASS_GUITAR");
        mapping.put("SIGN_POST", "BASS_GUITAR");
        mapping.put("WALL_SIGN", "BASS_GUITAR");
        mapping.put("ACACIA_DOOR", "BASS_GUITAR");
        mapping.put("BIRCH_DOOR", "BASS_GUITAR");
        mapping.put("DARK_OAK_DOOR", "BASS_GUITAR");
        mapping.put("JUNGLE_DOOR", "BASS_GUITAR");
        mapping.put("SPRUCE_DOOR", "BASS_GUITAR");
        mapping.put("WOOD_DOOR", "BASS_GUITAR");
        mapping.put("WOOD_PLATE", "BASS_GUITAR");
        mapping.put("JUKEBOX", "BASS_GUITAR");
        mapping.put("ACACIA_FENCE", "BASS_GUITAR");
        mapping.put("BIRCH_FENCE", "BASS_GUITAR");
        mapping.put("DARK_OAK_FENCE", "BASS_GUITAR");
        mapping.put("FENCE", "BASS_GUITAR");
        mapping.put("JUNGLE_FENCE", "BASS_GUITAR");
        mapping.put("SPRUCE_FENCE", "BASS_GUITAR");
        mapping.put("TRAP_DOOR", "BASS_GUITAR");
        mapping.put("BROWN_MUSHROOM", "BASS_GUITAR");
        mapping.put("HUGE_MUSHROOM_1", "BASS_GUITAR");
        mapping.put("HUGE_MUSHROOM_2", "BASS_GUITAR");
        mapping.put("RED_MUSHROOM", "BASS_GUITAR");
        mapping.put("ACACIA_FENCE_GATE", "BASS_GUITAR");
        mapping.put("BIRCH_FENCE_GATE", "BASS_GUITAR");
        mapping.put("DARK_OAK_FENCE_GATE", "BASS_GUITAR");
        mapping.put("FENCE_GATE", "BASS_GUITAR");
        mapping.put("JUNGLE_FENCE_GATE", "BASS_GUITAR");
        mapping.put("SPRUCE_FENCE_GATE", "BASS_GUITAR");
        mapping.put("TRAPPED_CHEST", "BASS_GUITAR");
        mapping.put("DAYLIGHT_DETECTOR", "BASS_GUITAR");
        mapping.put("DAYLIGHT_DETECTOR_INVERTED", "BASS_GUITAR");
        mapping.put("BANNER", "BASS_GUITAR");
        mapping.put("STANDING_BANNER", "BASS_GUITAR");
        mapping.put("WALL_BANNER", "BASS_GUITAR");
        mapping.put("WOOD_STEP", "BASS_GUITAR");
        mapping.put("WOOD_DOUBLE_STEP", "BASS_GUITAR");
        mapping.put("BARRIER", "BASS_GUITAR");
        mapping.put("SAND", "SNARE_DRUM");
        mapping.put("CONCRETE_POWDER", "SNARE_DRUM");
        mapping.put("GRAVEL", "SNARE_DRUM");
        mapping.put("GLASS", "STICKS");
        mapping.put("STAINED_GLASS", "STICKS");
        mapping.put("THIN_GLASS", "STICKS");
        mapping.put("STAINED_GLASS_PANE", "STICKS");
        mapping.put("GLOWSTONE", "STICKS");
        mapping.put("BEACON", "STICKS");
        mapping.put("SEA_LANTERN", "STICKS");
        mapping.put("STONE", "BASS_DRUM");
        mapping.put("COBBLESTONE", "BASS_DRUM");
        mapping.put("BEDROCK", "BASS_DRUM");
        mapping.put("COAL_ORE", "BASS_DRUM");
        mapping.put("DIAMOND_ORE", "BASS_DRUM");
        mapping.put("EMERALD_ORE", "BASS_DRUM");
        mapping.put("GLOWING_REDSTONE_ORE", "BASS_DRUM");
        mapping.put("GOLD_ORE", "BASS_DRUM");
        mapping.put("IRON_ORE", "BASS_DRUM");
        mapping.put("LAPIS_ORE", "BASS_DRUM");
        mapping.put("QUARTZ_ORE", "BASS_DRUM");
        mapping.put("REDSTONE_ORE", "BASS_DRUM");
        mapping.put("DISPENSER", "BASS_DRUM");
        mapping.put("SANDSTONE", "BASS_DRUM");
        mapping.put("RED_SANDSTONE", "BASS_DRUM");
        mapping.put("BRICK", "BASS_DRUM");
        mapping.put("MOSSY_COBBLESTONE", "BASS_DRUM");
        mapping.put("OBSIDIAN", "BASS_DRUM");
        mapping.put("MOB_SPAWNER", "BASS_DRUM");
        mapping.put("STONE", "BASS_DRUM");
        mapping.put("FURNACE", "BASS_DRUM");
        mapping.put("BURNING_FURNACE", "BASS_DRUM");
        mapping.put("SMOOTH_STAIRS", "BASS_DRUM");
        mapping.put("COBBLESTONE_STAIRS", "BASS_DRUM");
        mapping.put("NETHER_BRICK_STAIRS", "BASS_DRUM");
        mapping.put("STONE_PLATE", "BASS_DRUM");
        mapping.put("NETHERRACK", "BASS_DRUM");
        mapping.put("SMOOTH_BRICK", "BASS_DRUM");
        mapping.put("NETHER_BRICK", "BASS_DRUM");
        mapping.put("NETHER_FENCE", "BASS_DRUM");
        mapping.put("ENCHANTED_BOOK", "BASS_DRUM");
        mapping.put("ENDER_PORTAL_FRAME", "BASS_DRUM");
        mapping.put("ENDER_STONE", "BASS_DRUM");
        mapping.put("ENDER_CHEST", "BASS_DRUM");
        mapping.put("COBBLE_WALL", "BASS_DRUM");
        mapping.put("QUARTZ_BLOCK", "BASS_DRUM");
        mapping.put("DROPPER", "BASS_DRUM");
        mapping.put("BLACK_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("BLUE_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("BROWN_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("CYAN_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("GRAY_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("GREEN_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("LIGHT_BLUE_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("LIME_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("MAGENTA_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("ORANGE_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("PINK_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("PURPLE_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("RED_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("SILVER_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("WHITE_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("YELLOW_GLAZED_TERRACOTTA", "BASS_DRUM");
        mapping.put("HARD_CLAY", "BASS_DRUM");
        mapping.put("STAINED_CLAY", "BASS_DRUM");
        mapping.put("PRISMARINE", "BASS_DRUM");
        mapping.put("DOUBLE_STONE_SLAB2", "BASS_DRUM");
        mapping.put("PURPUR_DOUBLE_SLAB", "BASS_DRUM");
        mapping.put("PURPUR_SLAB", "BASS_DRUM");
        mapping.put("STONE_SLAB2", "BASS_DRUM");
        mapping.put("STEP", "BASS_DRUM");
        mapping.put("DOUBLE_STEP", "BASS_DRUM");
        mapping.put("COAL_BLOCK", "BASS_DRUM");
        mapping.put("PURPUR_BLOCK", "BASS_DRUM");
        mapping.put("PURPUR_PILLAR", "BASS_DRUM");
        mapping.put("PURPUR_STAIRS", "BASS_DRUM");
        mapping.put("END_BRICKS", "BASS_DRUM");
        mapping.put("MAGMA", "BASS_DRUM");
        mapping.put("RED_NETHER_BRICK", "BASS_DRUM");
        mapping.put("OBSERVER", "BASS_DRUM");
        mapping.put("CONCRETE", "BASS_DRUM");
        mapping.put("GOLD_BLOCK", "BELL");
        mapping.put("CLAY", "FLUTE");
        mapping.put("PACKED_ICE", "CHIME");
        mapping.put("WOOL", "GUITAR");
        mapping.put("BONE_BLOCK", "XYLOPHONE");
        mapping.put("IRON_BLOCK", "IRON_XYLOPHONE");
        mapping.put("SOUL_SAND", "COW_BELL");
        mapping.put("PUMPKIN", "DIDGERIDOO");
        mapping.put("EMERALD_BLOCK", "BIT");
        mapping.put("HAY_BLOCK", "BANJO");
        mapping.put("GLOWSTONE", "PLING");
    }

    public static String getInstrumentNameFromLegacy(String material) {
        String inst = mapping.get(material);
        return inst != null ? inst : "PIANO";
    }

}
