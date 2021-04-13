package com.loohp.interactionvisualizer.entityholders;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SurroundingPlaneArmorStand extends ArmorStand implements DynamicVisualizerEntity {
	
	public static final double RIGHT_ANGLE = 1.5707963267948966;
	public static final double _45_ANGLE = RIGHT_ANGLE / 2;
	
	private double radius;
	private PathType path;

	public SurroundingPlaneArmorStand(Location location, double radius, PathType path) {
		super(location);
		this.radius = radius;
		this.path = path;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	@Override
	public PathType getPathType() {
		return path;
	}
	
	@Override
	public void setPathType(PathType path) {
		this.path = path;
	}
	
	@Override
	public Location getViewingLocation(Location from, Vector direction) {
		if (!from.getWorld().equals(location.getWorld())) {
			throw new IllegalArgumentException("Cannot view SurroundingArmorStand in " + location.getWorld().getName() + " from " + from.getWorld().getName());
		}
		Vector vector = getViewingVector(location, from, direction, radius, path);
		return location.clone().add(vector);
	}
	
	private static Vector getViewingVector(Location location, Location from, Vector direction, double radius, PathType path) {
		Location leveled = from.clone();
		leveled.setY(location.getY());
		if (location.distanceSquared(leveled) < radius * radius) {
			Vector v = direction.clone().setY(0);
			if (v.getX() == 0 && v.getZ() == 0) {
				v.setX(0.001);
			}
			v.normalize().multiply(radius + 2);
			Location altLocation = leveled.clone().add(v);
			return getViewingVector(location, altLocation, direction, radius, path);
		} else {
			Vector vector;
			switch (path) {
			case SQUARE:
				Vector axis = location.clone().add(1, 0, 0).toVector().subtract(location.toVector()).normalize();
				vector = leveled.toVector().subtract(location.toVector()).normalize();
				double rawAngle = Math.abs(axis.angle(vector));
				double angle = rawAngle % _45_ANGLE;
				if (rawAngle % RIGHT_ANGLE > _45_ANGLE) {
					angle = _45_ANGLE - angle;
				}
				double length = radius / Math.cos(angle);
				vector.multiply(length);
				break;
			case CIRCLE:
			default:
				vector = leveled.toVector().subtract(location.toVector()).normalize().multiply(radius);
				break;
			}
			return vector;
		}
	}

}
