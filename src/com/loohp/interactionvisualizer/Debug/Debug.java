package com.loohp.interactionvisualizer.Debug;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.NBTUtils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class Debug implements Listener {
	
	@EventHandler
	public void onJoinPluginActive(PlayerJoinEvent event) {
		if (event.getPlayer().getName().equals("LOOHP") || event.getPlayer().getName().equals("AppLEskakE")) {
			event.getPlayer().sendMessage(ChatColor.BLUE + "InteractionVisualizer " + InteractionVisualizer.plugin.getDescription().getVersion() + " is running!");
		}
	}
	
	public Debug() {
		NamespacedKey key = new NamespacedKey(InteractionVisualizer.plugin, "nana_bone");
		
		InteractionVisualizer.plugin.getServer().removeRecipe(key);
		
		if (InteractionVisualizer.plugin.getConfig().contains("Special.b")) {
			if (!InteractionVisualizer.plugin.getConfig().getBoolean("Special.b")) {
				return;
			}
		}
		
		ItemStack bone = new ItemStack(Material.BONE, 1);
		ItemMeta meta = bone.getItemMeta();
		TextComponent text = new TextComponent("Nana's ");
		text.setColor(ChatColor.GOLD);
		TranslatableComponent trans = new TranslatableComponent("item.minecraft.bone");
		trans.setColor(ChatColor.YELLOW);
		text.addExtra(trans);
		List<String> lore = new ArrayList<String>();
		lore.add("§7Lost §6In-§dMaginary~~");
		lore.add("");
		lore.add("§6https://www.instagram.com/narliar/");
		lore.add("");
		lore.add("§7EasterEgg tribute to the IV author's best friend");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		bone.setItemMeta(meta);
		bone = NBTUtils.set(bone, ComponentSerializer.toString(text), "display", "Name");
		bone.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		
		ShapedRecipe recipe = new ShapedRecipe(key, bone);

		recipe.shape("$$#", "$%$", "#$$");
		recipe.setIngredient('#', Material.BONE_BLOCK);
		recipe.setIngredient('%', Material.BLAZE_ROD);
		recipe.setIngredient('$', Material.BONE);

		InteractionVisualizer.plugin.getServer().addRecipe(recipe);		
	}

}
