/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.nms.NMS;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

public class TranslationUtils {

    public static String getEffect(PotionEffectType type) {
        return NMS.getInstance().getEffectTranslationKey(type);
    }

    public static String getEnchantment(Enchantment enchantment) {
        return NMS.getInstance().getEnchantmentTranslationKey(enchantment);
    }

    public static String getLevel(int level) {
        if (level == 1) {
            return "container.enchant.level.one";
        } else {
            return "container.enchant.level.many";
        }
    }

    public static String getRecord(String type) {
        if (!type.contains("MUSIC_DISC_")) {
            return null;
        }
        if (InteractionVisualizer.version.isNewerOrEqualTo(MCVersion.V1_21)) {
            return "jukebox_song.minecraft." + type.toLowerCase().substring("music_disc_".length());
        } else {
            return "item.minecraft." + type.toLowerCase() + ".desc";
        }
    }

}
