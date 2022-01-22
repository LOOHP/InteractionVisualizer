package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class ChunkSectionPosition {

    public static ChunkSectionPosition of(World world, int... sections) {
        return new ChunkSectionPosition((world.getMaxHeight() >> 4), sections);
    }

    public static ChunkSectionPosition of(World world, boolean extra, int... sections) {
        if (extra) {
            return new ChunkSectionPosition((world.getMaxHeight() >> 4) + 2, sections);
        } else {
            return of(world, sections);
        }
    }

    public static ChunkSectionPosition of(int size, int... sections) {
        return new ChunkSectionPosition(size, sections);
    }

    public static byte[] changeLight(byte[] levels, int x, int y, int z, int newValue) {
        if (levels.length != 2048) {
            throw new IllegalArgumentException("Light Level array length not equal to 2048.");
        }
        x %= 16;
        y %= 16;
        z %= 16;
        byte value = levels[y * 128 + z * 8 + x / 2];
        if (x % 2 == 0) {
            value &= (byte) 240;
            value |= (byte) newValue;
        } else {
            value &= (byte) 15;
            value |= ((byte) newValue) << 4;
        }
        levels[y * 128 + z * 8 + x / 2] = value;
        return levels;
    }
    private final boolean[] flags;

    private ChunkSectionPosition(int size, int... sections) {
        flags = new boolean[size];
        for (int i : sections) {
            flags[i] = true;
        }
    }

    public void set(int position, boolean value) {
        flags[position] = value;
    }

    public int getSize() {
        return flags.length;
    }

    public boolean get(int position) {
        return flags[position];
    }

    public List<Integer> getSetPositions() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) {
                list.add(i);
            }
        }
        return list;
    }

    public int getBitmask() {
        int bitmask = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i]) {
                bitmask |= (1 << i);
            }
        }
        return bitmask;
    }

}
