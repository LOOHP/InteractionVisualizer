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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SynchronizedFilteredCollection<E> implements Collection<E> {

    /**
     * The predicate is evaluated for all elements once only for methods that return an iteration of elements.
     * Meaning it will not return a live view of the underlying collection.<br>
     * <br>
     * However, the SynchronizedFilteredCollection itself is a filtered live view of the underlying collection.<br>
     * <br>
     * "backingCollection" can be another SynchronizedFilteredCollection, in this case their locks will share.
     */
    public static <E> SynchronizedFilteredCollection<E> filter(Collection<E> backingCollection, Predicate<E> predicate) {
        return new SynchronizedFilteredCollection<E>(backingCollection, predicate);
    }

    private final Collection<E> backingCollection;
    private final Predicate<E> predicate;
    private final ReentrantReadWriteLock lock;

    private SynchronizedFilteredCollection(Collection<E> backingCollection, Predicate<E> predicate) {
        this.backingCollection = backingCollection;
        this.predicate = predicate;
        this.lock = acquireLock();
    }

    private ReentrantReadWriteLock acquireLock() {
        if (backingCollection instanceof SynchronizedFilteredCollection) {
            return ((SynchronizedFilteredCollection<E>) backingCollection).acquireLock();
        } else {
            return new ReentrantReadWriteLock();
        }
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    @Override
    public int size() {
        try {
            lock.readLock().lock();
            return (int) backingCollection.stream().filter(predicate).count();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return backingCollection.stream().noneMatch(predicate);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        try {
            lock.readLock().lock();
            return backingCollection.stream().filter(predicate).anyMatch(each -> Objects.equals(each, o));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<E> itr;
        try {
            lock.readLock().lock();
            itr = backingCollection.stream().filter(predicate).collect(Collectors.toList()).iterator();
        } finally {
            lock.readLock().unlock();
        }
        return new Iterator<E>() {

            private E currentElement = null;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public E next() {
                return currentElement = itr.next();
            }

            @Override
            public void remove() {
                if (currentElement == null) {
                    throw new IllegalStateException("Call itr.next() first");
                }
                backingCollection.remove(currentElement);
            }

        };
    }

    @Override
    public Object[] toArray() {
        try {
            lock.readLock().lock();
            return backingCollection.stream().filter(predicate).toArray();
        } finally {
            lock.readLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        try {
            lock.readLock().lock();
            return backingCollection.stream().filter(predicate).toArray(size -> a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean add(E e) {
        if (predicate.test(e)) {
            try {
                lock.writeLock().lock();
                return backingCollection.add(e);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        try {
            lock.writeLock().lock();
            return backingCollection.remove(o);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<E> list;
        try {
            lock.readLock().lock();
            list = backingCollection.stream().filter(predicate).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
        for (Object o : c) {
            if (!list.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean flag = false;
        for (E e : c) {
            try {
                lock.writeLock().lock();
                if (add(e)) {
                    flag = true;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return flag;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        try {
            lock.writeLock().lock();
            return backingCollection.removeAll(c);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Predicate<E> test = predicate.and(filter);
        try {
            lock.writeLock().lock();
            return backingCollection.removeIf(test);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean flag = false;
        try {
            lock.writeLock().lock();
            Iterator<E> itr = backingCollection.iterator();
            while (itr.hasNext()) {
                E e = itr.next();
                if (predicate.test(e) && !c.contains(e)) {
                    itr.remove();
                    flag = true;
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
        return flag;
    }

    @Override
    public void clear() {
        try {
            lock.writeLock().lock();
            backingCollection.removeIf(predicate);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        try {
            lock.readLock().lock();
            return backingCollection.stream().filter(predicate).collect(Collectors.toList()).spliterator();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Stream<E> stream() {
        try {
            lock.readLock().lock();
            return backingCollection.stream().filter(predicate).collect(Collectors.toList()).stream();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Stream<E> parallelStream() {
        try {
            lock.readLock().lock();
            return backingCollection.parallelStream().filter(predicate).collect(Collectors.toList()).parallelStream();
        } finally {
            lock.readLock().unlock();
        }
    }

}
