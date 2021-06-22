package com.loohp.interactionvisualizer.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import com.loohp.interactionvisualizer.InteractionVisualizer;

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
					getEffectFromIdMethod = nmsMobEffectListClass.getMethod("byId", int.class);
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
