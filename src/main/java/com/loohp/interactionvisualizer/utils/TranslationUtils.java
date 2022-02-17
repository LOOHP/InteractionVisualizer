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

import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TranslationUtils {

    private static Method bukkitEnchantmentGetIdMethod;
    private static Class<?> nmsEnchantmentClass;
    private static Method getEnchantmentByIdMethod;
    private static Method getEnchantmentKeyMethod;
    private static Class<?> nmsMobEffectListClass;
    private static Field nmsMobEffectByIdField;
    private static Method getEffectFromIdMethod;
    private static Method getEffectKeyMethod;

    static {
        if (InteractionVisualizer.version.isLegacy()) {
            try {
                bukkitEnchantmentGetIdMethod = Enchantment.class.getMethod("getId");
                nmsEnchantmentClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Enchantment", "net.minecraft.world.item.enchantment.Enchantment");
                getEnchantmentByIdMethod = nmsEnchantmentClass.getMethod("c", int.class);
                getEnchantmentKeyMethod = nmsEnchantmentClass.getMethod("a");
                nmsMobEffectListClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MobEffectList", "net.minecraft.world.effect.MobEffectList");
                if (InteractionVisualizer.version.isOlderOrEqualTo(MCVersion.V1_8_4)) {
                    nmsMobEffectByIdField = nmsMobEffectListClass.getField("byId");
                } else {
                    getEffectFromIdMethod = nmsMobEffectListClass.getMethod("fromId", int.class);
                }
                getEffectKeyMethod = nmsMobEffectListClass.getMethod("a");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                nmsMobEffectListClass = NMSUtils.getNMSClass("net.minecraft.server.%s.MobEffectList", "net.minecraft.world.effect.MobEffectList");
                try {
                    getEffectFromIdMethod = nmsMobEffectListClass.getMethod("fromId", int.class);
                } catch (Exception e) {
                    try {
                        getEffectFromIdMethod = nmsMobEffectListClass.getMethod("byId", int.class);
                    } catch (Exception e1) {
                        getEffectFromIdMethod = nmsMobEffectListClass.getMethod("a", int.class);
                    }
                }
                getEffectKeyMethod = nmsMobEffectListClass.getMethod("c");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("deprecation")
    public static String getEffect(PotionEffectType type) {
        if (!InteractionVisualizer.version.isLegacy()) {
            try {
                int id = type.getId();
                Object nmsMobEffectListObject = getEffectFromIdMethod.invoke(null, id);
                if (nmsMobEffectListObject != null) {
                    return getEffectKeyMethod.invoke(nmsMobEffectListObject).toString();
                } else {
                    return "";
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                return "";
            }
        } else {
            try {
                int id = type.getId();
                Object nmsMobEffectListObject;
                if (InteractionVisualizer.version.isOlderOrEqualTo(MCVersion.V1_8_4)) {
                    Object nmsMobEffectListArray = nmsMobEffectByIdField.get(null);
                    if (Array.getLength(nmsMobEffectListArray) > id) {
                        nmsMobEffectListObject = Array.get(nmsMobEffectListArray, id);
                    } else {
                        return "";
                    }
                } else {
                    nmsMobEffectListObject = getEffectFromIdMethod.invoke(null, id);
                }
                if (nmsMobEffectListObject != null) {
                    String str = getEffectKeyMethod.invoke(nmsMobEffectListObject).toString();
                    return "effect." + str.substring(str.indexOf(".") + 1);
                } else {
                    return "";
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    public static String getEnchantment(Enchantment enchantment) {
        if (!InteractionVisualizer.version.isLegacy()) {
            return "enchantment." + enchantment.getKey().getNamespace() + "." + enchantment.getKey().getKey();
        } else {
            try {
                Object nmsEnchantmentObject = getEnchantmentByIdMethod.invoke(null, bukkitEnchantmentGetIdMethod.invoke(enchantment));
                if (nmsEnchantmentObject != null) {
                    return getEnchantmentKeyMethod.invoke(nmsEnchantmentObject).toString();
                } else {
                    return "";
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
                return "";
            }
        }
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
        type = type.toLowerCase();
        if (!InteractionVisualizer.version.isLegacy()) {
            return "item.minecraft." + type + ".desc";
        } else {
            return "item.record." + type.substring(type.indexOf("MUSIC_DISC_") + 11) + ".desc";
        }
    }

}
