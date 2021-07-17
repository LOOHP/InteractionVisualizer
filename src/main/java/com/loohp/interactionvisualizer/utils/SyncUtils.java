package com.loohp.interactionvisualizer.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.Condition;

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
			Bukkit.getScheduler().runTaskAsynchronously(InteractionVisualizer.plugin, asyncTask);
		}
	}

}
