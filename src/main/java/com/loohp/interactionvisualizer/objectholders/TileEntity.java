package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class TileEntity {

    private static final Map<String, TileEntityType> TILE_ENTITY_TYPES = new HashMap<>();

    static {
        TILE_ENTITY_TYPES.put("BLAST_FURNACE", TileEntityType.BLAST_FURNACE);
        TILE_ENTITY_TYPES.put("BREWING_STAND", TileEntityType.BREWING_STAND);
        TILE_ENTITY_TYPES.put("FURNACE", TileEntityType.FURNACE);
        TILE_ENTITY_TYPES.put("BURNING_FURNACE", TileEntityType.FURNACE);
        TILE_ENTITY_TYPES.put("SMOKER", TileEntityType.SMOKER);
        TILE_ENTITY_TYPES.put("BEACON", TileEntityType.BEACON);
        TILE_ENTITY_TYPES.put("JUKEBOX", TileEntityType.JUKEBOX);
        TILE_ENTITY_TYPES.put("BEE_NEST", TileEntityType.BEE_NEST);
        TILE_ENTITY_TYPES.put("BEEHIVE", TileEntityType.BEEHIVE);
        TILE_ENTITY_TYPES.put("LECTERN", TileEntityType.LECTERN);
        TILE_ENTITY_TYPES.put("CAMPFIRE", TileEntityType.CAMPFIRE);
        TILE_ENTITY_TYPES.put("SOUL_CAMPFIRE", TileEntityType.SOUL_CAMPFIRE);
        TILE_ENTITY_TYPES.put("SPAWNER", TileEntityType.SPAWNER);
        TILE_ENTITY_TYPES.put("MOB_SPAWNER", TileEntityType.SPAWNER);
        TILE_ENTITY_TYPES.put("CONDUIT", TileEntityType.CONDUIT);

        TILE_ENTITY_TYPES.put("STANDING_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("WHITE_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("ORANGE_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("MAGENTA_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIGHT_BLUE_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("YELLOW_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIME_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("PINK_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("GRAY_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIGHT_GRAY_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("CYAN_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("PURPLE_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BLUE_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BROWN_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("GREEN_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("RED_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BLACK_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("FLOWER_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("CREEPER_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("SKULL_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("MOJANG_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("GLOBE_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("PIGLIN_BANNER_PATTERN", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("WHITE_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("ORANGE_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("MAGENTA_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIGHT_BLUE_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("YELLOW_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIME_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("PINK_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("GRAY_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("LIGHT_GRAY_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("CYAN_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("PURPLE_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BLUE_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BROWN_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("GREEN_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("RED_WALL_BANNER", TileEntityType.BANNER);
        TILE_ENTITY_TYPES.put("BLACK_WALL_BANNER", TileEntityType.BANNER);
    }

    public static TileEntityType getTileEntityType(Material material) {
        return TILE_ENTITY_TYPES.get(material.toString());
    }

    public static boolean isTileEntityType(Material material) {
        return TILE_ENTITY_TYPES.containsKey(material.toString());
    }
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final TileEntityType type;

    public TileEntity(World world, int x, int y, int z, TileEntityType type) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public TileEntityType getType() {
        return type;
    }

    public Block getBlock() {
        return world.getBlockAt(x, y, z);
    }

    public enum TileEntityType {

        BLAST_FURNACE,
        BREWING_STAND,
        FURNACE,
        SMOKER,
        BEACON,
        JUKEBOX,
        BEE_NEST,
        BEEHIVE,
        LECTERN,
        CAMPFIRE,
        SOUL_CAMPFIRE,
        SPAWNER,
        CONDUIT,
        BANNER

    }

}
