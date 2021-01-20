package com.loohp.interactionvisualizer.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.VisualizerInteractDisplay;
import com.loohp.interactionvisualizer.API.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.Blocks.AnvilDisplay;
import com.loohp.interactionvisualizer.Blocks.BeaconDisplay;
import com.loohp.interactionvisualizer.Blocks.BeeHiveDisplay;
import com.loohp.interactionvisualizer.Blocks.BeeNestDisplay;
import com.loohp.interactionvisualizer.Blocks.BlastFurnaceDisplay;
import com.loohp.interactionvisualizer.Blocks.BrewingStandDisplay;
import com.loohp.interactionvisualizer.Blocks.CartographyTableDisplay;
import com.loohp.interactionvisualizer.Blocks.ChestDisplay;
import com.loohp.interactionvisualizer.Blocks.CraftingTableDisplay;
import com.loohp.interactionvisualizer.Blocks.DispenserDisplay;
import com.loohp.interactionvisualizer.Blocks.DoubleChestDisplay;
import com.loohp.interactionvisualizer.Blocks.DropperDisplay;
import com.loohp.interactionvisualizer.Blocks.EnchantmentTableDisplay;
import com.loohp.interactionvisualizer.Blocks.EnderchestDisplay;
import com.loohp.interactionvisualizer.Blocks.FurnaceDisplay;
import com.loohp.interactionvisualizer.Blocks.GrindstoneDisplay;
import com.loohp.interactionvisualizer.Blocks.HopperDisplay;
import com.loohp.interactionvisualizer.Blocks.JukeBoxDisplay;
import com.loohp.interactionvisualizer.Blocks.LoomDisplay;
import com.loohp.interactionvisualizer.Blocks.NoteBlockDisplay;
import com.loohp.interactionvisualizer.Blocks.ShulkerBoxDisplay;
import com.loohp.interactionvisualizer.Blocks.SmithingTableDisplay;
import com.loohp.interactionvisualizer.Blocks.SmokerDisplay;
import com.loohp.interactionvisualizer.Blocks.StonecutterDisplay;
import com.loohp.interactionvisualizer.Debug.Debug;
import com.loohp.interactionvisualizer.Entities.VillagerDisplay;
import com.loohp.interactionvisualizer.Listeners.ChunkEvents;
import com.loohp.interactionvisualizer.Updater.Updater;
import com.loohp.interactionvisualizer.Utils.MCVersion;

public class TaskManager {
	
	public static Plugin plugin = InteractionVisualizer.plugin;
	public static MCVersion version;
	public static FileConfiguration config = InteractionVisualizer.config;
	
	public static boolean anvil;
	public static boolean beacon;
	public static boolean beehive;
	public static boolean beenest;
	public static boolean blastfurnace;
	public static boolean brewingstand;
	public static boolean cartographytable;
	public static boolean chest;
	public static boolean craftingtable;
	public static boolean dispenser;
	public static boolean doublechest;
	public static boolean dropper;
	public static boolean enchantmenttable;
	public static boolean enderchest;
	public static boolean furnace;
	public static boolean grindstone;
	public static boolean hopper;
	public static boolean jukebox;
	public static boolean loom;
	public static boolean noteblock;
	public static boolean smoker;
	public static boolean stonecutter;
	public static boolean shulkerbox;
	public static boolean smithingtable;
	
	public static boolean villager;
	
	public static List<Integer> tasks = new ArrayList<Integer>();
	
	public static HashMap<InventoryType, List<VisualizerInteractDisplay>> processes = new HashMap<InventoryType, List<VisualizerInteractDisplay>>();
	public static List<VisualizerRunnableDisplay> runnables = new ArrayList<VisualizerRunnableDisplay>();
	
