package com.loohp.interactionvisualizer.utils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class EntityUtils {
	
	private static Field entityCountField;
	
	static {
		try {
			Class<?> nmsEntityClass = NMSUtils.getNMSClass("net.minecraft.server.%s.Entity", "net.minecraft.world.entity.Entity");
			try {
				entityCountField = nmsEntityClass.getDeclaredField("entityCount");
			} catch (NoSuchFieldException | SecurityException e) {
				entityCountField = Stream.of(nmsEntityClass.getDeclaredFields()).filter(each -> each.getType().equals(AtomicInteger.class)).findFirst().get();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static int getNextEntityId() {
		try {
			entityCountField.setAccessible(true);
			Object entityCountObject = entityCountField.get(null);
			if (entityCountObject instanceof AtomicInteger) {
				return ((AtomicInteger) entityCountObject).incrementAndGet();
			} else if (entityCountObject instanceof Number) {
				int value = ((Number) entityCountObject).intValue() + 1;
				entityCountField.set(null, value);
				return value;
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
