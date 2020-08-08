package com.loohp.interactionvisualizer.Utils;

import java.util.Arrays;
import java.util.List;

public class CustomStringUtils {

	public static String capitalize(String str) {
		List<String> parts = Arrays.asList(str.split(" "));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.size(); i++) {
			String each = parts.get(i);
			String firstLetter = each.substring(0, 1);
			sb.append(firstLetter.toUpperCase() + each.substring(1) + " ");
		}
		return sb.substring(0, sb.length() - 1);
	}
}
