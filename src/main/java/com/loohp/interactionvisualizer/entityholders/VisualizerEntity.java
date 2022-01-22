package com.loohp.interactionvisualizer.entityholders;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.utils.EntityUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class VisualizerEntity implements IVisualizerEntity {

    protected UUID uuid;
    protected Location location;
    protected boolean lock;
    protected boolean isSilent;
    private int id;
    private final transient Future<Integer> entityIdFuture;

    public VisualizerEntity(Location location) {
        this.entityIdFuture = EntityUtils.getNextEntityId();
        this.id = Integer.MIN_VALUE;
        this.uuid = UUID.randomUUID();
        this.location = location.clone();
        this.lock = false;
        this.isSilent = false;
    }

    @Override
    public final int getEntityId() {
        if (id != Integer.MIN_VALUE) {
            return id;
        }
        try {
            return id = entityIdFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int cacheCode() {
        int prime = 17;
        int result = 1;
        result = prime * result + id;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        result = prime * result + ((location == null) ? 0 : location.hashCode());
        result = prime * result + ((lock) ? 1531 : 4021);
        result = prime * result + ((isSilent) ? 3301 : 4507);
        return result;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        if (lock) {
            return;
        }
        teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    @Override
    public void teleport(Location location) {
        this.location = location.clone();
    }

    @Override
    public void teleport(World world, double x, double y, double z) {
        this.location = new Location(world, x, y, z, location.getYaw(), location.getPitch());
    }

    @Override
    public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
        this.location = new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location location) {
        this.location = location.clone();
    }

    @Override
    public boolean isSilent() {
        return isSilent;
    }

    @Override
    public void setSilent(boolean bool) {
        this.isSilent = bool;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isLocked() {
        return lock;
    }

    @Override
    public void setLocked(boolean bool) {
        this.lock = bool;
    }

    @Override
    public abstract double getHeight();

    @Override
    public abstract WrappedDataWatcher getWrappedDataWatcher();

}
