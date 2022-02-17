/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.objectholders;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class NMSTileEntitySet<K, V> implements Iterable<TileEntity> {

    private final Map<K, V> nmsTileEntities;
    private final Function<Entry<K, V>, TileEntity> converter;

    public NMSTileEntitySet(Map<K, V> nmsTileEntities, Function<Entry<K, V>, TileEntity> converter) {
        this.nmsTileEntities = nmsTileEntities;
        this.converter = converter;
    }

    @Override
    public Iterator<TileEntity> iterator() {
        return new Iterator<TileEntity>() {

            private final Iterator<Entry<K, V>> itr = nmsTileEntities.entrySet().iterator();

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
