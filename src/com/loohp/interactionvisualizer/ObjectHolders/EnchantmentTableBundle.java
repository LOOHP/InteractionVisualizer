package com.loohp.interactionvisualizer.ObjectHolders;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.Managers.EnchantmentManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.SoundManager;
import com.loohp.interactionvisualizer.Utils.ChatColorUtils;
import com.loohp.interactionvisualizer.Utils.CustomStringUtils;
import com.loohp.interactionvisualizer.Utils.RomanNumberUtils;

import net.md_5.bungee.api.ChatColor;

public class EnchantmentTableBundle {
	
	Plugin plugin;
	Block block;
	Location location;
	Optional<Item> item;
	boolean animationPlaying;
	Player enchanter;
	List<Player> players;
	char arrow;
	
	public EnchantmentTableBundle(Player enchanter, Block block, List<Player> players) {
		this.plugin = InteractionVisualizer.plugin;
		this.block = block;
		this.location = block.getLocation().clone();
		this.item = Optional.empty();
		this.animationPlaying = false;
		this.players = players;
		this.enchanter = enchanter;
		this.arrow = '\u27f9';
	}
	
	@SuppressWarnings("deprecation")
	public void playEnchantAnimation(Map<Enchantment, Integer> enchantsToAdd, int expCost, ItemStack itemstack) {		
		Item item = this.item.get();
		if (item.isLocked()) {
			return;
		}
		if (animationPlaying) {
			return;
		}
		
		animationPlaying = true;
		
		if (!this.item.isPresent()) {
			this.item = Optional.of(new Item(location.clone().add(0.5, 1.3, 0.5)));
			PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		}
		
		item.setItemStack(itemstack);
		item.setGravity(false);
		item.setLocked(true);
		item.setVelocity(new Vector(0.0, 0.05, 0.0));
		PacketManager.updateItem(item);
		for (Player each : InteractionVisualizer.itemDrop) {
			each.spawnParticle(Particle.PORTAL, location.clone().add(0.5, 2.6, 0.5), 200);
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			item.teleport(location.clone().add(0.5, 2.3, 0.5));
			item.setVelocity(new Vector(0, 0, 0));
			PacketManager.updateItem(item);
		}, 20);
			
		List<ArmorStand> stands = new LinkedList<ArmorStand>();
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			Location standloc = item.getLocation().add(0.0, 0.5, 0.0);
			for (Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
				Enchantment ench = entry.getKey();
				int level = entry.getValue();
				String str = ChatColorUtils.translateAlternateColorCodes('&', EnchantmentManager.getEnchConfig().getString("Enchantments." + ench.getName()));
				String enchantmentName = str == null ? CustomStringUtils.capitalize(ench.getName().toLowerCase().replace("_", " ")) : str;
				if (enchantmentName == null) {
					continue;
				}
				ArmorStand stand = new ArmorStand(standloc);
				String display = ench.getMaxLevel() == 1 && level == 1 ? enchantmentName : enchantmentName + " " + ChatColor.AQUA + RomanNumberUtils.toRoman(entry.getValue());
				display = ench.isCursed() ? ChatColor.RED + display : ChatColor.AQUA + display;
				stand.setCustomName(display);
				stand.setCustomNameVisible(true);
				setStand(stand);
				PacketManager.sendArmorStandSpawn(InteractionVisualizer.itemDrop, stand);
				stands.add(stand);
				standloc.add(0.0, 0.3, 0.0);
			}
			
			ArmorStand stand = new ArmorStand(standloc);
			String levelStr = ChatColorUtils.translateAlternateColorCodes('&', EnchantmentManager.getEnchConfig().getString("Translations.LEVEL"));
			stand.setCustomName(ChatColor.GREEN + levelStr + " " + arrow + " " + expCost);
			stand.setCustomNameVisible(true);
			setStand(stand);
			PacketManager.sendArmorStandSpawn(InteractionVisualizer.itemDrop, stand);
			stands.add(stand);
			
