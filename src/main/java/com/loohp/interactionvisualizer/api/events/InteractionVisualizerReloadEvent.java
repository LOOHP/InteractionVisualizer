package com.loohp.interactionvisualizer.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called after InteractionVisualizer is reloaded.
 * @author LOOHP
 *
 */
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
