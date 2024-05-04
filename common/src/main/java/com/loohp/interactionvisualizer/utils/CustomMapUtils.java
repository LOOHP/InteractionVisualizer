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

package com.loohp.interactionvisualizer.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class CustomMapUtils {

    public static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        List<Map.Entry<K, V>> sortedEntries = new ArrayList<Map.Entry<K, V>>(map.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());
        return sortedEntries;
    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValue(Map<K, V> map) {
        LinkedHashMap<K, V> linkedmap = new LinkedHashMap<>();
        entriesSortedByValues(map).forEach(entry -> linkedmap.put(entry.getKey(), entry.getValue()));
        return linkedmap;
    }

    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortMapByValueReverse(Map<K, V> map) {
        LinkedHashMap<K, V> linkedmap = new LinkedHashMap<>();
        List<Map.Entry<K, V>> list = entriesSortedByValues(map);
        for (ListIterator<Map.Entry<K, V>> itr = list.listIterator(list.size()); itr.hasPrevious();) {
            Map.Entry<K, V> entry = itr.previous();
            linkedmap.put(entry.getKey(), entry.getValue());
        }
        return linkedmap;
    }

}
