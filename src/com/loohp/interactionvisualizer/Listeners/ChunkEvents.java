package com.loohp.interactionvisualizer.Listeners;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class ChunkEvents implements Listener {
	
	private static MethodHandle method;
	
	public static void setup() {
	    try {
	    	method = MethodHandles.lookup().findVirtual(ChunkUnloadEvent.class, "setCancelled", MethodType.methodType(void.class, boolean.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onUnload(ChunkUnloadEvent event) {
		if (event.getWorld().equals(InteractionVisualizer.defaultworld) && event.getChunk().getX() == 0 && event.getChunk().getZ() == 0) {
			try {
				method.invokeExact(event, true);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
