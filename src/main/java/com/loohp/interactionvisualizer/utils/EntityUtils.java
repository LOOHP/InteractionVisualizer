package com.loohp.interactionvisualizer.utils;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.bukkit.Bukkit;

import com.loohp.interactionvisualizer.InteractionVisualizer;

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
	
	public static CompletableFuture<Integer> getNextEntityId() {
		CompletableFuture<Integer> future = new CompletableFuture<>();
		try {
			entityCountField.setAccessible(true);
			Object entityCountObject = entityCountField.get(null);
			if (entityCountObject instanceof AtomicInteger) {
				future.complete(((AtomicInteger) entityCountObject).incrementAndGet());
				return future;
			} else if (entityCountObject instanceof Number) {
				if (Bukkit.isPrimaryThread()) {
					int value = ((Number) entityCountObject).intValue() + 1;
					try {
						entityCountField.set(null, value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
					future.complete(value);
				} else {
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> {
						int value = ((Number) entityCountObject).intValue() + 1;
						try {
							entityCountField.set(null, value);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
						future.complete(value);
					});
				}
				return future;
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		future.complete(-1);
		return future;
	}

}
