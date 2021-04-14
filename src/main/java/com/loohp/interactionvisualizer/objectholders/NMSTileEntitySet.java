package com.loohp.interactionvisualizer.objectholders;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class NMSTileEntitySet<K, V> implements Iterable<TileEntity> {
	
	private Map<K, V> nmsTileEntities;
	private Function<Entry<K, V>, TileEntity> converter;
	
	public NMSTileEntitySet(Map<K, V> nmsTileEntities, Function<Entry<K, V>, TileEntity> converter) {
		this.nmsTileEntities = nmsTileEntities;
		this.converter = converter;
	}

	@Override
	public Iterator<TileEntity> iterator() {
		return new Iterator<TileEntity>() {
			
			private Iterator<Entry<K, V>> itr = nmsTileEntities.entrySet().iterator();
			
			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public TileEntity next() {
				return converter.apply(itr.next());
			}
		};
	}
	
	public int size() {
		return nmsTileEntities.size();
	}
	
	public Map<K, V> getHandle() {
		return nmsTileEntities;
	}
	
	public Function<Entry<K, V>, TileEntity> getConverter() {
		return converter;
	}

}
