package com.loohp.interactionvisualizer.listeners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class ChunkEvents implements Listener {
	
	private static Method method;
	
	public static void setup() {
		try {
			method = ChunkUnloadEvent.class.getMethod("setCancelled", boolean.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onUnload(ChunkUnloadEvent event) {
		if (event.getWorld().equals(InteractionVisualizer.defaultworld) && event.getChunk().getX() == 0 && event.getChunk().getZ() == 0) {
			try {
				method.invoke(event, true);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
