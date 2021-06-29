package com.loohp.interactionvisualizer.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.simpleyaml.configuration.file.FileConfiguration;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.VisualizerInteractDisplay;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.blocks.AnvilDisplay;
import com.loohp.interactionvisualizer.blocks.BannerDisplay;
import com.loohp.interactionvisualizer.blocks.BeaconDisplay;
import com.loohp.interactionvisualizer.blocks.BeeHiveDisplay;
import com.loohp.interactionvisualizer.blocks.BeeNestDisplay;
import com.loohp.interactionvisualizer.blocks.BlastFurnaceDisplay;
import com.loohp.interactionvisualizer.blocks.BrewingStandDisplay;
import com.loohp.interactionvisualizer.blocks.CampfireDisplay;
import com.loohp.interactionvisualizer.blocks.CartographyTableDisplay;
import com.loohp.interactionvisualizer.blocks.ChestDisplay;
import com.loohp.interactionvisualizer.blocks.ConduitDisplay;
import com.loohp.interactionvisualizer.blocks.CraftingTableDisplay;
import com.loohp.interactionvisualizer.blocks.DispenserDisplay;
import com.loohp.interactionvisualizer.blocks.DoubleChestDisplay;
import com.loohp.interactionvisualizer.blocks.DropperDisplay;
import com.loohp.interactionvisualizer.blocks.EnchantmentTableDisplay;
import com.loohp.interactionvisualizer.blocks.EnderchestDisplay;
import com.loohp.interactionvisualizer.blocks.FurnaceDisplay;
import com.loohp.interactionvisualizer.blocks.GrindstoneDisplay;
import com.loohp.interactionvisualizer.blocks.HopperDisplay;
import com.loohp.interactionvisualizer.blocks.JukeBoxDisplay;
import com.loohp.interactionvisualizer.blocks.LecternDisplay;
import com.loohp.interactionvisualizer.blocks.LoomDisplay;
import com.loohp.interactionvisualizer.blocks.NoteBlockDisplay;
import com.loohp.interactionvisualizer.blocks.ShulkerBoxDisplay;
import com.loohp.interactionvisualizer.blocks.SmithingTableDisplay;
import com.loohp.interactionvisualizer.blocks.SmokerDisplay;
import com.loohp.interactionvisualizer.blocks.SoulCampfireDisplay;
import com.loohp.interactionvisualizer.blocks.SpawnerDisplay;
import com.loohp.interactionvisualizer.blocks.StonecutterDisplay;
import com.loohp.interactionvisualizer.debug.Debug;
import com.loohp.interactionvisualizer.entities.ItemDisplay;
import com.loohp.interactionvisualizer.entities.VillagerDisplay;
import com.loohp.interactionvisualizer.listeners.ChunkEvents;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.updater.Updater;
import com.loohp.interactionvisualizer.utils.MCVersion;

public class TaskManager {
	
	public static Plugin plugin = InteractionVisualizer.plugin;
	public static MCVersion version;
	
	public static boolean anvil;
	public static boolean banner;
	public static boolean beacon;
	public static boolean beehive;
	public static boolean beenest;
	public static boolean blastfurnace;
	public static boolean brewingstand;
	public static boolean campfire;
	public static boolean cartographytable;
	public static boolean chest;
	public static boolean conduit;
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
	public static boolean lectern;
	public static boolean loom;
	public static boolean noteblock;
	public static boolean shulkerbox;
	public static boolean smoker;
	public static boolean soulcampfire;
	public static boolean spawner;
	public static boolean stonecutter;
	public static boolean smithingtable;
	
	public static boolean item;
	public static boolean villager;
	
	public static List<Integer> tasks = new ArrayList<Integer>();
	
	public static HashMap<InventoryType, List<VisualizerInteractDisplay>> processes = new HashMap<InventoryType, List<VisualizerInteractDisplay>>();
	public static List<VisualizerRunnableDisplay> runnables = new ArrayList<VisualizerRunnableDisplay>();
	
