package com.loohp.interactionvisualizer.entityholders;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public interface DynamicVisualizerEntity extends IVisualizerEntity {

    Location getViewingLocation(Location from, Vector direction);

    double getRadius();

    void setRadius(double radius);

    PathType getPathType();

    void setPathType(PathType path);

    enum PathType {
        CIRCLE, SQUARE, FACE
    }

}
