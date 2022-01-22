package com.loohp.interactionvisualizer.utils;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

    public static Future<Integer> getNextEntityId() {
        try {
            entityCountField.setAccessible(true);
            Object entityCountObject = entityCountField.get(null);
            if (entityCountObject instanceof AtomicInteger) {
                return CompletableFuture.completedFuture(((AtomicInteger) entityCountObject).incrementAndGet());
            } else if (entityCountObject instanceof Integer) {
                if (Bukkit.isPrimaryThread()) {
                    int value = (Integer) entityCountObject;
                    try {
                        entityCountField.set(null, value + 1);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return CompletableFuture.completedFuture(value);
                } else {
                    return Bukkit.getScheduler().callSyncMethod(InteractionVisualizer.plugin, () -> getNextEntityId().get());
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(-1);
    }

}
