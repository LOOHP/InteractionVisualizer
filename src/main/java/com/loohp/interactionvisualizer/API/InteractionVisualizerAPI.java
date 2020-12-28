package com.loohp.interactionvisualizer.API;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.InteractionVisualizer.Modules;
import com.loohp.interactionvisualizer.Toggle;
import com.loohp.interactionvisualizer.Database.Database;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.Managers.EffectManager;
import com.loohp.interactionvisualizer.Managers.EnchantmentManager;
import com.loohp.interactionvisualizer.Managers.MaterialManager;
import com.loohp.interactionvisualizer.Managers.MusicManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Managers.SoundManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager;
import com.loohp.interactionvisualizer.Managers.TileEntityManager.TileEntityType;

public class InteractionVisualizerAPI {
	
	/**
	Gets the GC period in ticks defined in the config.
	@return The GC period in ticks.
	*/
	public static int getGCPeriod() {
		return InteractionVisualizer.plugin.getConfig().getInt("GarbageCollector.Period");
	}
	
	/**
	Gets the list of tile entity blocks that is within range of a player.
	@return A list of blocks that is within range of at least one player.
	*/
	public static List<Block> getActiveTileEntityBlocks(TileEntityType type) {
		return TileEntityManager.getTileEntites(type);
	}
	
	/**
	Gets all players that has a module enabled for themselves.
	@return A list of players.
	*/
	public static List<Player> getPlayerModuleList(Modules module) {
		switch (module) {
		case HOLOGRAM:
			return Collections.unmodifiableList(InteractionVisualizer.holograms);
		case ITEMDROP:
			return Collections.unmodifiableList(InteractionVisualizer.itemDrop);
		case ITEMSTAND:
			return Collections.unmodifiableList(InteractionVisualizer.itemStand);
		}
		return null;
	}
	
	/**
	Check if an online player has a module enabled.
	@return true/false.
	*/
	public static boolean hasPlayerEnabledModule(Player player, Modules module) {
		switch (module) {
		case HOLOGRAM:
			return InteractionVisualizer.holograms.contains(player);
		case ITEMDROP:
			return InteractionVisualizer.itemDrop.contains(player);
		case ITEMSTAND:
			return InteractionVisualizer.itemStand.contains(player);
		}
		return false;
	}
	
