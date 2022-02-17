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
import com.loohp.interactionvisualizer.objectholders.Condition;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyncUtils {

    public static <T> T executeSync(Callable<T> task, long timeout, T def) {
        if (Bukkit.isPrimaryThread()) {
            try {
                return task.call();
            } catch (Exception e) {
                return def;
            }
        } else if (InteractionVisualizer.plugin.isEnabled()) {
            Future<T> future = Bukkit.getScheduler().callSyncMethod(InteractionVisualizer.plugin, task);
            try {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                return def;
            }
        } else {
            return def;
        }
    }

    public static void runAsyncWithSyncCondition(Condition syncCondition, long timeout, Runnable asyncTask) {
        if (executeSync(() -> syncCondition.check(), timeout, false) && InteractionVisualizer.plugin.isEnabled()) {
            InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(asyncTask);
        }
    }

}
