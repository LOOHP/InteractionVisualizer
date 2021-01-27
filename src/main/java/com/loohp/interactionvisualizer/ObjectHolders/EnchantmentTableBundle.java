package com.loohp.interactionvisualizer.ObjectHolders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
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
	
	protected static Method playEnchantAnimation;
	protected static Method playPickUpAnimation;
	protected static Method playPickUpRemoveAnimation;
	
	static {
		try {
			Class<?> clazz = EnchantmentTableBundle.class;
			playEnchantAnimation = clazz.getDeclaredMethod("playEnchantAnimationMethod", Map.class, Integer.class, ItemStack.class);
			playPickUpAnimation = clazz.getDeclaredMethod("playPickUpAnimationMethod", ItemStack.class);
			playPickUpRemoveAnimation = clazz.getDeclaredMethod("playPickUpAnimationAndRemoveMethod", ItemStack.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	private Plugin plugin;
	private Block block;
	private Location location;
	private Optional<Item> item;
	private Player enchanter;
	private Collection<Player> players;
	private char arrow;
	
	private List<Item> createdItems;
	
	private ConcurrentLinkedQueue<MethodWrapper<CompletableFuture<Boolean>>> methodQueue;
	private CompletableFuture<Boolean> activeMethod;
	
	private final int timerTaskId;
	
	public EnchantmentTableBundle(Player enchanter, Block block, Collection<Player> players) {
		this.plugin = InteractionVisualizer.plugin;
		this.block = block;
		this.location = block.getLocation().clone();
		this.item = Optional.empty();
		this.players = players;
		this.enchanter = enchanter;
		this.arrow = '\u27f9';
		methodQueue = new ConcurrentLinkedQueue<>();
		activeMethod = null;
		createdItems = new ArrayList<>();
		timerTaskId = run();
	}
	
	private int run() {
		return Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			if (activeMethod == null || activeMethod.isDone() || activeMethod.isCompletedExceptionally()) {
				try {
					if (activeMethod != null && activeMethod.isDone() && activeMethod.getNow(false)) {
						for (Item item : createdItems) {
							PacketManager.removeItem(players, item);
						}
						Bukkit.getScheduler().cancelTask(timerTaskId);
					} else {
						MethodWrapper<CompletableFuture<Boolean>> method = methodQueue.poll();
						if (method != null) {
							activeMethod = method.execute();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 1).getTaskId();
	}
	
	@SuppressWarnings("deprecation")
	protected CompletableFuture<Boolean> playEnchantAnimationMethod(Map<Enchantment, Integer> enchantsToAdd, Integer expCost, ItemStack itemstack) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		
		if (item.isPresent() && item.get().isLocked()) {
			future.complete(false);
			return future;
		}
		
		if (!this.item.isPresent()) {
			this.item = Optional.of(new Item(location.clone().add(0.5, 1.3, 0.5)));
			createdItems.add(this.item.get());
			PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), item.get());
		}
		
		Item item = this.item.get();
		
		item.setItemStack(itemstack);
		item.setGravity(false);
		item.setLocked(true);
		item.setVelocity(new Vector(0.0, 0.05, 0.0));
		PacketManager.updateItem(item);
		for (Player each : InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP)) {
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
				PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), stand);
				stands.add(stand);
				standloc.add(0.0, 0.3, 0.0);
			}
			
			ArmorStand stand = new ArmorStand(standloc);
			String levelStr = ChatColorUtils.translateAlternateColorCodes('&', EnchantmentManager.getEnchConfig().getString("Translations.LEVEL"));
			stand.setCustomName(ChatColor.GREEN + levelStr + " " + arrow + " " + expCost);
			stand.setCustomNameVisible(true);
			setStand(stand);
			PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP), stand);
			stands.add(stand);
			
			PacketManager.updateItem(item);
		}, 50);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			while (!stands.isEmpty()) {
				ArmorStand stand = stands.remove(0);
				PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
			}
			item.setGravity(true);
			PacketManager.updateItem(item);
		}, 90);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			item.teleport(location.clone().add(0.5, 1.3, 0.5));
			item.setGravity(false);
			PacketManager.updateItem(item);
			item.setLocked(false);
			future.complete(false);
		}, 98);
		return future;
	}
	
	public void playEnchantAnimation(Map<Enchantment, Integer> enchantsToAdd, Integer expCost, ItemStack itemstack) {
		methodQueue.add(new MethodWrapper<>(playEnchantAnimation, this, enchantsToAdd, expCost, itemstack));
	}
	
	protected CompletableFuture<Boolean> playPickUpAnimationMethod(ItemStack itemstack) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		
		if (!item.isPresent()) {
			future.complete(false);
			return future;
		}
		Item item = this.item.get();
		item.setLocked(true);
		item.setItemStack(itemstack);
		if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
			future.complete(false);
			return future;
		}
		
		Vector lift = new Vector(0.0, 0.15, 0.0);
		Vector pickup = enchanter.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(location.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
		item.setVelocity(pickup);
		item.setGravity(true);
		item.setPickupDelay(32767);
		PacketManager.updateItem(item);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP));
			PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
			createdItems.remove(item);
		    this.item = Optional.empty();
		    future.complete(false);
		}, 8);
		return future;
	}
	
	public void playPickUpAnimation(ItemStack itemstack) {
		methodQueue.add(new MethodWrapper<>(playPickUpAnimation, this, itemstack));
	}
	
	protected CompletableFuture<Boolean> playPickUpAnimationAndRemoveMethod(ItemStack itemstack, Map<Block, EnchantmentTableBundle> mapToRemoveFrom) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		
		if (!item.isPresent()) {
			mapToRemoveFrom.remove(block);
			future.complete(true);
			return future;
		}
		Item item = this.item.get();
		item.setLocked(true);
		if (itemstack != null && !itemstack.getType().equals(Material.AIR)) {
			item.setItemStack(itemstack);
			Vector lift = new Vector(0.0, 0.15, 0.0);
			Vector pickup = enchanter.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(location.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
			item.setVelocity(pickup);
			item.setGravity(true);
			item.setPickupDelay(32767);
			PacketManager.updateItem(item);
			
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP));
				PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
				createdItems.remove(item);
				mapToRemoveFrom.remove(block);
				future.complete(true);
			}, 8);
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				while (this.item.isPresent()) {
					try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
				}
				Bukkit.getScheduler().runTask(plugin, () -> {
					mapToRemoveFrom.remove(block);
					future.complete(true);
				});
			});
		}
		return future;
	}
	
	public void playPickUpAnimationAndRemove(ItemStack itemstack, Map<Block, EnchantmentTableBundle> mapToRemoveFrom) {
		methodQueue.add(new MethodWrapper<>(playPickUpRemoveAnimation, this, itemstack, mapToRemoveFrom));
	}
	
	public void setItemStack(ItemStack itemstack) {
		if (!methodQueue.isEmpty() || !(activeMethod == null || activeMethod.isDone() || activeMethod.isCompletedExceptionally())) {
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
			createdItems.add(this.item.get());
			this.item.get().setItemStack(itemstack);
			PacketManager.sendItemSpawn(players, item.get());
			PacketManager.updateItem(item.get());
		}
	}
	
	public void clearItemStack() {
		if (this.item.isPresent()) {
			PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item.get());
			createdItems.remove(item.get());
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
	
	public Player getEnchanter() {
		return enchanter;
	}
	
	public Collection<Player> getViewers() {
		return players;
	}
	
	public Block getBlock() {
		return block;
	}

}
