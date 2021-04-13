package com.loohp.interactionvisualizer.entityholders;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public interface DynamicVisualizerEntity extends IVisualizerEntity {
	
	public Location getViewingLocation(Location from, Vector direction);
	
	public double getRadius();
	
	public void setRadius(double radius);
	
	public PathType getPathType();
	
	public void setPathType(PathType path);
	
	public static enum PathType {
		CIRCLE, SQUARE;
	}

}
