package com.loohp.interactionvisualizer.Debug;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.MCVersion;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class Debug implements Listener {
	
	private ItemStack bone;
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
			event.getPlayer().sendMessage(ChatColor.BLUE + "InteractionVisualizer " + InteractionVisualizer.plugin.getDescription().getVersion() + " is running!");
		}
	}
	
	@EventHandler
	public void onCraft(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getType().equals(InventoryType.WORKBENCH)) {
			ItemStack i1 = event.getView().getItem(1);
			if (i1 == null || !i1.getType().equals(Material.BONE)) {
				return;
			}
			ItemStack i2 = event.getView().getItem(2);
			if (i2 == null || !i2.getType().equals(Material.BONE)) {
				return;
			}
			ItemStack i3 = event.getView().getItem(3);
			if (i3 == null || !i3.getType().equals(Material.BONE_BLOCK)) {
				return;
			}
			ItemStack i4 = event.getView().getItem(4);
			if (i4 == null || !i4.getType().equals(Material.BONE)) {
				return;
			}
			ItemStack i5 = event.getView().getItem(5);
			if (i5 == null || !i5.getType().equals(Material.BLAZE_ROD)) {
				return;
			}
			ItemStack i6 = event.getView().getItem(6);
			if (i6 == null || !i6.getType().equals(Material.BONE)) {
				return;
			}
			ItemStack i7 = event.getView().getItem(7);
			if (i7 == null || !i7.getType().equals(Material.BONE_BLOCK)) {
				return;
			}
			ItemStack i8 = event.getView().getItem(8);
			if (i8 == null || !i8.getType().equals(Material.BONE)) {
				return;
			}
			ItemStack i9 = event.getView().getItem(9);
			if (i9 == null || !i9.getType().equals(Material.BONE)) {
				return;
			}
			event.getView().setItem(0, bone.clone());
		}
	}
	
	public Debug() {
		if (InteractionVisualizer.version.isNewerOrEqualTo(MCVersion.V1_16_4)) {
			Bukkit.removeRecipe(new NamespacedKey(InteractionVisualizer.plugin, "nana_bone"));
		}
		
		if (InteractionVisualizer.plugin.getConfig().contains("Special.b")) {
			if (!InteractionVisualizer.plugin.getConfig().getBoolean("Special.b")) {
				return;
			}
		}
		
		bone = new ItemStack(Material.BONE, 1);
		ItemMeta meta = bone.getItemMeta();
		TextComponent text = new TextComponent("Nana's Bone");
		text.setColor(ChatColor.YELLOW);
		List<String> lore = new ArrayList<String>();
		lore.add("\u00a77Lost \u00a76In-\u00a7dMaginary~~");
		lore.add("");
		lore.add("\u00a76https://www.instagram.com/narliar/");
		lore.add("");
		lore.add("\u00a77EasterEgg tribute to the IV author's best friend");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.setDisplayName(text.toLegacyText());
		bone.setItemMeta(meta);
		bone.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
	}

}
