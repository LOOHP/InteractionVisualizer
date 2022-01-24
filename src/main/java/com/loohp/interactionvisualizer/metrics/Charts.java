package com.loohp.interactionvisualizer.metrics;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.TileEntity.TileEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

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
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Anvil.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("banner_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Banner.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("barrel_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Barrel.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("beacon_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Beacon.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("beehive_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.BeeHive.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("beenest_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.BeeNest.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("blastfurnace_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.BlastFurnace.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("brewingstand_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.BrewingStand.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("campfire_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Campfire.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("cartographytable_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.CartographyTable.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("chest_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Chest.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("conduit_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Conduit.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("craftingtable_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.CraftingTable.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("dispenser_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Dispenser.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("doublechest_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.DoubleChest.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("dropper_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Dropper.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("enchantmenttable_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.EnchantmentTable.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("enderchest_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.EnderChest.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("furnace_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Furnace.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("grindstone_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Grindstone.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("hopper_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Hopper.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("lectern_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Lectern.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("loom_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Loom.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("shulkerbox_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.ShulkerBox.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("smithingtable_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.SmithingTable.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("smoker_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Smoker.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("soulcampfire_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.SoulCampfire.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("spawner_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Spawner.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("stonecutter_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.Stonecutter.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("noteblock_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.NoteBlock.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("jukebox_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Blocks.JukeBox.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("item_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Entities.Item.Enabled")) {
                    string = "Enabled";
                }
                return string;
            }
        }));

        metrics.addCustomChart(new Metrics.SimplePie("villager_enabled", new Callable<String>() {
            @Override
            public String call() throws Exception {
                String string = "Disabled";
                if (InteractionVisualizer.plugin.getConfiguration().getBoolean("Entities.Villager.Enabled")) {
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

        metrics.addCustomChart(new Metrics.DrilldownPie("visualizer_displays_from_addons", new Callable<Map<String, Map<String, Integer>>>() {
            @Override
            public Map<String, Map<String, Integer>> call() throws Exception {
                Map<String, Map<String, Integer>> map = new HashMap<>();
                for (EntryKey entryKey : InteractionVisualizerAPI.getRegisteredEntries()) {
                    if (!entryKey.isNative()) {
                        Map<String, Integer> entry = map.get(entryKey.getNamespace());
                        if (entry == null) {
                            map.put(entryKey.getNamespace(), entry = new HashMap<>());
                        }
                        Integer value = entry.get(entryKey.getKey());
                        if (value == null) {
                            entry.put(entryKey.getKey(), 1);
                        } else {
                            entry.put(entryKey.getKey(), value + 1);
                        }
                    }
                }
                return map;
            }
        }));

    }

}
