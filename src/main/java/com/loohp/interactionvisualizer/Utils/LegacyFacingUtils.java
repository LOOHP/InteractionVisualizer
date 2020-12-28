package com.loohp.interactionvisualizer.Utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class LegacyFacingUtils {
	
	@SuppressWarnings("deprecation")
	public static BlockFace getFacing(Block block) {
		if(block.getData() == (byte) 3) {
            return BlockFace.SOUTH;
		} else if(block.getData() == (byte) 2) {
			return BlockFace.NORTH;                       
		} else if(block.getData() == (byte) 4) {
			return BlockFace.WEST;
		} else if(block.getData() == (byte) 5) {
			return BlockFace.EAST;
		}
		return BlockFace.SOUTH;
	}

}
