package com.loohp.interactionvisualizer.API.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class InteractionVisualizerReloadEvent extends Event {
	
	public InteractionVisualizerReloadEvent() {
		
	}

	private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}