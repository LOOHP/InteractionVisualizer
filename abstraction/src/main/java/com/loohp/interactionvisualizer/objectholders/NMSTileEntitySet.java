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

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NMSTileEntitySet<K, V> implements Set<TileEntity> {

    @SuppressWarnings("rawtypes")
    public static final NMSTileEntitySet EMPTY_SET = new NMSTileEntitySet<>(Collections.emptyMap(), e -> null);

    @SuppressWarnings("unchecked")
    public static <K, V> NMSTileEntitySet<K, V> emptySet() {
        return (NMSTileEntitySet<K, V>) EMPTY_SET;
    }

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

    public Map<K, V> getHandle() {
        return nmsTileEntities;
    }

    public Function<Entry<K, V>, TileEntity> getConverter() {
        return converter;
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return nmsTileEntities.entrySet().stream().map(converter).toArray();
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        a = a.length < nmsTileEntities.size() ? (T[]) Array.newInstance(a.getClass().getComponentType(), nmsTileEntities.size()) : a;
        int i = 0;
        for (Entry<K, V> entry : nmsTileEntities.entrySet()) {
            a[i++] = (T) converter.apply(entry);
        }
        return a;
    }

    @Override
    public boolean add(TileEntity tileEntity) {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        Set<TileEntity> set = nmsTileEntities.entrySet().stream().map(converter).collect(Collectors.toSet());
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends TileEntity> c) {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("NMSTileEntitySet is read only!");
    }

    @Override
    public int size() {
        return nmsTileEntities.size();
    }

    @Override
    public boolean isEmpty() {
        return nmsTileEntities.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return nmsTileEntities.entrySet().stream().map(converter).anyMatch(each -> each.equals(o));
    }

}
