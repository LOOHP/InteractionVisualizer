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
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WrappedIterable<I, O> implements Iterable<O> {

    private final Iterable<I> backingIterable;
    private final Function<I, O> converter;

    public WrappedIterable(Iterable<I> backingCollection, Function<I, O> converter) {
        this.backingIterable = backingCollection;
        this.converter = converter;
    }

    @Override
    public Iterator<O> iterator() {
        return new Iterator<O>() {

            private final Iterator<I> itr = backingIterable.iterator();

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public O next() {
                I obj = itr.next();
                return obj == null ? null : converter.apply(obj);
            }
        };
    }

    public Stream<O> stream() {
        Spliterator<I> spliterator = backingIterable.spliterator();
        return StreamSupport.stream(() -> spliterator, spliterator.characteristics(), false).map(each -> each == null ? null : converter.apply(each));
    }

    public Iterable<I> getHandle() {
        return backingIterable;
    }

    public Function<I, O> getConverter() {
        return converter;
    }

    public boolean isEmpty() {
        return !backingIterable.iterator().hasNext();
    }

}
