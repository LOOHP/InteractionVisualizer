package com.loohp.interactionvisualizer.entityholders;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public interface IVisualizerEntity {

    void setRotation(float yaw, float pitch);

    World getWorld();

    void teleport(Location location);

    void teleport(World world, double x, double y, double z);

    void teleport(World world, double x, double y, double z, float yaw, float pitch);

    Location getLocation();

    void setLocation(Location location);

    boolean isSilent();

    void setSilent(boolean bool);

    UUID getUniqueId();

    int getEntityId();

    boolean isLocked();

    void setLocked(boolean bool);

    double getHeight();

    WrappedDataWatcher getWrappedDataWatcher();

}
