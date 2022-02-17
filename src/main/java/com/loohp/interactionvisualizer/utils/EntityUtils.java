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
