package com.loohp.interactionvisualizer.NMS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_11_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.ObjectHolders.BlockPosition;
import com.loohp.interactionvisualizer.ObjectHolders.BoundingBox;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkSectionPosition;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity;
import com.loohp.interactionvisualizer.ObjectHolders.TileEntity.TileEntityType;

import net.minecraft.server.v1_11_R1.AxisAlignedBB;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.ChunkSection;
import net.minecraft.server.v1_11_R1.EnumSkyBlock;
import net.minecraft.server.v1_11_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_11_R1.WorldServer;
import ru.beykerykt.lightapi.LightType;

public class V1_11 extends NMS {
	
	public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
		net.minecraft.server.v1_11_R1.BlockPosition blockpos = new net.minecraft.server.v1_11_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
		WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
		AxisAlignedBB box = world.getType(blockpos).d(world, blockpos);
		List<BoundingBox> boxes = new ArrayList<>(1);
		boxes.add(new BoundingBox(box.a + pos.getX(), box.b + pos.getY(), box.c + pos.getZ(), box.d + pos.getX(), box.e + pos.getY(), box.f + pos.getZ()));
		return boxes;
	}
	
	@Override
	public List<TileEntity> getTileEntities(ChunkPosition chunk, boolean load) {
		List<TileEntity> list = new ArrayList<>();
		if (!chunk.isLoaded() && !load) {
			return list;
		}
		World world = chunk.getWorld();
		
		((CraftChunk) chunk.getChunk()).getHandle().tileEntities.entrySet().forEach(entry -> {
			net.minecraft.server.v1_11_R1.BlockPosition pos = entry.getKey();
			Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock());
			TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
			if (tileEntityType != null) {
				list.add(new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType));
			}
		});
		return list;
	}
	
	@Override
	public List<byte[]> getBlockLightArray(ChunkPosition chunkPosition, boolean load) {
		List<byte[]> list = new ArrayList<>();
		if (!chunkPosition.isLoaded() && !load) {
			return list;
		}
		
		Chunk chunk = ((CraftChunk) chunkPosition.getChunk()).getHandle();
		int i = '\uffff';
		ChunkSection[] achunksection = chunk.getSections();
        int k = 0;

        for (int l = achunksection.length; k < l; ++k) {
            ChunkSection chunksection = achunksection[k];

            if (chunksection != Chunk.a && !chunksection.a() && (i & 1 << k) != 0) {
            	list.add(chunksection.getEmittedLightArray().asBytes());
            }
        }
		
		return list;
	}

	@Override
	public List<byte[]> getSkyLightArray(ChunkPosition chunkPosition, boolean load) {
		List<byte[]> list = new ArrayList<>();
		if (!chunkPosition.isLoaded() && !load) {
			return list;
		}
		
		Chunk chunk = ((CraftChunk) chunkPosition.getChunk()).getHandle();
		int i = '\uffff';
		if (!chunk.getWorld().worldProvider.m()) {
			return list;
		}
		
		ChunkSection[] achunksection = chunk.getSections();
        int k = 0;

        for (int l = achunksection.length; k < l; ++k) {
            ChunkSection chunksection = achunksection[k];

            if (chunksection != Chunk.a && !chunksection.a() && (i & 1 << k) != 0) {
            	list.add(chunksection.getSkyLightArray().asBytes());
            }
        }
		
		return list;
	}
	
	@Override
	public void createLight(World world, int x, int y, int z, LightType lightType, int light) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		worldServer.a(lightType == LightType.SKY ? EnumSkyBlock.SKY : EnumSkyBlock.BLOCK, new net.minecraft.server.v1_11_R1.BlockPosition(x, y, z), light);
	}

	@Override
	public void deleteLight(World world, int x, int y, int z, LightType lightType) {
		recalculateLighting(world, x, y, z, lightType);
	}

	protected void recalculateLighting(World world, int x, int y, int z, LightType lightType) {
		WorldServer worldServer = ((CraftWorld) world).getHandle();
		net.minecraft.server.v1_11_R1.BlockPosition position = new net.minecraft.server.v1_11_R1.BlockPosition(x, y, z);
		worldServer.c(lightType == LightType.SKY ? EnumSkyBlock.SKY : EnumSkyBlock.BLOCK, position);
	}

	@Override
	public void sendChunkSectionsUpdate(World world, int chunkX, int chunkZ, int sectionsMaskSky, int sectionsMaskBlock, Player player) {
		int sectionsMask = sectionsMaskSky | sectionsMaskBlock;
		Chunk chunk = ((CraftWorld) world).getHandle().getChunkAt(chunkX, chunkZ);
		// The last argument is bit-mask what chunk sections to update. Mask containing
		// 16 bits, with the lowest bit corresponding to chunk section 0 (y=0 to y=15)
		// and the highest bit for chunk section 15 (y=240 to 255).
		PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, sectionsMask);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public boolean isLightTypeSupported(World world, LightType lightType) {
		if (!(world instanceof CraftWorld)) {
			return false;
		}
		if (lightType == LightType.SKY) {
			WorldServer worldServer = ((CraftWorld) world).getHandle();
			return worldServer.worldProvider.m();
		} else {
			return true;
		}
	}

	@Override
	public boolean isLegacyLightEngine() {
		return true;
	}

	@Override
	public void sendLightUpdate(ChunkPosition chunkPosition, ChunkSectionPosition blockSectionPosition, ChunkSectionPosition skySectionPosition, List<byte[]> blocklevels, List<byte[]> skylevels, Collection<Player> players) {
		throw new UnsupportedOperationException("Method cannot be used with the legacy light engine.");
	}

}
