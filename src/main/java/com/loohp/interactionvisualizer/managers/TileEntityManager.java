/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.events.TileEntityRemovedEvent;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TileEntityManager implements Listener {

    private static final Plugin plugin = InteractionVisualizer.plugin;
    private static final TileEntityType[] tileEntityTypes = TileEntityType.values();
    private static final Map<TileEntityType, Set<Block>> active = new EnumMap<>(TileEntityType.class);
    private static final Map<ChunkPosition, Set<Block>> byChunk = new HashMap<>();

    public static void _init_() {
        for (TileEntityType type : tileEntityTypes) {
            active.put(type, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }
        TileEntityManager instance = new TileEntityManager();
        Bukkit.getPluginManager().registerEvents(instance, plugin);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (TileEntityType type : tileEntityTypes) {
                Set<Block> blocks = active.get(type);
                Iterator<Block> itr = blocks.iterator();
                while (itr.hasNext()) {
                    Block block = itr.next();
                    if (!PlayerLocationManager.hasPlayerNearby(block.getLocation())) {
                        itr.remove();
                    }
                }
            }
        }, 0, InteractionVisualizerAPI.getGCPeriod());
        for (Player player : Bukkit.getOnlinePlayers()) {
            instance.onJoin(new PlayerJoinEvent(player, ""));
        }
    }

    public static Set<Block> getTileEntites(TileEntityType type) {
        Set<Block> set = active.get(type);
        return set != null ? set : new LinkedHashSet<>();
    }

    private static Set<ChunkPosition> getAllChunks(Location location) {
        Set<ChunkPosition> chunks = new LinkedHashSet<>();
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        for (int z = -InteractionVisualizer.tileEntityCheckingRange; z <= InteractionVisualizer.tileEntityCheckingRange; z++) {
            for (int x = -InteractionVisualizer.tileEntityCheckingRange; x <= InteractionVisualizer.tileEntityCheckingRange; x++) {
                chunks.add(new ChunkPosition(world, chunkX + x, chunkZ + z));
            }
        }
        return chunks;
    }

    private static ChunkPosition getChunk(Location location) {
        World world = location.getWorld();
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return new ChunkPosition(world, chunkX, chunkZ);
    }

    private static void addTileEntities(Collection<ChunkPosition> chunks) {
        for (ChunkPosition chunk : chunks) {
            addTileEntities(chunk);
        }
    }

    private synchronized static void addTileEntities(ChunkPosition chunk) {
        NMSTileEntitySet<?, ?> list = NMS.getInstance().getTileEntities(chunk, false);
        Set<Block> blocks = byChunk.get(chunk);
        if (blocks == null) {
            blocks = new LinkedHashSet<>();
            byChunk.put(chunk, blocks);
        }
        Map<Block, TileEntityType> newBlocks = new LinkedHashMap<>();
        if (list != null) {
            for (TileEntity tile : list) {
                if (tile != null) {
                    Block block = tile.getBlock();
                    TileEntityType type = tile.getType();
                    active.get(type).add(block);
                    newBlocks.put(block, type);
                    blocks.add(block);
                }
            }
        }
        Iterator<Block> itr = blocks.iterator();
        while (itr.hasNext()) {
            Block block = itr.next();
            TileEntityType type = newBlocks.get(block);
            if (type == null) {
                itr.remove();
                for (TileEntityType t : tileEntityTypes) {
                    if (active.get(t).remove(block)) {
                        Bukkit.getPluginManager().callEvent(new TileEntityRemovedEvent(block, t));
                    }
                }
            } else {
                for (TileEntityType t : tileEntityTypes) {
                    if (!t.equals(type)) {
                        if (active.get(t).remove(block)) {
                            Bukkit.getPluginManager().callEvent(new TileEntityRemovedEvent(block, t));
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        addTileEntities(getAllChunks(event.getPlayer().getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null) {
            TileEntityType type = TileEntity.getTileEntityType(block.getType());
            if (type != null) {
                if (!active.get(type).contains(block)) {
                    addTileEntities(getChunk(block.getLocation()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (!from.getWorld().equals(to.getWorld()) || from.getBlockX() >> 4 != to.getBlockX() >> 4 || from.getBlockZ() >> 4 != to.getBlockZ() >> 4) {
            addTileEntities(getAllChunks(to));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (!from.getWorld().equals(to.getWorld()) || ((from.getBlockX() >> 4 != to.getBlockX() >> 4 || from.getBlockZ() >> 4 != to.getBlockZ() >> 4) && !isMovingTooFast(event.getPlayer(), from, to))) {
            addTileEntities(getAllChunks(to));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle().getPassengers().stream().anyMatch(each -> each instanceof Player)) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (!from.getWorld().equals(to.getWorld()) || ((from.getBlockX() >> 4 != to.getBlockX() >> 4 || from.getBlockZ() >> 4 != to.getBlockZ() >> 4) && !isMovingTooFast(null, from, to))) {
                addTileEntities(getAllChunks(to));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        if (TileEntity.isTileEntityType(event.getBlock().getType())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> addTileEntities(getChunk(event.getBlock().getLocation())), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        if (TileEntity.isTileEntityType(event.getBlock().getType())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> addTileEntities(getChunk(event.getBlock().getLocation())), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Set<ChunkPosition> chunks = new LinkedHashSet<>();
        if (TileEntity.isTileEntityType(event.getBlock().getType())) {
            chunks.add(getChunk(event.getBlock().getLocation()));
        }
        for (Block block : event.blockList()) {
            if (TileEntity.isTileEntityType(block.getType())) {
                chunks.add(getChunk(block.getLocation()));
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> addTileEntities(chunks), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Set<ChunkPosition> chunks = new LinkedHashSet<>();
        for (Block block : event.blockList()) {
            if (TileEntity.isTileEntityType(block.getType())) {
                chunks.add(getChunk(block.getLocation()));
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> addTileEntities(chunks), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (TileEntity.isTileEntityType(event.getBlock().getType())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> addTileEntities(getChunk(event.getBlock().getLocation())), 1);
        }
    }

    private boolean isMovingTooFast(Player player, Location from, Location to) {
        double changeX = Math.abs(from.getX() - to.getX());
        double changeZ = Math.abs(from.getZ() - to.getZ());
        double horizontalDistanceSquared = changeX * changeX + changeZ * changeZ;
        if (player != null && player.isGliding()) {
            return horizontalDistanceSquared > InteractionVisualizer.ignoreGlideSquared;
        }
        if (player != null && player.isFlying()) {
            return horizontalDistanceSquared > InteractionVisualizer.ignoreFlySquared;
        }
        return horizontalDistanceSquared > InteractionVisualizer.ignoreWalkSquared;
    }

}