	/**
	Check if player has a module enabled.
	@return true/false.
	*/
	public static boolean hasPlayerEnabledModule(UUID uuid, Modules module) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			return hasPlayerEnabledModule(player, module);
		} else {
			return Database.getPlayerInfo(uuid).get(module);
		}
	}
	
	/**
	Toggle a module for an online player.
	@return true/false - the new toggle status.
	*/
	public static boolean togglePlayerModule(Player player, Modules module) {
		return Toggle.toggle(player, module);
	}
	
	public static enum ConfiguationType {
		MAIN("config.yml"),
		MATERIAL("material.yml"),
		EFFECTS("effect.yml"),
		ENCHANTMENT("enchantment.yml"),
		MUSIC("music.yml");
		
		String fileName;
		
		ConfiguationType(String fileName) {
			this.fileName = fileName;
		}
		
		public String getConfigFileName() {
			return fileName;
		}
		
		public static ConfiguationType fromConfigFileName(String filename) {
			for (ConfiguationType cfgtype : ConfiguationType.values()) {
				if (cfgtype.getConfigFileName().equalsIgnoreCase(filename)) {
					return cfgtype;
				}
			}
			return null;
		}
	}
	
	/**
	Get InteractionVisualizer's configurations.
	@return The FileConfiguration of the given ConfiguationType.
	*/
	public static FileConfiguration getConfig(ConfiguationType configType) {
		switch (configType) {
		case EFFECTS:
			return EffectManager.getEffectConfig();
		case ENCHANTMENT:
			return EnchantmentManager.getEnchConfig();
		case MUSIC:
			return MusicManager.getMusicConfig();
		case MATERIAL:
			return MaterialManager.getMaterialConfig();
		case MAIN:
		default:
			return InteractionVisualizer.config;
		}
	}
	
	/**
	Please use getConfig(ConfiguationType configType) instead
	@return Magic Configuration.
	*/
	@Deprecated
	public static FileConfiguration getConfig() {
		return InteractionVisualizer.config;
	}
	
	/**
	Please use getConfig(ConfiguationType configType) instead
	@return Magic Configuration.
	*/
	@Deprecated
	public static FileConfiguration getEnchantmentConfig() {
		return EnchantmentManager.getEnchConfig();
	}
	
	/**
	Please use getConfig(ConfiguationType configType) instead
	@return Magic Configuration.
	*/
	@Deprecated
	public static FileConfiguration getMusicConfig() {
		return MusicManager.getMusicConfig();
	}
	
	/**
	Please use getConfig(ConfiguationType configType) instead
	@return Magic Configuration.
	*/
	@Deprecated
	public static FileConfiguration getEffectConfig() {
		return EffectManager.getEffectConfig();
	}
	
	/**
	Play a throw item animation from location1 to location2.
	If the boolean "pickupSound" is true, a pickup item sound will be played.
	*/
	public static void playFakeItemThrowAnimation(Location from, Location to, ItemStack itemstack, boolean pickupSound) {
		Item item = new Item(from.clone());
		item.setItemStack(itemstack);
		item.setLocked(true);
		item.setGravity(true);
		Vector lift = new Vector(0.0, 0.15, 0.0);
		Vector pickup = to.clone().toVector().subtract(from.clone().toVector()).multiply(0.15).add(lift);
		item.setVelocity(pickup);
		item.setPickupDelay(32767);
		PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		PacketManager.updateItem(item);
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			if (pickupSound) {
				SoundManager.playItemPickup(item.getLocation(), InteractionVisualizer.itemDrop);
			}
			PacketManager.removeItem(InteractionVisualizer.getOnlinePlayers(), item);
		}, 8);
	}
	
	/**
	Create a fake armorstand at the given location.
	DOES NOT SPAWN THE ARMORSTAND.
	@return The InteractionVisualizer ArmorStand object created.
	*/
	public static ArmorStand createArmorStandObject(Location location) {
		return new ArmorStand(location.clone());
	}
	
	/**
	Create a fake armorstand for holding mini items at the given location.
	DOES NOT SPAWN THE ARMORSTAND.
	@return The InteractionVisualizer ArmorStand object created.
	*/
	public static ArmorStand createArmorStandItemHoldingObject(Location location) {
		Vector vector = rotateVectorAroundY(location.clone().getDirection().normalize().multiply(0.19), -100).add(location.clone().getDirection().normalize().multiply(-0.11));
		ArmorStand stand = new ArmorStand(location.add(vector));
		setStand(stand, location.getYaw());
		return stand;
	}
	
	public static enum ArmorStandHoldingMode {
		ITEM("Item"),
		LOWBLOCK("LowBlock"),
		TOOL("Tool"),
		STANDING("Standing");
		
		private String mode;
		
		ArmorStandHoldingMode(String mode) {
			this.mode = mode;
		}
		
		public String toString() {
			return mode;
		}
		
		public static ArmorStandHoldingMode fromName(String name) {
			for (ArmorStandHoldingMode mode : values()) {
				if (mode.toString().equalsIgnoreCase(name)) {
					return mode;
				}
			}
			return null;
		}
	}
	
	/**
	Get the rotation mode for a mini item holding ArmorStand.
	ONLY WORKS WITH ARMORSTANDS CREATED USING createArmorStandItemHoldingObject(Location location)
	@return The same InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStandHoldingMode getArmorStandItemHoldingObjectMode(ArmorStand stand, ArmorStandHoldingMode mode) {
		switch (getStandModeRaw(stand).toLowerCase()) {
		case "Item":
			return ArmorStandHoldingMode.ITEM;
		case "LowBlock":
			return ArmorStandHoldingMode.LOWBLOCK;
		case "Tool":
			return ArmorStandHoldingMode.TOOL;
		case "Standing":
			return ArmorStandHoldingMode.STANDING;
		}
		return null;
	}
	
	/**
	Sets the rotation mode for a mini item holding ArmorStand.
	ONLY WORKS WITH ARMORSTANDS CREATED USING createArmorStandItemHoldingObject(Location location)
	@return The same InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand rotateArmorStandItemHoldingObject(ArmorStand stand, ArmorStandHoldingMode mode) {
		toggleStandMode(stand, mode.toString());
		return stand;
	}
	
	private static Vector rotateVectorAroundY(Vector vector, double degrees) {
	    double rad = Math.toRadians(degrees);
	   
	    double currentX = vector.getX();
	    double currentZ = vector.getZ();
	   
	    double cosine = Math.cos(rad);
	    double sine = Math.sin(rad);
	   
	    return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
	}
	
	private static void setStand(ArmorStand stand, float yaw) {
		stand.setArms(true);
		stand.setBasePlate(false);
		stand.setMarker(true);
		stand.setGravity(false);
		stand.setSmall(true);
		stand.setInvulnerable(true);
		stand.setVisible(false);
		stand.setSilent(true);
		stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
		stand.setCustomName("IV.Custom.Item");
		stand.setRotation(yaw, stand.getLocation().getPitch());
	}
	
	@Deprecated
	public static String getStandModeRaw(ArmorStand stand) {
		if (stand.getCustomName().startsWith("IV.Custom.")) {
			return stand.getCustomName().substring(stand.getCustomName().lastIndexOf(".") + 1);
		}
		return null;
	}
	
	public static ArmorStandHoldingMode getStandMode(ArmorStand stand) {
		if (stand.getCustomName().startsWith("IV.Custom.")) {
			return ArmorStandHoldingMode.fromName(stand.getCustomName().substring(stand.getCustomName().lastIndexOf(".") + 1));
		}
		return null;
	}
	
	private static void toggleStandMode(ArmorStand stand, String mode) {
		if (!stand.getCustomName().equals("IV.Custom.Item")) {
			if (stand.getCustomName().equals("IV.Custom.Block")) {
				stand.setCustomName("IV.Custom.Item");
				stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
				stand.teleport(stand.getLocation().add(0.0, -0.084, 0.0));
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.102), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.14)));
				
			}
			if (stand.getCustomName().equals("IV.Custom.LowBlock")) {
				stand.setCustomName("IV.Custom.Item");
				stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
				stand.teleport(stand.getLocation().add(0.0, -0.02, 0.0));
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.09), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.15)));
				
			}
			if (stand.getCustomName().equals("IV.Custom.Tool")) {
				stand.setCustomName("IV.Custom.Item");
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.1)));
				stand.teleport(stand.getLocation().add(0, 0.26, 0));
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
			}
			if (stand.getCustomName().equals("IV.Custom.Standing")) {
				stand.setCustomName("IV.Custom.Item");
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(0.323), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(-0.115)));
				stand.teleport(stand.getLocation().add(0, 0.32, 0));
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
			}
		}
		if (mode.equals("Block")) {
			stand.setCustomName("IV.Custom.Block");
			stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.14)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.102), -90)));
			stand.teleport(stand.getLocation().add(0.0, 0.084, 0.0));
			stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
			stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
		}
		if (mode.equals("LowBlock")) {
			stand.setCustomName("IV.Custom.LowBlock");
			stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.15)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.09), -90)));
			stand.teleport(stand.getLocation().add(0.0, 0.02, 0.0));
			stand.setRightArmPose(new EulerAngle(357.9, 0.0, 0.0));
			stand.setRotation(stand.getLocation().getYaw() + 45, stand.getLocation().getPitch());
		}
		if (mode.equals("Tool")) {
			stand.setCustomName("IV.Custom.Tool");
			stand.setRightArmPose(new EulerAngle(357.99, 0.0, 300.0));
			stand.teleport(stand.getLocation().add(0, -0.26, 0));
			stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.1)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.3), -90)));
		}
		if (mode.equals("Standing")) {
			stand.setCustomName("IV.Custom.Standing");
			stand.setRightArmPose(new EulerAngle(0.0, 4.7, 4.7));
			stand.teleport(stand.getLocation().add(0, -0.32, 0));
			stand.teleport(stand.getLocation().add(stand.getLocation().getDirection().normalize().multiply(0.115)));
			stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().getDirection().normalize().multiply(-0.323), -90)));
		}
	}
	
	/**
	Spawns the given InteractionVisualizer ArmorStand object to all players.
	@return The InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand spawnFakeArmorStand(ArmorStand stand) {
		PacketManager.sendArmorStandSpawn(InteractionVisualizer.holograms, stand);
		return stand;
	}
	
	/**
	Updates the given InteractionVisualizer ArmorStand object to all players.
	@return The InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand updateFakeArmorStand(ArmorStand stand) {
		PacketManager.updateArmorStand(InteractionVisualizer.holograms, stand);
		return stand;
	}
	
	/**
	Remove the given InteractionVisualizer ArmorStand object from all players.
	@return The InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand removeFakeArmorStand(ArmorStand stand) {
		PacketManager.removeArmorStand(InteractionVisualizer.holograms, stand);
		return stand;
	}
	
	/**
	Create a fake item at the given location.
	DOES NOT SPAWN THE ITEM.
	@return The InteractionVisualizer Item object created.
	*/
	public static Item createItemObject(Location location) {
		return new Item(location.clone());
	}
	
	/**
	Spawns the given InteractionVisualizer Item object to all players.
	@return The InteractionVisualizer Item object.
	*/
	public static Item spawnFakeItem(Item item) {
		PacketManager.sendItemSpawn(InteractionVisualizer.itemDrop, item);
		return item;
	}
	
	/**
	Updates the given InteractionVisualizer Item object to all players.
	@return The InteractionVisualizer Item object.
	*/
	public static Item updateItem(Item item) {
		PacketManager.updateItem(InteractionVisualizer.itemDrop, item);
		return item;
	}
	
	/**
	Remove the given InteractionVisualizer Item object from all players.
	@return The InteractionVisualizer Item object.
	*/
	public static Item removeItem(Item item) {
		PacketManager.removeItem(InteractionVisualizer.itemDrop, item);
		return item;
	}
}
