package com.loohp.interactionvisualizer.ObjectHolders;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChunkUpdateQueue {
	
	Set<ChunkPosition> queue;
	
	public ChunkUpdateQueue() {
		queue = new LinkedHashSet<ChunkPosition>();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public void add(ChunkPosition chunkposition) {
		queue.add(chunkposition);
	}
	
	public ChunkPosition poll() {
		Iterator<ChunkPosition> itr = queue.iterator();
		if (!itr.hasNext()) {
			return null;
		}
		ChunkPosition chunkpos = itr.next();
		itr.remove();
		return chunkpos;
	}
	
	public void clear() {
		queue.clear();
	}

}
