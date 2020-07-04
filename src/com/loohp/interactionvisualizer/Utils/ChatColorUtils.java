package com.loohp.interactionvisualizer.Utils;

import java.util.HashSet;
import java.util.Set;

public class ChatColorUtils {
	
	private static Set<Character> colors = new HashSet<Character>();
	
	static {
		colors.add('0');
		colors.add('1');
		colors.add('2');
		colors.add('3');
		colors.add('4');
		colors.add('5');
		colors.add('6');
		colors.add('7');
		colors.add('8');
		colors.add('9');
		colors.add('a');
		colors.add('b');
		colors.add('c');
		colors.add('d');
		colors.add('e');
		colors.add('f');
	}
	
	public static String translateAlternateColorCodes(char code, String text) {
        if (text.length() < 2) {
        	return "";
        }
        
        for (int i = 0; i < text.length() - 1; i++) {
        	if (text.charAt(i) == code) {
        		if (text.charAt(i + 1) == 'x') {
        			String section = text.substring(i, i + 14);
        			String translated = section.replace(code, '§');
        			text = text.replace(section, translated);
        		} else if (colors.contains(text.charAt(i + 1))) {
        			StringBuilder sb = new StringBuilder(text);
        			sb.setCharAt(i, '§');
        			text = sb.toString();
        		}
        	}
        }

        return text;
    }

}
