package com.loohp.interactionvisualizer.NMS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkSectionPosition;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity.TileEntityType;

import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.EnumSkyBlock;
import net.minecraft.server.v1_15_R1.LightEngineBlock;
import net.minecraft.server.v1_15_R1.LightEngineLayerEventListener;
import net.minecraft.server.v1_15_R1.LightEngineSky;
import net.minecraft.server.v1_15_R1.LightEngineThreaded;
import net.minecraft.server.v1_15_R1.NibbleArray;
import net.minecraft.server.v1_15_R1.SectionPosition;
import net.minecraft.server.v1_15_R1.VoxelShape;
import net.minecraft.server.v1_15_R1.WorldServer;
import ru.beykerykt.lightapi.LightType;

public class V1_15 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_15_R1.BlockPosition blockpos = new net.minecraft.server.v1_15_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
		return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
	}
	
	@Override
	public List<TileEntity> getTileEntities(ChunkPosition chunk, boolean load) {
		List<TileEntity> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		World world = chunk.getWorld();
		
		((CraftChunk) chunk.getChunk()).getHandle().tileEntities.entrySet().forEach(entry -> {
			net.minecraft.server.v1_15_R1.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				list.add(new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType));
			}
		});
		return list;
	}
	
	@Override
	public List<byte[]> getBlockLightArray(ChunkPosition chunk, boolean load) {
		List<byte[]> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		
		LightEngineLayerEventListener engine = ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().getLightEngine().a(EnumSkyBlock.BLOCK);
		for (int i = 0; i < 18; ++i) {
            NibbleArray nibblearray = engine.a(SectionPosition.a(new ChunkCoordIntPair(chunk.getChunkX(), chunk.getChunkZ()), i - 1));
            if (nibblearray != null) {
            	list.add(nibblearray.asBytes().clone());
            }
        }
		
		return list;
	}

	@Override
	public List<byte[]> getSkyLightArray(ChunkPosition chunk, boolean load) {
		List<byte[]> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		
		LightEngineLayerEventListener engine = ((CraftWorld) chunk.getWorld()).getHandle().getChunkProvider().getLightEngine().a(EnumSkyBlock.SKY);
		for (int i = 0; i < 18; ++i) {
            NibbleArray nibblearray = engine.a(SectionPosition.a(new ChunkCoordIntPair(chunk.getChunkX(), chunk.getChunkZ()), i - 1));
            if (nibblearray != null) {
            	list.add(nibblearray.asBytes().clone());
            }
        }
		
		return list;
	}
	
	@Override
	public boolean isLightTypeSupported(World world, LightType lightType) {
		if (!(world instanceof CraftWorld)) {
			return false;
		}
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		LightEngineThreaded lightEngine = worldServer.getChunkProvider().getLightEngine();
		if (lightType == LightType.SKY) {
			return lightEngine.a(EnumSkyBlock.SKY) instanceof LightEngineSky;
		} else {
			return lightEngine.a(EnumSkyBlock.BLOCK) instanceof LightEngineBlock;
		}
	}

	@Override
	public boolean isLegacyLightEngine() {
		return false;
	}

	@Override
	public void createLight(World world, int x, int y, int z, LightType lightType, int light) {
		throw new UnsupportedOperationException("Method cannot be used with the modern light engine.");
	}

	@Override
	public void deleteLight(World world, int x, int y, int z, LightType lightType) {
		throw new UnsupportedOperationException("Method cannot be used with the modern light engine.");
	}

	@Override
	public void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMaskSky, int sectionsMaskBlock, Player player) {
		throw new UnsupportedOperationException("Method cannot be used with the modern light engine.");
	}
	
	@Override
	public void sendLightUpdate(ChunkPosition chunkPosition, ChunkSectionPosition blockSectionPosition, ChunkSectionPosition skySectionPosition, List<byte[]> blocklevels, List<byte[]> skylevels, Collection<Player> players) {
		PacketContainer packet = InteractionVisualizer.protocolManager.createPacket(PacketType.Play.Server.LIGHT_UPDATE);
		packet.getIntegers().write(0, chunkPosition.getChunkX());
		packet.getIntegers().write(1, chunkPosition.getChunkZ());
		int skyBitmask = skySectionPosition.getBitmask();
		int blockBitmask = skySectionPosition.getBitmask();
		packet.getIntegers().write(2, skyBitmask);
		packet.getIntegers().write(3, blockBitmask);
		packet.getIntegers().write(4, ~skyBitmask);
		packet.getIntegers().write(5, ~blockBitmask);
		List<byte[]> skyBits = new ArrayList<>();
		for (int i : skySectionPosition.getSetPositions()) {
			skyBits.add(skylevels.get(i));
		}
		List<byte[]> blockBits = new ArrayList<>();
		for (int i : blockSectionPosition.getSetPositions()) {
			blockBits.add(blocklevels.get(i));
		}
		packet.getModifier().write(6, skyBits);
		packet.getModifier().write(7, blockBits);
		if (packet.getBooleans().size() > 0) {
			packet.getBooleans().write(0, false);
		}
	}

}
