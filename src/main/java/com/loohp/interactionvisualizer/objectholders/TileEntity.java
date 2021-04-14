package com.loohp.interactionvisualizer.objectholders;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

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
	}
	
	public static TileEntityType getTileEntityType(Material material) {
		return TILE_ENTITY_TYPES.get(material.toString());
	}
	
	public static boolean isTileEntityType(Material material) {
		return TILE_ENTITY_TYPES.containsKey(material.toString());
	}
	
	private World world;
	private int x;
	private int y;
	private int z;
	private TileEntityType type;
	
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
	
	public static enum TileEntityType {
		
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
		CONDUIT;
		
	}

}
