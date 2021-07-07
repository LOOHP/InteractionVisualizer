package com.loohp.interactionvisualizer.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.config.Config;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PreferenceManager;
import com.loohp.interactionvisualizer.managers.SoundManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.SynchronizedFilteredCollection;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;

public class InteractionVisualizerAPI {
	
	/**
	Get the user friendly names of a module
	@return The user friendly name of the module, or the internal name, if not found.
	*/
	public static String getUserFriendlyName(Modules module) {
		return Config.getConfig(InteractionVisualizer.CONFIG_ID).getConfiguration().getString("Messages.ModuleName." + module.toString().toLowerCase(), module.toString().toLowerCase());
	}
	
	/**
	Get the user friendly names of an entry
	@return The user friendly name of the entry, or the internal name, if not found.
	*/
	public static String getUserFriendlyName(EntryKey entry) {
		return Config.getConfig(InteractionVisualizer.CONFIG_ID).getConfiguration().getString("Messages.EntryName." + entry.toString().toLowerCase(), entry.toString().toLowerCase());
	}
	
	/**
	Gets the GC period in ticks defined in the config.
	@return The GC period in ticks.
	*/
	public static int getGCPeriod() {
		return InteractionVisualizer.plugin.getConfiguration().getInt("GarbageCollector.Period");
	}
	
	/**
	Gets the list of tile entity blocks that is within range of a player.
	@return A list of blocks that is within range of at least one player.
	*/
	public static Collection<Block> getActiveTileEntityBlocks(TileEntityType type) {
		return TileEntityManager.getTileEntites(type);
	}
	
	public static enum Modules {
		ITEMSTAND,
		ITEMDROP,
		HOLOGRAM;
	}
	
	/**
	Gets all players that has a module enabled for themselves, excluding players in disabled worlds.
	@return A set of players.
	*/
	public static Collection<Player> getPlayerModuleList(Modules module, EntryKey entry) {
		return getPlayerModuleList(module, entry, true);
	}
	
	/**
	Gets all players that has a module enabled for themselves, excluding the provided players and players in disabled worlds.
	@return A set of players.
	*/
	public static Collection<Player> getPlayerModuleList(Modules module, EntryKey entry, Player... excludes) {
		return getPlayerModuleList(module, entry, true, excludes);
	}
	
	/**
	Gets all players that has a module enabled for themselves, excluding the provided players.
	@return A set of players.
	*/
	public static Collection<Player> getPlayerModuleList(Modules module, EntryKey entry, boolean excludeDisabledWorlds, Player... excludes) {
		Collection<Player> players = InteractionVisualizer.preferenceManager.getPlayerList(module, entry);
		Set<Player> excludedPlayers = Stream.of(excludes).collect(Collectors.toSet());
		if (excludeDisabledWorlds) {
			Set<String> disabledWorlds = getDisabledWorlds();
			players = SynchronizedFilteredCollection.filterSynchronized(players, each -> !excludedPlayers.contains(each) && !disabledWorlds.contains(each.getWorld().getName()));
		} else {
			players = SynchronizedFilteredCollection.filterSynchronized(players, each -> !excludedPlayers.contains(each));
		}
		return Collections.unmodifiableCollection(players);
	}
	
	/**
	Gets all players.
	<B>Including</B> players in disabled worlds.
	@return A set of players.
	*/
	public static Collection<Player> getPlayers() {
		return getPlayers(false);
	}
	
	/**
	Gets all players.
	@return A set of players.
	*/
	public static Collection<Player> getPlayers(boolean excludeDisabledWorlds) {
		if (excludeDisabledWorlds) {
			Set<String> disabledWorlds = getDisabledWorlds();
			return Bukkit.getWorlds().stream().filter(each -> disabledWorlds.contains(each.getName())).flatMap(each -> each.getPlayers().stream()).collect(Collectors.toSet());
		} else {
			return new HashSet<>(Bukkit.getOnlinePlayers());
		}
	}
	
