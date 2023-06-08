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

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ArrayUtils {

    public static String toBase64String(byte[] array) {
        return Base64.getEncoder().encodeToString(array);
    }

    public static byte[] fromBase64String(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public static byte[] reverse(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = temp;
        }
        return bytes;
    }

    public static <L extends List<T>, T> L putToArrayList(Map<Integer, T> mapping, L list) {
        int size = mapping.keySet().stream().mapToInt(i -> i).max().orElse(-1) + 1;
        for (int i = 0; i < size; i++) {
            list.add(mapping.getOrDefault(i, null));
        }
        return list;
    }

    public static <M extends Map<Integer, T>, T> M putToMap(List<T> list, M mapping) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            mapping.put(i, t);
        }
        return mapping;
    }

}
