package com.loohp.interactionvisualizer.api;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;

import com.loohp.interactionvisualizer.managers.TaskManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;

/**
This class is used for Displays which are shown when something is active by itself like a furnace cooking
*/
public abstract class VisualizerRunnableDisplay implements VisualizerDisplay {
	
	/**
	DO NOT CHANGE THESE FIELD
	*/
	private Set<Integer> tasks;
	
	/**
	This method is used for cleaning up, return the task id, return -1 to disable
	*/
	public abstract int gc();
	
	/**
	This method is used for a runnable, return the task id, return -1 to disable
	*/
	public abstract int run();	
	
	/**
	Register this custom display to InteractionVisualizer.
	*/
	public final void register() {
		InteractionVisualizerAPI.getPreferenceManager().registerEntry(key());
		TaskManager.runnables.add(this);
		this.tasks = new HashSet<>();
		int gc = gc();
		if (gc >= 0) {
			this.tasks.add(gc);
		}
		int run = run();
		if (run >= 0) {
			this.tasks.add(run);
		}
	}
	
	@Deprecated
	public final EntryKey registerNative() {
		TaskManager.runnables.add(this);
		this.tasks = new HashSet<>();
		int gc = gc();
		if (gc >= 0) {
			this.tasks.add(gc);
		}
		int run = run();
		if (run >= 0) {
			this.tasks.add(run);
		}
		return key();
	}
	
	/**
	Unregister this custom display to InteractionVisualizer.
	You don't have to use this normally.
	*/
	public final void unregister() {
		TaskManager.runnables.remove(this);
		this.tasks.forEach(each -> Bukkit.getScheduler().cancelTask(each));
	}

}
