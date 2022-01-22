package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.objectholders.ILightManager;
import com.loohp.interactionvisualizer.objectholders.LightData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class LightManager implements ILightManager {

    private static LightType convert(com.loohp.interactionvisualizer.objectholders.LightType lightType) {
        if (lightType == null) {
            return null;
        }
        switch (lightType) {
            case BLOCK:
                return LightType.BLOCK;
            case SKY:
                return LightType.SKY;
        }
        return null;
    }
    private final InteractionVisualizer plugin;
    private Set<LightData> addqueue;
    private Set<LightData> deletequeue;

    public LightManager(InteractionVisualizer plugin) {
        this.plugin = plugin;
        this.addqueue = new HashSet<>();
        this.deletequeue = new HashSet<>();
    }

    @Override
    public void createLight(Location location, int lightlevel, com.loohp.interactionvisualizer.objectholders.LightType lightType) {
        addqueue.add(LightData.of(location, lightlevel, lightType));
    }

    @Override
    public void deleteLight(Location location) {
        addqueue.remove(LightData.of(location, com.loohp.interactionvisualizer.objectholders.LightType.BLOCK));
        addqueue.remove(LightData.of(location, com.loohp.interactionvisualizer.objectholders.LightType.SKY));
        deletequeue.add(LightData.of(location));
    }

    @Override
    public int run() {
        return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            boolean changed = false;

            Queue<LightData> updateQueue = new LinkedList<>();

            Set<LightData> addqueue = this.addqueue;
            Set<LightData> deletequeue = this.deletequeue;

            this.addqueue = new HashSet<>();
            this.deletequeue = new HashSet<>();

            if (!deletequeue.isEmpty()) {
                changed = true;
            }
            Iterator<LightData> itr0 = deletequeue.iterator();
            while (itr0.hasNext()) {
                LightData lightdata = itr0.next();
                if (lightdata.isLocationLoaded()) {
                    Location location = lightdata.getLocation();
                    if (LightAPI.isSupported(location.getWorld(), LightType.SKY)) {
                        LightAPI.deleteLight(location, LightType.SKY, false);
                    }
                    LightAPI.deleteLight(location, LightType.BLOCK, false);
                    updateQueue.add(LightData.of(location, 14, com.loohp.interactionvisualizer.objectholders.LightType.SKY));
                    updateQueue.add(LightData.of(location, 14, com.loohp.interactionvisualizer.objectholders.LightType.BLOCK));
                }
                itr0.remove();
            }

            if (!addqueue.isEmpty()) {
                changed = true;
            }
            Iterator<LightData> itr1 = addqueue.iterator();
            while (itr1.hasNext()) {
                LightData lightdata = itr1.next();
                if (lightdata.isLocationLoaded()) {
                    Location location = lightdata.getLocation();
                    int lightlevel = lightdata.getLightLevel();
                    if (LightAPI.isSupported(location.getWorld(), convert(lightdata.getLightType()))) {
                        LightAPI.createLight(location, convert(lightdata.getLightType()), lightlevel, false);
                        updateQueue.add(lightdata);
                    }
                }
                itr1.remove();
            }

            if (changed) {
                HashSet<ChunkInfo> blockinfos = new HashSet<>();
                HashSet<ChunkInfo> skyinfos = new HashSet<>();
                while (!updateQueue.isEmpty()) {
                    LightData lightdata = updateQueue.poll();
                    LightType lightType = convert(lightdata.getLightType());
                    switch (lightType) {
                        case BLOCK:
                            blockinfos.addAll(LightAPI.collectChunks(lightdata.getLocation(), lightType, lightdata.getLightLevel()));
                            break;
                        case SKY:
                            skyinfos.addAll(LightAPI.collectChunks(lightdata.getLocation(), lightType, lightdata.getLightLevel()));
                            break;
                    }
                }
                for (ChunkInfo info : skyinfos) {
                    LightAPI.updateChunk(info, LightType.SKY);
                }
                for (ChunkInfo info : blockinfos) {
                    LightAPI.updateChunk(info, LightType.BLOCK);
                }
            }
        }, 0, InteractionVisualizer.lightUpdatePeriod).getTaskId();
    }

}
