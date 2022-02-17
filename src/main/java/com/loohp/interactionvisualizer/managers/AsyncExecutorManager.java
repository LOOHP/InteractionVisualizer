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

package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncExecutorManager implements AutoCloseable {

    private final ExecutorService executor;
    private final AtomicBoolean valid;

    public AsyncExecutorManager(ExecutorService executor) {
        this.executor = executor;
        this.valid = new AtomicBoolean(true);
    }

    public void runTaskAsynchronously(Runnable runnable) {
        if (!valid.get()) {
            return;
        }
        executor.submit(runnable);
    }

    public void runTaskLaterAsynchronously(Runnable runnable, long delay) {
        if (!valid.get()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> runTaskAsynchronously(runnable), delay);
    }

    public boolean isValid() {
        return valid.get();
    }

    @Override
    public void close() {
        if (!valid.get()) {
            return;
        }
        executor.shutdown();
    }

}
