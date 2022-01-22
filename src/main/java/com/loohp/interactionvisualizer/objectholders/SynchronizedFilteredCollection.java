package com.loohp.interactionvisualizer.objectholders;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SynchronizedFilteredCollection<E> implements Collection<E> {

    /**
     * The provided Collection should already be synchronized.<br>
     * <br>
     * The predicate is evaluated for all elements once only for methods that return an iteration of elements.
     * Meaning it will not return a live view of the underlying collection.<br>
     * <br>
     * However, the SynchronizedFilteredCollection itself is a filtered live view of the underlying collection.
     */
    public static <E> SynchronizedFilteredCollection<E> filterSynchronized(Collection<E> backingCollection, Predicate<E> predicate) {
        return new SynchronizedFilteredCollection<E>(backingCollection, predicate);
    }
    private final Collection<E> backingCollection;
    private final Predicate<E> predicate;
    private final Object lock;

    private SynchronizedFilteredCollection(Collection<E> backingCollection, Predicate<E> predicate) {
        this.backingCollection = backingCollection;
        this.predicate = predicate;
        this.lock = aquireLock();
    }

    private Object aquireLock() {
        if (backingCollection instanceof SynchronizedFilteredCollection) {
            return ((SynchronizedFilteredCollection<E>) backingCollection).aquireLock();
        } else {
            return backingCollection;
        }
    }

    public Object getLock() {
        return lock;
    }

    @Override
    public int size() {
        synchronized (lock) {
            return (int) Math.min(backingCollection.stream().filter(predicate).count(), Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).count() <= 0;
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).anyMatch(each -> Objects.equals(each, o));
        }
    }

    @Override
    public Iterator<E> iterator() {
        synchronized (lock) {
            return new Iterator<E>() {

                private final Iterator<E> itr = backingCollection.stream().filter(predicate).collect(Collectors.toList()).iterator();
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
    }

    @Override
    public Object[] toArray() {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).toArray();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).toArray(size -> a.length >= size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size));
        }
    }

    @Override
    public boolean add(E e) {
        if (predicate.test(e)) {
            return backingCollection.add(e);
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return backingCollection.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        synchronized (lock) {
            Collection<E> list = backingCollection.stream().filter(predicate).collect(Collectors.toList());
            for (Object o : c) {
                if (!list.contains(o)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean flag = false;
        for (E e : c) {
            if (add(e)) {
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return backingCollection.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Predicate<E> test = predicate.and(filter);
        boolean flag = false;
        synchronized (lock) {
            Iterator<E> itr = backingCollection.iterator();
            while (itr.hasNext()) {
                E e = itr.next();
                if (test.test(e)) {
                    itr.remove();
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean flag = false;
        synchronized (lock) {
            Iterator<E> itr = backingCollection.iterator();
            while (itr.hasNext()) {
                E e = itr.next();
                if (predicate.test(e) && !c.contains(e)) {
                    itr.remove();
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public void clear() {
        synchronized (lock) {
            Iterator<E> itr = backingCollection.iterator();
            while (itr.hasNext()) {
                E e = itr.next();
                if (predicate.test(e)) {
                    itr.remove();
                }
            }
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).collect(Collectors.toList()).spliterator();
        }
    }

    @Override
    public Stream<E> stream() {
        synchronized (lock) {
            return backingCollection.stream().filter(predicate).collect(Collectors.toList()).stream();
        }
    }

    @Override
    public Stream<E> parallelStream() {
        synchronized (lock) {
            return backingCollection.parallelStream().filter(predicate).collect(Collectors.toList()).parallelStream();
        }
    }

}
