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
import com.loohp.interactionvisualizer.objectholders.Condition;
import com.loohp.platformscheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class SyncUtils {

    public static void runAsyncWithSyncCondition(Location location, Condition syncCondition, Runnable asyncTask) {
        if (Bukkit.isPrimaryThread()) {
            if (syncCondition.check()) {
                InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(asyncTask);
            }
        } else if (InteractionVisualizer.plugin.isEnabled()) {
            Scheduler.runTask(InteractionVisualizer.plugin, () -> {
                if (syncCondition.check()) {
                    InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(asyncTask);
                }
            }, location);
        }
    }
}
