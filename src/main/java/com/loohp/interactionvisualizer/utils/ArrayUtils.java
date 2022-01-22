package com.loohp.interactionvisualizer.utils;

import java.util.Base64;
import java.util.Comparator;
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

    public static <T> List<T> putToArrayList(Map<Integer, T> mapping, List<T> list) {
        int size = mapping.keySet().stream().max(Comparator.naturalOrder()).orElse(-1) + 1;
        for (int i = 0; i < size; i++) {
            list.add(mapping.getOrDefault(i, null));
        }
        return list;
    }

    public static <T> Map<Integer, T> putToMap(List<T> list, Map<Integer, T> mapping) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            mapping.put(i, t);
        }
        return mapping;
    }

}
