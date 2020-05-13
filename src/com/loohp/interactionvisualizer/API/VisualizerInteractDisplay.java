package com.loohp.interactionvisualizer.API;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import com.loohp.interactionvisualizer.Managers.TaskManager;

public abstract class VisualizerInteractDisplay {
	
	/**
	DO NOT CHANGE THIS FIELD
	*/
	private InventoryType type;
	
	/**
	This method will be called whenever a player opens the InventoryType registered.
	*/
	public abstract void process(Player player);
	
	/**
	Register this custom display to InteractionVisualizer.
	*/
	public final void register(InventoryType type) {
		this.type = type;
		TaskManager.customProcesses.get(type).add(this);
	}
	
	/**
	Unregister this custom display to InteractionVisualizer.
	You don't have to use this normally.
	*/
	public final void unregister() {
		TaskManager.customProcesses.get(type).remove(this);
	}

}
