package com.loohp.interactionvisualizer.api;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import com.loohp.interactionvisualizer.managers.TaskManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;

/**
This class is used for Displays which are shown when a player interact with a certain interface
*/
public abstract class VisualizerInteractDisplay implements VisualizerDisplay {
	
	/**
	DO NOT CHANGE THESE FIELD
	*/
	private InventoryType type;
	private Set<Integer> tasks;
	
	/**
	This method will be called whenever a player opens the InventoryType registered.
	*/
	public abstract void process(Player player);
	
	/**
	This method is used if you need a runnable, return the task id, return -1 to disable
	*/
	public int run() {
		return -1;
	}
	
	/**
	<b>Use register(InventoryType) instead</b>
	*/
	@Deprecated
	public final void register() {
		throw new UnsupportedOperationException("Use register(InventoryType) instead");
	}
	
	@Deprecated
	public final EntryKey registerNative() {
		throw new UnsupportedOperationException("Use register(InventoryType) instead");
	}
	
	/**
	Register this custom display to InteractionVisualizer.
	*/
	public final void register(InventoryType type) {
		if (key().isNative()) {
			throw new IllegalStateException("EntryKey must not have the default interactionvisualizer namespace");
		}
		InteractionVisualizerAPI.getPreferenceManager().registerEntry(key());
		this.type = type;
		TaskManager.processes.get(type).add(this);
		this.tasks = new HashSet<>();
		int run = run();
		if (run >= 0) {
			this.tasks.add(run);
		}
	}
	
	/**
	<b>Only use if this is a native built-in display</b>
	*/
	@Deprecated
	public final EntryKey registerNative(InventoryType type) {
		this.type = type;
		TaskManager.processes.get(type).add(this);
		this.tasks = new HashSet<>();
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
	@Deprecated
	public final void unregister() {
		TaskManager.processes.get(type).remove(this);
		this.tasks.forEach(each -> Bukkit.getScheduler().cancelTask(each));
	}

}
