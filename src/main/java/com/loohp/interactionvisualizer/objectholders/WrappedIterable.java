package com.loohp.interactionvisualizer.objectholders;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WrappedIterable<I, O> implements Iterable<O> {
	
	private Iterable<I> backingIterable;
	private Function<I, O> converter;
	
	public WrappedIterable(Iterable<I> backingCollection, Function<I, O> converter) {
		this.backingIterable = backingCollection;
		this.converter = converter;
	}

	@Override
	public Iterator<O> iterator() {
		return new Iterator<O>() {
			
			private Iterator<I> itr = backingIterable.iterator();
			
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
		return StreamSupport.stream(backingIterable.spliterator(), false).map(each -> each == null ? null : converter.apply(each));
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