	/**
	Check if an online player has a module enabled.
	@return true/false.
	*/
	public static boolean hasPlayerEnabledModule(Player player, Modules module, EntryKey entry) {
		switch (module) {
		case HOLOGRAM:
			return InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, entry).contains(player);
		case ITEMDROP:
			return InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry).contains(player);
		case ITEMSTAND:
			return InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMSTAND, entry).contains(player);
		}
		return false;
	}
	
	/**
	Check if player has a module enabled.
	@return true/false.
	*/
	public static boolean hasPlayerEnabledModule(UUID uuid, Modules module, EntryKey entry) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			return hasPlayerEnabledModule(player, module, entry);
		} else {
			InteractionVisualizer.preferenceManager.loadPlayer(uuid, "", false);
			boolean value = hasPlayerEnabledModule(player, module, entry);
			InteractionVisualizer.preferenceManager.savePlayer(uuid, true);
			return value;
		}
	}
	
	/**
	Check if an entry is registered.
	@return true/false.
	*/
	public static boolean isRegisteredEntry(EntryKey entry) {
		return InteractionVisualizer.preferenceManager.getRegisteredEntries().contains(entry);
	}
	
	/**
	Get all registered entries.
	@return true/false.
	*/
	public static List<EntryKey> getRegisteredEntries() {
		return Collections.unmodifiableList(InteractionVisualizer.preferenceManager.getRegisteredEntries());
	}
	
	/**
	Get the list of disabled world names
	@return a set of world names
	*/
	public static Set<String> getDisabledWorlds() {
		return Collections.unmodifiableSet(InteractionVisualizer.disabledWorlds);
	}
	
	/**
	 * Get the PreferenceManager
	 * @return PreferenceManager
	 */
	public static PreferenceManager getPreferenceManager() {
		return InteractionVisualizer.preferenceManager;
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
	Play a throw item animation from location1 to location2.
	If the boolean "pickupSound" is true, a pickup item sound will be played.
	*/
	public static void playFakeItemThrowAnimation(EntryKey entry, Location from, Location to, ItemStack itemstack, boolean pickupSound) {
		Item item = new Item(from.clone());
		item.setItemStack(itemstack);
		item.setLocked(true);
		item.setGravity(true);
		Vector lift = new Vector(0.0, 0.15, 0.0);
		Vector pickup = to.clone().toVector().subtract(from.clone().toVector()).multiply(0.15).add(lift);
		item.setVelocity(pickup);
		item.setPickupDelay(32767);
		PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry), item);
		PacketManager.updateItem(item);
		
		Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
			if (pickupSound) {
				SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry));
			}
			PacketManager.removeItem(getPlayers(), item);
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
		if (stand.getCustomName().toPlainText().startsWith("IV.Custom.")) {
			return stand.getCustomName().toPlainText().substring(stand.getCustomName().toPlainText().lastIndexOf(".") + 1);
		}
		return null;
	}
	
	public static ArmorStandHoldingMode getStandMode(ArmorStand stand) {
		if (stand.getCustomName().toPlainText().startsWith("IV.Custom.")) {
			return ArmorStandHoldingMode.fromName(stand.getCustomName().toPlainText().substring(stand.getCustomName().toPlainText().lastIndexOf(".") + 1));
		}
		return null;
	}
	
	private static void toggleStandMode(ArmorStand stand, String mode) {
		if (!stand.getCustomName().toPlainText().equals("IV.Custom.Item")) {
			if (stand.getCustomName().toPlainText().equals("IV.Custom.Block")) {
				stand.setCustomName("IV.Custom.Item");
				stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
				stand.teleport(stand.getLocation().add(0.0, -0.084, 0.0));
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.102), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.14)));
				
			}
			if (stand.getCustomName().toPlainText().equals("IV.Custom.LowBlock")) {
				stand.setCustomName("IV.Custom.Item");
				stand.setRotation(stand.getLocation().getYaw() - 45, stand.getLocation().getPitch());
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
				stand.teleport(stand.getLocation().add(0.0, -0.02, 0.0));
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(-0.09), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(-0.15)));
				
			}
			if (stand.getCustomName().toPlainText().equals("IV.Custom.Tool")) {
				stand.setCustomName("IV.Custom.Item");
				stand.teleport(stand.getLocation().add(rotateVectorAroundY(stand.getLocation().clone().getDirection().normalize().multiply(0.3), -90)));
				stand.teleport(stand.getLocation().add(stand.getLocation().clone().getDirection().normalize().multiply(0.1)));
				stand.teleport(stand.getLocation().add(0, 0.26, 0));
				stand.setRightArmPose(new EulerAngle(0.0, 0.0, 0.0));
			}
			if (stand.getCustomName().toPlainText().equals("IV.Custom.Standing")) {
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
	public static ArmorStand spawnFakeArmorStand(ArmorStand stand, EntryKey entry) {
		PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, entry), stand);
		return stand;
	}
	
	/**
	Updates the given InteractionVisualizer ArmorStand object to all players.
	@return The InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand updateFakeArmorStand(ArmorStand stand, EntryKey entry) {
		PacketManager.updateArmorStand(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, entry), stand);
		return stand;
	}
	
	/**
	Remove the given InteractionVisualizer ArmorStand object from all players.
	@return The InteractionVisualizer ArmorStand object.
	*/
	public static ArmorStand removeFakeArmorStand(ArmorStand stand, EntryKey entry) {
		PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, entry), stand);
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
	public static Item spawnFakeItem(Item item, EntryKey entry) {
		PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry), item);
		return item;
	}
	
	/**
	Updates the given InteractionVisualizer Item object to all players.
	@return The InteractionVisualizer Item object.
	*/
	public static Item updateItem(Item item, EntryKey entry) {
		PacketManager.updateItem(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry), item);
		return item;
	}
	
	/**
	Remove the given InteractionVisualizer Item object from all players.
	@return The InteractionVisualizer Item object.
	*/
	public static Item removeItem(Item item, EntryKey entry) {
		PacketManager.removeItem(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, entry), item);
		return item;
	}
}
