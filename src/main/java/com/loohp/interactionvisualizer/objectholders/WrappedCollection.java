package com.loohp.interactionvisualizer.objectholders;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

public class WrappedCollection<I, O> implements Collection<O> {
	
	private Collection<I> backingCollection;
	private Function<I, O> converter;
	
	public WrappedCollection(Collection<I> backingCollection, Function<I, O> converter) {
		this.backingCollection = backingCollection;
		this.converter = converter;
	}

	@Override
	public Iterator<O> iterator() {
		return new Iterator<O>() {
			
			private Iterator<I> itr = backingCollection.iterator();
			
			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public O next() {
				return converter.apply(itr.next());
			}
		};
	}
	
	@Override
	public Stream<O> stream() {
		return backingCollection.stream().map(converter);
	}
	
	public int size() {
		return backingCollection.size();
	}
	
	public Collection<I> getHandle() {
		return backingCollection;
	}
	
	public Function<I, O> getConverter() {
		return converter;
	}

	@Override
	public boolean isEmpty() {
		return backingCollection.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return stream().anyMatch(each -> each.equals(o));
	}

	@Override
	public Object[] toArray() {
		return stream().toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		Object[] array = toArray();
		if (a.length < array.length)
            return (T[]) Arrays.copyOf(array, array.length, a.getClass());
        System.arraycopy(array, 0, a, 0, array.length);
        if (a.length > array.length)
            a[array.length] = null;
        return a;
	}

	@Override
	public boolean add(O e) {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!stream().anyMatch(each -> each.equals(o))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends O> c) {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("NMSWrappedCollection is read only");
	}
	
}
