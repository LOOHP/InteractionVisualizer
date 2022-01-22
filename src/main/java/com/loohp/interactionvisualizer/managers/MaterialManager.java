package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class MaterialManager {

    public static final String MATERIAL_CONFIG_ID = "material";

    public static FileConfiguration config;
    public static File file;

    private static Set<Material> tools = EnumSet.noneOf(Material.class);
    private static Set<Material> standing = EnumSet.noneOf(Material.class);
    private static Set<Material> lowblocks = EnumSet.noneOf(Material.class);
    private static Set<Material> blockexceptions = EnumSet.noneOf(Material.class);
    private static Set<Material> nonSolid = EnumSet.noneOf(Material.class);

    public static void setup() {
        if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
            InteractionVisualizer.plugin.getDataFolder().mkdir();
        }
        try {
            Config.loadConfig(MATERIAL_CONFIG_ID, new File(InteractionVisualizer.plugin.getDataFolder(), "material.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("material.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("material.yml"), true);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(InteractionVisualizer.plugin);
            return;
        }
        reload();
    }

    public static FileConfiguration getMaterialConfig() {
        return Config.getConfig(MATERIAL_CONFIG_ID).getConfiguration();
    }

    public static void saveConfig() {
        Config.getConfig(MATERIAL_CONFIG_ID).save();
    }

    public static void reloadConfig() {
        Config.getConfig(MATERIAL_CONFIG_ID).reload();
        reload();
    }

    public static void reload() {
        getTools().clear();
        getBlockexceptions().clear();
        getStanding().clear();
        getLowblocks().clear();
        getNonSolid().clear();

        for (String material : MaterialManager.getMaterialConfig().getStringList("Tools")) {
            try {
                getTools().add(Material.valueOf(material));
            } catch (Exception e) {
            }
        }

        for (String material : MaterialManager.getMaterialConfig().getStringList("BlockExceptions")) {
            try {
                getBlockexceptions().add(Material.valueOf(material));
            } catch (Exception e) {
            }
        }

        for (String material : MaterialManager.getMaterialConfig().getStringList("Standing")) {
            try {
                getStanding().add(Material.valueOf(material));
            } catch (Exception e) {
            }
        }

        for (String material : MaterialManager.getMaterialConfig().getStringList("LowBlocks")) {
            try {
                getLowblocks().add(Material.valueOf(material));
            } catch (Exception e) {
            }
        }

        for (Material material : Material.values()) {
            if (!material.isBlock()) {
                continue;
            }
            if (!material.isSolid()) {
                getNonSolid().add(material);
            }
        }
    }

    public static Set<Material> getTools() {
        return tools;
    }

    public static void setTools(EnumSet<Material> tools) {
        MaterialManager.tools = tools;
    }

    public static Set<Material> getStanding() {
        return standing;
    }

    public static void setStanding(Set<Material> standing) {
        if (standing instanceof EnumSet<?>) {
            MaterialManager.standing = EnumSet.copyOf(standing);
        } else {
            MaterialManager.standing = EnumSet.noneOf(Material.class);
            MaterialManager.standing.addAll(standing);
        }
    }

    public static Set<Material> getLowblocks() {
        return lowblocks;
    }

    public static void setLowblocks(Set<Material> lowblocks) {
        if (lowblocks instanceof EnumSet<?>) {
            MaterialManager.lowblocks = EnumSet.copyOf(lowblocks);
        } else {
            MaterialManager.lowblocks = EnumSet.noneOf(Material.class);
            MaterialManager.lowblocks.addAll(lowblocks);
        }
    }

    public static Set<Material> getBlockexceptions() {
        return blockexceptions;
    }

    public static void setBlockexceptions(Set<Material> blockexceptions) {
        if (blockexceptions instanceof EnumSet<?>) {
            MaterialManager.blockexceptions = EnumSet.copyOf(blockexceptions);
        } else {
            MaterialManager.blockexceptions = EnumSet.noneOf(Material.class);
            MaterialManager.blockexceptions.addAll(blockexceptions);
        }
    }

    public static Set<Material> getNonSolid() {
        return nonSolid;
    }

    public static void setNonSolid(Set<Material> nonSolid) {
        if (nonSolid instanceof EnumSet<?>) {
            MaterialManager.nonSolid = EnumSet.copyOf(nonSolid);
        } else {
            MaterialManager.nonSolid = EnumSet.noneOf(Material.class);
            MaterialManager.nonSolid.addAll(nonSolid);
        }
    }

}
