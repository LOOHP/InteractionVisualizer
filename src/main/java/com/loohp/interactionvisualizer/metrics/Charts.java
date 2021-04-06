package com.loohp.interactionvisualizer.metrics;

import java.util.concurrent.Callable;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;

public class Charts {
	
	public static void registerCharts(Metrics metrics) {
		
		metrics.addCustomChart(new Metrics.SimplePie("line_of_sight_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.hideIfObstructed) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("anvil_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Anvil.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("beacon_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Beacon.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("beehive_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.BeeHive.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("beenest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.BeeNest.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("blastfurnace_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.BlastFurnace.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("brewingstand_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.BrewingStand.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("cartographytable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.CartographyTable.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("chest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Chest.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("craftingtable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.CraftingTable.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("dispenser_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Dispenser.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("doublechest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.DoubleChest.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("dropper_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Dropper.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("enchantmenttable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.EnchantmentTable.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("enderchest_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.EnderChest.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("furnace_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Furnace.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("grindstone_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Grindstone.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("hopper_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Hopper.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("loom_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Loom.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("smoker_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Smoker.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("shulkerbox_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.ShulkerBox.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("smithingtable_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.SmithingTable.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("stonecutter_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.Stonecutter.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("noteblock_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.NoteBlock.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("jukebox_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Blocks.JukeBox.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		metrics.addCustomChart(new Metrics.SimplePie("villager_enabled", new Callable<String>() {
	        @Override
	        public String call() throws Exception {
	        	String string = "Disabled";
	        	if (InteractionVisualizer.plugin.getConfig().getBoolean("Entities.Villager.Enabled")) {
	        		string = "Enabled";
	        	}
	            return string;
	        }
	    }));
		
		
		//----
		metrics.addCustomChart(new Metrics.SingleLineChart("total_tile_entities_in_range_visualizing", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
            	int total = 0;
            	for (TileEntityType type : TileEntityType.values()) {
            		total += TileEntityManager.getTileEntites(type).size();
            	}
                return total;
            }
        }));
		
	}

}
