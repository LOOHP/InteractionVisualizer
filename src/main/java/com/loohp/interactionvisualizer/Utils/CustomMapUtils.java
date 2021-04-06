package com.loohp.interactionvisualizer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

public class CustomMapUtils {
	
	public static <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {
		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());
		Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() {
			@Override
			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});
		return sortedEntries;
	}
	
	public static <K,V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map) {
		LinkedHashMap<K, V> linkedmap = new LinkedHashMap<>();
		entriesSortedByValues(map).stream().forEach((entry) -> linkedmap.put(entry.getKey(), entry.getValue()));
		return linkedmap;
	}
	
	public static <K,V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValueReverse(Map<K, V> map) {
		LinkedHashMap<K, V> linkedmap = new LinkedHashMap<>();
		List<Entry<K, V>> list = new ArrayList<Entry<K, V>>(sortMapByValue(map).entrySet());
		ListIterator<Entry<K, V>> itr = list.listIterator(list.size());
		while (itr.hasPrevious()) {
			Entry<K, V> entry = itr.previous();
			linkedmap.put(entry.getKey(), entry.getValue());
		}
		return linkedmap;
	}
}
