package com.loohp.interactionvisualizer.Metrics;

import java.util.concurrent.Callable;

import org.bukkit.configuration.file.FileConfiguration;

import com.loohp.interactionvisualizer.InteractionVisualizer;

public class Charts {
	
	public static FileConfiguration config = InteractionVisualizer.config;
	
	public static void registerCharts(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SimplePie("anvil_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Anvil.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("blastfurnace_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.BlastFurnace.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("brewingstand_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.BrewingStand.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("cartographytable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.CartographyTable.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("chest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Chest.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("craftingtable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.CraftingTable.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("doublechest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.DoubleChest.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("enchantmenttable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.EnchantmentTable.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("furnace_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Furnace.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("grindstone_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Grindstone.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("loom_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Loom.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("smoker_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Smoker.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("stonecutter_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (config.getBoolean("Blocks.Stonecutter.Enabled") == true) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
	}

}