	public static void setup() {
		anvil = false;
		beacon = false;
		beehive = false;
		beenest = false;
		blastfurnace = false;
		brewingstand = false;
		cartographytable = false;
		chest = false;
		craftingtable = false;
		dispenser = false;
		doublechest = false;
		dropper = false;
		enchantmenttable = false;
		enderchest = false;
		furnace = false;
		grindstone = false;
		hopper = false;
		jukebox = false;
		loom = false;
		noteblock = false;
		smoker = false;
		stonecutter = false;
		shulkerbox = false;
		smithingtable = false;
		
		villager = false;
		
		version = InteractionVisualizer.version;
		
		/*
		HandlerList.unregisterAll(plugin);
		for (int taskid : tasks) {
			Bukkit.getScheduler().cancelTask(taskid);
		}
		tasks.clear();
		*/
		
		Bukkit.getPluginManager().registerEvents(new Debug(), plugin);
		Bukkit.getPluginManager().registerEvents(new Updater(), plugin);
		Bukkit.getPluginManager().registerEvents(new com.loohp.interactionvisualizer.Listeners.Events(), plugin);
		Bukkit.getPluginManager().registerEvents(new PacketManager(), plugin);
		if (version.isLegacy()) {
			ChunkEvents.setup();
			InteractionVisualizer.defaultworld.getChunkAt(0, 0).load();
			Bukkit.getPluginManager().registerEvents(new ChunkEvents(), plugin);
		}
		
		for (InventoryType type : InventoryType.values()) {
			processes.put(type, new ArrayList<VisualizerInteractDisplay>());
		}
		
		if (config.getBoolean("Blocks.CraftingTable.Enabled")) {
			CraftingTableDisplay ctd = new CraftingTableDisplay();
			ctd.register(InventoryType.WORKBENCH);
			Bukkit.getPluginManager().registerEvents(ctd, plugin);
			craftingtable = true;
		}
		
		if (config.getBoolean("Blocks.Loom.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			LoomDisplay ld = new LoomDisplay();
			ld.register(InventoryType.LOOM);
			Bukkit.getPluginManager().registerEvents(ld, plugin);
			loom = true;
		}
		
		if (config.getBoolean("Blocks.EnchantmentTable.Enabled")) {
			EnchantmentTableDisplay etd = new EnchantmentTableDisplay();
			etd.register(InventoryType.ENCHANTING);
			Bukkit.getPluginManager().registerEvents(etd, plugin);
			enchantmenttable = true;
		}
		
		if (config.getBoolean("Blocks.CartographyTable.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			CartographyTableDisplay ctd = new CartographyTableDisplay();
			ctd.register(InventoryType.CARTOGRAPHY);
			Bukkit.getPluginManager().registerEvents(ctd, plugin);
			cartographytable = true;
		}
		
		if (config.getBoolean("Blocks.Anvil.Enabled")) {
			AnvilDisplay ad = new AnvilDisplay();
			ad.register(InventoryType.ANVIL);
			Bukkit.getPluginManager().registerEvents(ad, plugin);
			anvil = true;
		}
		
		if (config.getBoolean("Blocks.Grindstone.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			GrindstoneDisplay gd = new GrindstoneDisplay();
			gd.register(InventoryType.GRINDSTONE);
			Bukkit.getPluginManager().registerEvents(gd, plugin);
			grindstone = true;
		}
		
		if (config.getBoolean("Blocks.Stonecutter.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			StonecutterDisplay sd = new StonecutterDisplay();
			sd.register(InventoryType.STONECUTTER);
			Bukkit.getPluginManager().registerEvents(sd, plugin);
			stonecutter = true;
		}
		
		if (config.getBoolean("Blocks.BrewingStand.Enabled")) {
			BrewingStandDisplay bsd = new BrewingStandDisplay();
			bsd.register();
			Bukkit.getPluginManager().registerEvents(bsd, plugin);
			brewingstand = true;
		}
		
		if (config.getBoolean("Blocks.Chest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new ChestDisplay(), plugin);
			chest = true;
		}
		
		if (config.getBoolean("Blocks.DoubleChest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new DoubleChestDisplay(), plugin);
			doublechest = true;
		}
		
		if (config.getBoolean("Blocks.Furnace.Enabled")) {
			FurnaceDisplay fd = new FurnaceDisplay();
			fd.register();
			Bukkit.getPluginManager().registerEvents(fd, plugin);
			furnace = true;
		}
		
		if (config.getBoolean("Blocks.BlastFurnace.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			BlastFurnaceDisplay bfd = new BlastFurnaceDisplay();
			bfd.register();
			Bukkit.getPluginManager().registerEvents(bfd, plugin);
			blastfurnace = true;
		}
		
		if (config.getBoolean("Blocks.Smoker.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			SmokerDisplay sd = new SmokerDisplay();
			sd.register();
			Bukkit.getPluginManager().registerEvents(sd, plugin);
			smoker = true;
		}
		
		if (config.getBoolean("Blocks.EnderChest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new EnderchestDisplay(), plugin);
			enderchest = true;
		}
		
		if (config.getBoolean("Blocks.ShulkerBox.Enabled") &&
				   (!version.isOld())
				) {
			Bukkit.getPluginManager().registerEvents(new ShulkerBoxDisplay(), plugin);
			shulkerbox = true;
		}
		
		if (config.getBoolean("Blocks.Dispenser.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new DispenserDisplay(), plugin);
			dispenser = true;
		}
		
		if (config.getBoolean("Blocks.Dropper.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new DropperDisplay(), plugin);
			dropper = true;
		}
		
		if (config.getBoolean("Blocks.Hopper.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new HopperDisplay(), plugin);
			hopper = true;
		}
		
		if (config.getBoolean("Blocks.Beacon.Enabled")) {
			BeaconDisplay bd = new BeaconDisplay();
			bd.register();
			Bukkit.getPluginManager().registerEvents(bd, plugin);
			beacon = true;
		}
		
		if (config.getBoolean("Blocks.NoteBlock.Enabled")) {
			NoteBlockDisplay nbd = new NoteBlockDisplay();
			nbd.register();
			Bukkit.getPluginManager().registerEvents(nbd, plugin);
			noteblock = true;
		}
		
		if (config.getBoolean("Blocks.JukeBox.Enabled")) {
			JukeBoxDisplay jbd = new JukeBoxDisplay();
			jbd.register();
			Bukkit.getPluginManager().registerEvents(jbd, plugin);
			jukebox = true;
		}
		
		if (config.getBoolean("Blocks.SmithingTable.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_16)) {
			SmithingTableDisplay std = new SmithingTableDisplay();
			std.register(InventoryType.SMITHING);
			Bukkit.getPluginManager().registerEvents(std, plugin);
			smithingtable = true;
		}
		
		if (config.getBoolean("Blocks.BeeNest.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_15)) {
			BeeNestDisplay bnd = new BeeNestDisplay();
			bnd.register();
			Bukkit.getPluginManager().registerEvents(bnd, plugin);
			beenest = true;
		}
		
		if (config.getBoolean("Blocks.BeeHive.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_15)) {
			BeeHiveDisplay bhd = new BeeHiveDisplay();
			bhd.register();
			Bukkit.getPluginManager().registerEvents(bhd, plugin);
			beehive = true;
		}
		
		if (config.getBoolean("Entities.Villager.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new VillagerDisplay(), plugin);
			villager = true;
		}
		
		tasks.add(LightManager.run());
		PacketManager.update();
	}
	
	public static void run() {
		int next = 1;
		int count = 0;
		int size = Bukkit.getOnlinePlayers().size();
		int maxper = (int) Math.ceil((double) size / (double) 5);
		if (maxper > 10) {
			maxper = 10;
		}
		int delay = 1;
		for (Player eachPlayer : Bukkit.getOnlinePlayers()) {
			if (eachPlayer.getOpenInventory().getType().equals(InventoryType.CRAFTING) || eachPlayer.getOpenInventory().getType().equals(InventoryType.CREATIVE)) {
				continue;
			}
			count++;
			if (count > maxper) {
				count = 0;
				delay++;
			}
			UUID uuid = eachPlayer.getUniqueId();
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					return;
				}
				
				Inventory inv = player.getOpenInventory().getTopInventory();
				
				processes.get(inv.getType()).forEach((each) -> each.process(player));
			}, delay);
		}
		next = next + delay;
		Bukkit.getScheduler().runTaskLater(plugin, () -> run(), next);
	}

}