	@SuppressWarnings("deprecation")
	public static void setup() {
		anvil = false;
		banner = false;
		beacon = false;
		beehive = false;
		beenest = false;
		blastfurnace = false;
		brewingstand = false;
		campfire = false;
		cartographytable = false;
		chest = false;
		conduit = false;
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
		lectern = false;
		loom = false;
		noteblock = false;
		shulkerbox = false;
		smoker = false;
		soulcampfire = false;
		spawner = false;
		stonecutter = false;
		smithingtable = false;
		
		item = false;
		villager = false;
		
		version = InteractionVisualizer.version;
		
		/*
		HandlerList.unregisterAll(plugin);
		for (int taskid : tasks) {
			Bukkit.getScheduler().cancelTask(taskid);
		}
		tasks.clear();
		*/
		
		List<EntryKey> keys = new ArrayList<>();
		
		Bukkit.getPluginManager().registerEvents(new Debug(), plugin);
		Bukkit.getPluginManager().registerEvents(new Updater(), plugin);
		Bukkit.getPluginManager().registerEvents(new com.loohp.interactionvisualizer.listeners.Events(), plugin);
		Bukkit.getPluginManager().registerEvents(new PacketManager(), plugin);
		if (version.isLegacy()) {
			ChunkEvents.setup();
			InteractionVisualizer.defaultworld.getChunkAt(0, 0).load();
			Bukkit.getPluginManager().registerEvents(new ChunkEvents(), plugin);
		}
		
		for (InventoryType type : InventoryType.values()) {
			processes.put(type, new ArrayList<VisualizerInteractDisplay>());
		}
		
		if (getConfig().getBoolean("Blocks.CraftingTable.Enabled")) {
			CraftingTableDisplay ctd = new CraftingTableDisplay();
			keys.add(ctd.registerNative(InventoryType.WORKBENCH));
			Bukkit.getPluginManager().registerEvents(ctd, plugin);
			craftingtable = true;
		}
		
		if (getConfig().getBoolean("Blocks.Loom.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			LoomDisplay ld = new LoomDisplay();
			keys.add(ld.registerNative(InventoryType.LOOM));
			Bukkit.getPluginManager().registerEvents(ld, plugin);
			loom = true;
		}
		
		if (getConfig().getBoolean("Blocks.EnchantmentTable.Enabled")) {
			EnchantmentTableDisplay etd = new EnchantmentTableDisplay();
			keys.add(etd.registerNative(InventoryType.ENCHANTING));
			Bukkit.getPluginManager().registerEvents(etd, plugin);
			enchantmenttable = true;
		}
		
		if (getConfig().getBoolean("Blocks.CartographyTable.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			CartographyTableDisplay ctd = new CartographyTableDisplay();
			keys.add(ctd.registerNative(InventoryType.CARTOGRAPHY));
			Bukkit.getPluginManager().registerEvents(ctd, plugin);
			cartographytable = true;
		}
		
		if (getConfig().getBoolean("Blocks.Anvil.Enabled")) {
			AnvilDisplay ad = new AnvilDisplay();
			keys.add(ad.registerNative(InventoryType.ANVIL));
			Bukkit.getPluginManager().registerEvents(ad, plugin);
			anvil = true;
		}
		
		if (getConfig().getBoolean("Blocks.Grindstone.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			GrindstoneDisplay gd = new GrindstoneDisplay();
			keys.add(gd.registerNative(InventoryType.GRINDSTONE));
			Bukkit.getPluginManager().registerEvents(gd, plugin);
			grindstone = true;
		}
		
		if (getConfig().getBoolean("Blocks.Stonecutter.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			StonecutterDisplay sd = new StonecutterDisplay();
			keys.add(sd.registerNative(InventoryType.STONECUTTER));
			Bukkit.getPluginManager().registerEvents(sd, plugin);
			stonecutter = true;
		}
		
		if (getConfig().getBoolean("Blocks.BrewingStand.Enabled")) {
			BrewingStandDisplay bsd = new BrewingStandDisplay();
			keys.add(bsd.registerNative());
			Bukkit.getPluginManager().registerEvents(bsd, plugin);
			brewingstand = true;
		}
		
		if (getConfig().getBoolean("Blocks.Chest.Enabled")) {
			ChestDisplay cd = new ChestDisplay();
			keys.add(cd.registerNative());
			Bukkit.getPluginManager().registerEvents(cd, plugin);
			chest = true;
		}
		
		if (getConfig().getBoolean("Blocks.DoubleChest.Enabled")) {
			DoubleChestDisplay dcd = new DoubleChestDisplay();
			keys.add(dcd.registerNative());
			Bukkit.getPluginManager().registerEvents(dcd, plugin);
			doublechest = true;
		}
		
		if (getConfig().getBoolean("Blocks.Furnace.Enabled")) {
			FurnaceDisplay fd = new FurnaceDisplay();
			keys.add(fd.registerNative());
			Bukkit.getPluginManager().registerEvents(fd, plugin);
			furnace = true;
		}
		
		if (getConfig().getBoolean("Blocks.BlastFurnace.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			BlastFurnaceDisplay bfd = new BlastFurnaceDisplay();
			keys.add(bfd.registerNative());
			Bukkit.getPluginManager().registerEvents(bfd, plugin);
			blastfurnace = true;
		}
		
		if (getConfig().getBoolean("Blocks.Smoker.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			SmokerDisplay sd = new SmokerDisplay();
			keys.add(sd.registerNative());
			Bukkit.getPluginManager().registerEvents(sd, plugin);
			smoker = true;
		}
		
		if (getConfig().getBoolean("Blocks.EnderChest.Enabled")) {
			EnderchestDisplay ed = new EnderchestDisplay();
			keys.add(ed.registerNative());
			Bukkit.getPluginManager().registerEvents(ed, plugin);
			enderchest = true;
		}
		
		if (getConfig().getBoolean("Blocks.ShulkerBox.Enabled") && (!version.isOld())) {
			ShulkerBoxDisplay sbd = new ShulkerBoxDisplay();
			keys.add(sbd.registerNative());
			Bukkit.getPluginManager().registerEvents(sbd, plugin);
			shulkerbox = true;
		}
		
		if (getConfig().getBoolean("Blocks.Dispenser.Enabled")) {
			DispenserDisplay dd = new DispenserDisplay();
			keys.add(dd.registerNative());
			Bukkit.getPluginManager().registerEvents(dd, plugin);
			dispenser = true;
		}
		
		if (getConfig().getBoolean("Blocks.Dropper.Enabled")) {
			DropperDisplay dd = new DropperDisplay();
			keys.add(dd.registerNative());
			Bukkit.getPluginManager().registerEvents(dd, plugin);
			dropper = true;
		}
		
		if (getConfig().getBoolean("Blocks.Hopper.Enabled")) {
			HopperDisplay hd = new HopperDisplay();
			keys.add(hd.registerNative());
			Bukkit.getPluginManager().registerEvents(hd, plugin);
			hopper = true;
		}
		
		if (getConfig().getBoolean("Blocks.Beacon.Enabled")) {
			BeaconDisplay bd = new BeaconDisplay();
			keys.add(bd.registerNative());
			Bukkit.getPluginManager().registerEvents(bd, plugin);
			beacon = true;
		}
		
		if (getConfig().getBoolean("Blocks.NoteBlock.Enabled")) {
			NoteBlockDisplay nbd = new NoteBlockDisplay();
			keys.add(nbd.registerNative());
			Bukkit.getPluginManager().registerEvents(nbd, plugin);
			noteblock = true;
		}
		
		if (getConfig().getBoolean("Blocks.JukeBox.Enabled")) {
			JukeBoxDisplay jbd = new JukeBoxDisplay();
			keys.add(jbd.registerNative());
			Bukkit.getPluginManager().registerEvents(jbd, plugin);
			jukebox = true;
		}
		
		if (getConfig().getBoolean("Blocks.SmithingTable.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_16)) {
			SmithingTableDisplay std = new SmithingTableDisplay();
			keys.add(std.registerNative(InventoryType.SMITHING));
			Bukkit.getPluginManager().registerEvents(std, plugin);
			smithingtable = true;
		}
		
		if (getConfig().getBoolean("Blocks.BeeNest.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_15)) {
			BeeNestDisplay bnd = new BeeNestDisplay();
			keys.add(bnd.registerNative());
			Bukkit.getPluginManager().registerEvents(bnd, plugin);
			beenest = true;
		}
		
		if (getConfig().getBoolean("Blocks.BeeHive.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_15)) {
			BeeHiveDisplay bhd = new BeeHiveDisplay();
			keys.add(bhd.registerNative());
			Bukkit.getPluginManager().registerEvents(bhd, plugin);
			beehive = true;
		}
		
		if (getConfig().getBoolean("Blocks.Lectern.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			LecternDisplay ld = new LecternDisplay();
			keys.add(ld.registerNative());
			Bukkit.getPluginManager().registerEvents(ld, plugin);
			lectern = true;
		}
		
		if (getConfig().getBoolean("Blocks.Campfire.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_14)) {
			CampfireDisplay cd = new CampfireDisplay();
			keys.add(cd.registerNative());
			Bukkit.getPluginManager().registerEvents(cd, plugin);
			campfire = true;
		}
		
		if (getConfig().getBoolean("Blocks.SoulCampfire.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_16)) {
			SoulCampfireDisplay scd = new SoulCampfireDisplay();
			keys.add(scd.registerNative());
			Bukkit.getPluginManager().registerEvents(scd, plugin);
			soulcampfire = true;
		}
		
		if (getConfig().getBoolean("Blocks.Spawner.Enabled")) {
			SpawnerDisplay sd = new SpawnerDisplay();
			keys.add(sd.registerNative());
			Bukkit.getPluginManager().registerEvents(sd, plugin);
			spawner = true;
		}
		
		if (getConfig().getBoolean("Blocks.Conduit.Enabled") && version.isNewerOrEqualTo(MCVersion.V1_13)) {
			ConduitDisplay cd = new ConduitDisplay();
			keys.add(cd.registerNative());
			Bukkit.getPluginManager().registerEvents(cd, plugin);
			conduit = true;
		}
		
		if (getConfig().getBoolean("Blocks.Banner.Enabled")) {
			BannerDisplay bd = new BannerDisplay();
			keys.add(bd.registerNative());
			Bukkit.getPluginManager().registerEvents(bd, plugin);
			banner = true;
		}
		
		if (getConfig().getBoolean("Entities.Item.Enabled")) {
			ItemDisplay id = new ItemDisplay();
			keys.add(id.registerNative());
			Bukkit.getPluginManager().registerEvents(id, plugin);
			item = true;
		}
		
		if (getConfig().getBoolean("Entities.Villager.Enabled")) {
			VillagerDisplay vd = new VillagerDisplay();
			keys.add(vd.registerNative());
			Bukkit.getPluginManager().registerEvents(vd, plugin);
			villager = true;
		}
		
		InteractionVisualizer.preferenceManager.registerEntry(keys.toArray(new EntryKey[keys.size()]));
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
	
	private static FileConfiguration getConfig() {
		return InteractionVisualizer.plugin.getConfiguration();
	}	

}
