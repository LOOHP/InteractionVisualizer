package com.loohp.interactionvisualizer.NMS;

import java.util.Collection;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkSectionPosition;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity;

import ru.beykerykt.lightapi.LightType;

public abstract class NMS {
	
	private static NMS instance;
	
	public static NMS getInstance() {
		if (instance == null) {
			switch (InteractionVisualizer.version) {
			case V1_16_4:
				instance = new V1_16_4();
				break;
			case V1_16_2:
				instance = new V1_16_2();
				break;
			case V1_16:
				instance = new V1_16();
				break;
			case V1_15:
				instance = new V1_15();
				break;
			case V1_14:
				instance = new V1_14();
				break;
			case V1_13_1:
				instance = new V1_13_1();
				break;
			case V1_13:
				instance = new V1_13();
				break;
			case V1_12:
				instance = new V1_12();
				break;
			case V1_11:
				instance = new V1_11();
				break;
			default:
				instance = null;
				break;
			}
		}
		return instance;
	}
	
	public abstract List<BoundingBox> getBoundingBoxes(BlockPosition pos);
	
	public abstract List<TileEntity> getTileEntities(ChunkPosition chunk, boolean load);
	
	public abstract List<byte[]> getBlockLightArray(ChunkPosition chunk, boolean load);
	
	public abstract List<byte[]> getSkyLightArray(ChunkPosition chunk, boolean load);
	
	public abstract boolean isLightTypeSupported(World world, LightType lightType);
	
	public abstract boolean isLegacyLightEngine();
	
	@Deprecated
	public abstract void createLight(World world, int x, int y, int z, LightType lightType, int light);
	
	@Deprecated
	public abstract void deleteLight(World world, int x, int y, int z, LightType lightType);

	@Deprecated
	public abstract void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMaskSky, int sectionsMaskBlock, Player player);
	
	public abstract void sendLightUpdate(ChunkPosition chunkPosition, ChunkSectionPosition blockSectionPosition, ChunkSectionPosition skySectionPosition, List<byte[]> blocklevels, List<byte[]> skylevels, Collection<Player> players);

}
