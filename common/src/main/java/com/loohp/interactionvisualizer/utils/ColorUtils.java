/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.stream.Stream;

public class ColorUtils {

    private static final BiMap<ChatColor, Color> COLORS = HashBiMap.create();

    private static final boolean CHAT_COLOR_HAS_GET_COLOR;

    static {
        COLORS.put(ChatColor.BLACK, new Color(0x000000));
        COLORS.put(ChatColor.DARK_BLUE, new Color(0x0000AA));
        COLORS.put(ChatColor.DARK_GREEN, new Color(0x00AA00));
        COLORS.put(ChatColor.DARK_AQUA, new Color(0x00AAAA));
        COLORS.put(ChatColor.DARK_RED, new Color(0xAA0000));
        COLORS.put(ChatColor.DARK_PURPLE, new Color(0xAA00AA));
        COLORS.put(ChatColor.GOLD, new Color(0xFFAA00));
        COLORS.put(ChatColor.GRAY, new Color(0xAAAAAA));
        COLORS.put(ChatColor.DARK_GRAY, new Color(0x555555));
        COLORS.put(ChatColor.BLUE, new Color(0x05555FF));
        COLORS.put(ChatColor.GREEN, new Color(0x55FF55));
        COLORS.put(ChatColor.AQUA, new Color(0x55FFFF));
        COLORS.put(ChatColor.RED, new Color(0xFF5555));
        COLORS.put(ChatColor.LIGHT_PURPLE, new Color(0xFF55FF));
        COLORS.put(ChatColor.YELLOW, new Color(0xFFFF55));
        COLORS.put(ChatColor.WHITE, new Color(0xFFFFFF));

        CHAT_COLOR_HAS_GET_COLOR = Stream.of(ChatColor.class.getMethods()).anyMatch(each -> each.getName().equalsIgnoreCase("getColor") && each.getReturnType().equals(Color.class));
    }

    public static ChatColor toChatColor(String str) {
        try {
            if (str.length() < 2) {
                return null;
            }
            if (str.charAt(1) == 'x' && str.length() > 13) {
                return ChatColor.of("#" + str.charAt(3) + str.charAt(5) + str.charAt(7) + str.charAt(9) + str.charAt(11) + str.charAt(13));
            } else {
                return ChatColor.getByChar(str.charAt(1));
            }
        } catch (Throwable e) {
            return null;
        }
    }

    public static Color getColor(ChatColor chatcolor) {
        if (CHAT_COLOR_HAS_GET_COLOR) {
            return chatcolor.getColor();
        } else {
            Color color = COLORS.get(chatcolor);
            return color == null ? Color.white : color;
        }
    }

    public static ChatColor getLegacyChatColor(Color color) {
        ChatColor chatcolor = COLORS.inverse().get(color);
        return chatcolor == null ? ChatColor.WHITE : chatcolor;
    }

    public static Color hex2Rgb(String colorStr) {
        return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf(colorStr.substring(3, 5), 16),
                         Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    public static String rgb2Hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color getFirstColor(String str) {
        String colorStr = ChatColorUtils.getFirstColors(str);
        if (colorStr.length() > 1) {
            ChatColor chatColor = toChatColor(colorStr);
            if (chatColor != null && ChatColorUtils.isColor(chatColor)) {
                return CHAT_COLOR_HAS_GET_COLOR ? chatColor.getColor() : getColor(chatColor);
            }
        }
        return null;
    }

    public static NamedTextColor toNamedTextColor(ChatColor color) {
        Color awtColor = getColor(color);
        return NamedTextColor.nearestTo(TextColor.color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
    }

    public static TextColor toTextColor(ChatColor color) {
        Color awtColor = getColor(color);
        return TextColor.color(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    public static ChatColor toChatColor(NamedTextColor color) {
        return getLegacyChatColor(new Color(color.value()));
    }

}