			PacketManager.updateItem(item);
		}, 50);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			while (!stands.isEmpty()) {
				ArmorStand stand = stands.remove(0);
				PacketManager.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
			}
			item.setGravity(true);
			PacketManager.updateItem(item);
		}, 90);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			item.teleport(location.clone().add(0.5, 1.3, 0.5));
			item.setGravity(false);
			PacketManager.updateItem(item);
			item.setLocked(false);
			animationPlaying = false;
		}, 98);
	}
	
	public void playPickUpAnimation(ItemStack itemstack) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			while (animationPlaying) {
				try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
			}
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!item.isPresent()) {
					return;
				}
				animationPlaying = true;
				Item item = this.item.get();
				item.setLocked(true);
				item.setItemStack(itemstack);
				if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
					animationPlaying = false;
					return;
				}
				
				Vector lift = new Vector(0.0, 0.15, 0.0);
				Vector pickup = enchanter.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(location.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
				item.setVelocity(pickup);
				item.setGravity(true);
				item.setPickupDelay(32767);
				PacketManager.updateItem(item);
				
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					SoundManager.playItemPickup(item.getLocation(), InteractionVisualizer.itemDrop);
					PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
				    this.item = Optional.empty();
				    animationPlaying = false;
				}, 8);
			});
		});
	}
	
	public void playPickUpAnimationAndRemove(ItemStack itemstack, Map<Block, EnchantmentTableBundle> mapToRemoveFrom) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			while (animationPlaying) {
				try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
			}
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (!item.isPresent()) {
					mapToRemoveFrom.remove(block);
					return;
				}
				Item item = this.item.get();
				item.setLocked(true);
				if (itemstack != null && !itemstack.getType().equals(Material.AIR)) {
					animationPlaying = true;
					item.setItemStack(itemstack);
					Vector lift = new Vector(0.0, 0.15, 0.0);
					Vector pickup = enchanter.getEyeLocation().add(0.0, -0.5, 0.0).toVector().subtract(location.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
					item.setVelocity(pickup);
					item.setGravity(true);
					item.setPickupDelay(32767);
					PacketManager.updateItem(item);
					
					Bukkit.getScheduler().runTaskLater(plugin, () -> {
						SoundManager.playItemPickup(item.getLocation(), InteractionVisualizer.itemDrop);
						PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
						animationPlaying = false;
						mapToRemoveFrom.remove(block);
					}, 8);
				} else {
					Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
						while (this.item.isPresent()) {
							try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
						}
						Bukkit.getScheduler().runTask(plugin, () -> {
							mapToRemoveFrom.remove(block);
						});
					});
				}
			});
		});
	}
	
	public void setItemStack(ItemStack itemstack) {
		if (animationPlaying) {
			return;
		}
		if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
			clearItemStack();
			return;
		}
		if (this.item.isPresent()) {
			this.item.get().setItemStack(itemstack);
			PacketManager.updateItem(item.get());
		} else {
			this.item = Optional.of(new Item(location.clone().add(0.5, 1.3, 0.5)));
			this.item.get().setItemStack(itemstack);
			PacketManager.sendItemSpawn(players, item.get());
			PacketManager.updateItem(item.get());
		}
	}
	
	public void clearItemStack() {
		if (this.item.isPresent()) {
			PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item.get());
			this.item = Optional.empty();
		}
	}
	
	private void setStand(ArmorStand stand) {
		stand.setMarker(true);
		stand.setSmall(true);
		stand.setVisible(true);
		stand.setInvulnerable(true);
		stand.setBasePlate(false);
		stand.setVisible(false);
	}
	
	public ItemStack getItemStack() {
		return item.isPresent() ? item.get().getItemStack() : null;
	}
	
	public boolean isAnimationPlaying() {
		return animationPlaying;
	}
	
	public Player getEnchanter() {
		return enchanter;
	}
	
	public List<Player> getViewers() {
		return players;
	}
	
	public Block getBlock() {
		return block;
	}

}
