package com.loohp.interactionvisualizer.managers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class AsyncExecutorManager implements AutoCloseable {
	
	private ExecutorService executor;
	private AtomicBoolean valid;
	
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
