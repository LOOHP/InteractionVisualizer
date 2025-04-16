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

import com.loohp.interactionvisualizer.nms.NMS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemNameUtils {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    public static Component getDisplayName(ItemStack itemstack) {
        return getDisplayName(itemstack, ChatColor.WHITE);
    }

    public static  Component getDisplayName(ItemStack itemstack, ChatColor defaultRarityColor) {
        if (itemstack == null) {
            itemstack = AIR.clone();
        }

        ItemMeta itemMeta = itemstack.getItemMeta();
        Component component = Component.empty().append(NMS.getInstance().getItemHoverName(itemstack));

        if (itemMeta != null && itemMeta.hasDisplayName()) {
            component = component.decorate(TextDecoration.ITALIC);
        }

        ChatColor rarityChatColor = NMS.getInstance().getRarityColor(itemstack);
        if (rarityChatColor.equals(ChatColor.WHITE)) {
            rarityChatColor = defaultRarityColor;
        }

        return component.colorIfAbsent(ColorUtils.toTextColor(rarityChatColor));
    }

}
