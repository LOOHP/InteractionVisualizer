package com.loohp.interactionvisualizer.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class ChatColorUtils {
	
	private static Set<Character> colors = new HashSet<Character>();
	private static Pattern colorFormating = Pattern.compile("(?=(?<!\\\\)|(?<=\\\\\\\\))\\[[^\\]]*?color=#[0-9a-fA-F]{6}[^\\[]*?\\]");
	
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
	
    public static String hexToColorCode(String hex) {
    	if (hex == null) {
    		return hex;
    	}
    	
    	int pos = hex.indexOf("#");
    	if (pos < 0 || hex.length() < (pos + 7)) {
    		return "§x§F§F§F§F§F§F";
    	}
    	return "§x§" + String.valueOf(hex.charAt(1)) + "§" + String.valueOf(hex.charAt(2)) + "§" + String.valueOf(hex.charAt(3)) + "§" + String.valueOf(hex.charAt(4)) + "§" + String.valueOf(hex.charAt(5)) + "§" + String.valueOf(hex.charAt(6));
    }
    
    public static String translatePluginColorFormatting(String text) {
    	while (true) {
    		Matcher matcher = colorFormating.matcher(text);
    		
    		if (matcher.find()) {
	    	    String foramtedColor = matcher.group().toLowerCase();
	    	    int start = matcher.start();
	    	    int pos = foramtedColor.indexOf("color");
	    	    int absPos = text.indexOf("color", start);
	    	    int end = matcher.end();
	    	    
	    	    if (pos < 0) {
	    	    	continue;
	    	    }
	
	    	    String colorCode = hexToColorCode(foramtedColor.substring(pos + 6, pos + 13));
	    	    
	    	    StringBuilder sb = new StringBuilder(text);
	    	    sb.insert(end, colorCode);
	    	    
	    	    sb.delete(absPos, absPos + 13);

	    	    while (sb.charAt(absPos) == ',' || sb.charAt(absPos) == ' ') {
	    	    	sb.deleteCharAt(absPos);
	    	    }
	    	    
	    	    while (sb.charAt(absPos - 1) == ',' || sb.charAt(absPos - 1) == ' ') {
	    	    	sb.deleteCharAt(absPos - 1);
	    	    	absPos--;
	    	    }
	    	    
	    	    if (sb.charAt(absPos) == ']' && sb.charAt(absPos - 1) == '[') {
	    	    	sb.deleteCharAt(absPos - 1);
	    	    	sb.deleteCharAt(absPos - 1);
	    	    }
	    	    
	    	    text = sb.toString();	    	    
    		} else {
    			break;
    		}
    	}

    	return text;
    }
    
    public static String translateAlternateColorCodes(char code, String text) {    	
		if (text == null) {
			return text;
		}
		
		if (text.length() < 2) {
        	return text;
        }
		
		if (InteractionVisualizer.version.isPost1_16()) {
    		text = translatePluginColorFormatting(text);
    	}
        
        for (int i = 0; i < text.length() - 1; i++) {
        	if (text.charAt(i) == code) {
        		if (text.charAt(i + 1) == 'x' && text.length() > (i + 14)) {
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
