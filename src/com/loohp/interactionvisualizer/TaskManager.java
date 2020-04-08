package com.loohp.interactionvisualizer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.Blocks.AnvilDisplay;
import com.loohp.interactionvisualizer.Blocks.BlastFurnaceDisplay;
import com.loohp.interactionvisualizer.Blocks.BrewingStandDisplay;
import com.loohp.interactionvisualizer.Blocks.CartographyTableDisplay;
import com.loohp.interactionvisualizer.Blocks.ChestDisplay;
import com.loohp.interactionvisualizer.Blocks.CraftingTableDisplay;
import com.loohp.interactionvisualizer.Blocks.DoubleChestDisplay;
import com.loohp.interactionvisualizer.Blocks.EnchantmentTableDisplay;
import com.loohp.interactionvisualizer.Blocks.EnderchestDisplay;
import com.loohp.interactionvisualizer.Blocks.FurnaceDisplay;
import com.loohp.interactionvisualizer.Blocks.GrindstoneDisplay;
import com.loohp.interactionvisualizer.Blocks.LoomDisplay;
import com.loohp.interactionvisualizer.Blocks.SmokerDisplay;
import com.loohp.interactionvisualizer.Blocks.StonecutterDisplay;
import com.loohp.interactionvisualizer.Debug.Debug;
import com.loohp.interactionvisualizer.Utils.PacketSending;

public class TaskManager {
	
	public static Plugin plugin = InteractionVisualizer.plugin;
	public static String version;
	public static FileConfiguration config = InteractionVisualizer.config;
	
	public static AnvilDisplay anvil;
	public static BlastFurnaceDisplay blastfurnace;
	public static BrewingStandDisplay brewingstand;
	public static CartographyTableDisplay cartographytable;
	public static ChestDisplay chest;
	public static CraftingTableDisplay craftingtable;
	public static DoubleChestDisplay doublechest;
	public static EnchantmentTableDisplay enchantmenttable;
	public static EnderchestDisplay enderchest;
	public static FurnaceDisplay furnace;
	public static GrindstoneDisplay grindstone;
	public static LoomDisplay loom;
	public static SmokerDisplay smoker;
	public static StonecutterDisplay stonecutter;
	
	public static List<Integer> tasks = new ArrayList<Integer>();
	
	public static void setup() {
		anvil = new AnvilDisplay();
		blastfurnace = new BlastFurnaceDisplay();
		brewingstand = new BrewingStandDisplay();
		cartographytable = new CartographyTableDisplay();
		chest = new ChestDisplay();
		craftingtable = new CraftingTableDisplay();
		doublechest = new DoubleChestDisplay();
		enchantmenttable = new EnchantmentTableDisplay();
		enderchest = new EnderchestDisplay();
		furnace = new FurnaceDisplay();
		grindstone = new GrindstoneDisplay();
		loom = new LoomDisplay();
		smoker = new SmokerDisplay();
		stonecutter = new StonecutterDisplay();
		version = InteractionVisualizer.version;
	}
	
	public static void load() {
		HandlerList.unregisterAll(plugin);
		for (int taskid : tasks) {
			Bukkit.getScheduler().cancelTask(taskid);
		}
		tasks.clear();
		
		Bukkit.getPluginManager().registerEvents(new Debug(), plugin);
		Bukkit.getPluginManager().registerEvents(new com.loohp.interactionvisualizer.Listeners.Events(), plugin);
		Bukkit.getPluginManager().registerEvents(new PacketSending(), plugin);
		
		if (config.getBoolean("Blocks.CraftingTable.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new CraftingTableDisplay(), plugin);
			CraftingTableDisplay.run();
		}
		
		if (config.getBoolean("Blocks.Loom.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new LoomDisplay(), plugin);
			tasks.add(LoomDisplay.run());
		}
		
		if (config.getBoolean("Blocks.EnchantmentTable.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new EnchantmentTableDisplay(), plugin);
			tasks.add(EnchantmentTableDisplay.run());
		}
		
		if (config.getBoolean("Blocks.CartographyTable.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new CartographyTableDisplay(), plugin);
			tasks.add(CartographyTableDisplay.run());
		}
		
		if (config.getBoolean("Blocks.Anvil.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new AnvilDisplay(), plugin);
			tasks.add(AnvilDisplay.run());
		}
		
		if (config.getBoolean("Blocks.Grindstone.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new GrindstoneDisplay(), plugin);
			tasks.add(GrindstoneDisplay.run());
		}
		
		if (config.getBoolean("Blocks.Stonecutter.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new StonecutterDisplay(), plugin);
			tasks.add(StonecutterDisplay.run());
		}
		
		if (config.getBoolean("Blocks.BrewingStand.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new BrewingStandDisplay(), plugin);
			tasks.add(BrewingStandDisplay.run());
		}
		
		if (config.getBoolean("Blocks.Chest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new ChestDisplay(), plugin);
		}
		
		if (config.getBoolean("Blocks.DoubleChest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new DoubleChestDisplay(), plugin);
		}
		
		if (config.getBoolean("Blocks.Furnace.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new FurnaceDisplay(), plugin);
			tasks.add(FurnaceDisplay.run());
		}
		
		if (config.getBoolean("Blocks.BlastFurnace.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new BlastFurnaceDisplay(), plugin);
			tasks.add(BlastFurnaceDisplay.run());
		}
		
		if (config.getBoolean("Blocks.Smoker.Enabled") &&
				   (version.equals("1.14") || version.equals("1.15"))
				) {
			Bukkit.getPluginManager().registerEvents(new SmokerDisplay(), plugin);
			tasks.add(SmokerDisplay.run());
		}
		
		if (config.getBoolean("Blocks.EnderChest.Enabled")) {
			Bukkit.getPluginManager().registerEvents(new EnderchestDisplay(), plugin);
		}
	}

}
