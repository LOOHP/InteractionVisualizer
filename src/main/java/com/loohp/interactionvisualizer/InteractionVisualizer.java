/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.config.Config;
import com.loohp.interactionvisualizer.database.Database;
import com.loohp.interactionvisualizer.managers.AsyncExecutorManager;
import com.loohp.interactionvisualizer.managers.LangManager;
import com.loohp.interactionvisualizer.managers.LightManager;
import com.loohp.interactionvisualizer.managers.MaterialManager;
import com.loohp.interactionvisualizer.managers.MusicManager;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.PreferenceManager;
import com.loohp.interactionvisualizer.managers.SoundManager;
import com.loohp.interactionvisualizer.managers.TaskManager;
import com.loohp.interactionvisualizer.managers.TileEntityManager;
import com.loohp.interactionvisualizer.metrics.Charts;
import com.loohp.interactionvisualizer.metrics.Metrics;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.ILightManager;
import com.loohp.interactionvisualizer.placeholderAPI.Placeholders;
import com.loohp.interactionvisualizer.protocol.WatchableCollection;
import com.loohp.interactionvisualizer.updater.Updater;
import com.loohp.interactionvisualizer.updater.Updater.UpdaterResponse;
import com.loohp.interactionvisualizer.utils.LanguageUtils;
import com.loohp.interactionvisualizer.utils.MCVersion;
import com.loohp.interactionvisualizer.utils.PotionUtils;
import com.loohp.yamlconfiguration.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InteractionVisualizer extends JavaPlugin {

    public static final int BSTATS_PLUGIN_ID = 7024;
    public static final String CONFIG_ID = "config";

    public static InteractionVisualizer plugin = null;
    public static ProtocolManager protocolManager;

    public static String exactMinecraftVersion;
    public static MCVersion version;
    public static Integer metaversion = 0;

    public static Boolean lightapi = false;
    public static Boolean openinv = false;
    public static Boolean vanish = false;
    public static Boolean cmi = false;
    public static Boolean ess3 = false;

    public static Set<String> exemptBlocks = new HashSet<>();
    public static Set<String> disabledWorlds = new HashSet<>();

    public static Reference<World> defaultWorld;
    public static Location defaultLocation;

    public static boolean itemStandEnabled = true;
    public static boolean itemDropEnabled = true;
    public static boolean hologramsEnabled = true;
    public static Set<EntryKey> itemStandDisabled = new HashSet<>();
    public static Set<EntryKey> itemDropDisabled = new HashSet<>();
    public static Set<EntryKey> hologramsDisabled = new HashSet<>();

    public static Double playerPickupYOffset = 0.0;

    public static Integer tileEntityCheckingRange = 1;
    public static double ignoreWalkSquared = 0.0;
    public static double ignoreFlySquared = 0.0;
    public static double ignoreGlideSquared = 0.0;

    public static Boolean handMovementEnabled = true;

    public static Integer lightUpdatePeriod = 10;

    public static boolean updaterEnabled = true;

    public static Map<World, Integer> playerTrackingRange = new HashMap<>();
    public static boolean hideIfObstructed = false;
    public static String language = "en_us";

    public static boolean allPacketsSync = false;
    public static boolean defaultDisabledAll = false;

    public static ILightManager lightManager;
    public static PreferenceManager preferenceManager;
    public static AsyncExecutorManager asyncExecutorManager;

    private static void unsupportedMessage() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This version of minecraft is unsupported!");
    }

    private static void hookMessage(String pluginName) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "[InteractionVisualizer] InteractionVisualizer has hooked into " + pluginName + "!");
    }

    public static void sendMessage(CommandSender sender, Component component) {
        if (version.isLegacyRGB()) {
            try {
                sender.spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.colorDownsamplingGson().serialize(component)));
            } catch (Throwable e) {
                if (sender instanceof Player) {
                    ((Player) sender).spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.colorDownsamplingGson().serialize(component)));
                } else {
                    sender.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
                }
            }
        } else {
            sender.spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(component)));
        }
    }

    public static boolean isPluginEnabled(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public void onEnable() {
        plugin = this;

        protocolManager = ProtocolLibrary.getProtocolManager();

        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        exactMinecraftVersion = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("(") + 5, Bukkit.getVersion().indexOf(")"));
        version = MCVersion.fromPackageName(getServer().getClass().getPackage().getName());

        ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("InteractionVisualizer Async Processing Thread #%d").build();
        ExecutorService threadPool = new ThreadPoolExecutor(8, 120, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), factory);
        asyncExecutorManager = new AsyncExecutorManager(threadPool);

        switch (version) {
            case V1_19_4:
            case V1_19_3:
            case V1_19:
            case V1_18_2:
            case V1_18:
            case V1_17:
                metaversion = 4;
                break;
            case V1_16_4:
            case V1_16_2:
            case V1_16:
            case V1_15:
                metaversion = 3;
                break;
            case V1_14:
                metaversion = 2;
                break;
            case V1_13_1:
            case V1_13:
                metaversion = 1;
                break;
            case V1_12:
            case V1_11:
                metaversion = 0;
                break;
            default:
                unsupportedMessage();
                break;
        }

        if (isPluginEnabled("LightAPI")) {
            try {
                Class.forName("ru.beykerykt.lightapi.utils.Debug");
                hookMessage("LightAPI");
                lightapi = true;
                lightManager = new LightManager(this);
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (!lightapi) {
            if (version.isOlderOrEqualTo(MCVersion.V1_16_4)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractionVisualizer] LightAPI (Fork) is recommended to be installed on servers with Minecraft version 1.16.5 or below!");
            }
            lightManager = ILightManager.DUMMY_INSTANCE;
        }
        if (isPluginEnabled("OpenInv")) {
            hookMessage("OpenInv");
            openinv = true;
        }
        if (isPluginEnabled("SuperVanish") || isPluginEnabled("PremiumVanish")) {
            hookMessage("SuperVanish/PremiumVanish");
            vanish = true;
        }
        if (isPluginEnabled("CMI")) {
            hookMessage("CMI");
            cmi = true;
        }
        if (isPluginEnabled("Essentials")) {
            hookMessage("Essentials");
            ess3 = true;
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        try {
            Config.loadConfig(CONFIG_ID, new File(getDataFolder(), "config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), getClass().getClassLoader().getResourceAsStream("config.yml"), true);
        } catch (IOException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        loadConfig();

        defaultWorld = new WeakReference<>(getServer().getWorlds().get(0));
        defaultLocation = new Location(getDefaultWorld(), 0, 0, 0);
        if (!version.isLegacy() && !version.equals(MCVersion.V1_13) && !version.equals(MCVersion.V1_13_1)) {
            getDefaultWorld().setChunkForceLoaded(0, 0, true);
        }

        if (getConfiguration().getBoolean("Options.DownloadLanguageFiles")) {
            getServer().getScheduler().runTaskAsynchronously(this, () -> LangManager.generate());
        }

        WatchableCollection.setup();
        SoundManager.setup();
        MusicManager.setup();
        Database.setup();
        preferenceManager = new PreferenceManager(this);
        TaskManager.setup();
        TileEntityManager._init_();
        PacketManager.run();
        PacketManager.dynamicEntity();
        PotionUtils.setupPotions();

        MaterialManager.setup();

        getCommand("interactionvisualizer").setExecutor(new Commands());

        TaskManager.run();

        Charts.registerCharts(metrics);

        if (isPluginEnabled("PlaceholderAPI")) {
            new Placeholders().register();
        }

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getNearbyEntities(new Location(world, 0, 0, 0), 2, 2, 2)) {
                if (entity.getScoreboardTags().contains("isInteractionVisualizer")) {
                    entity.remove();
                }
            }
        }

        exemptBlocks.add("CRAFTING_TABLE");
        exemptBlocks.add("WORKBENCH");
        exemptBlocks.add("LOOM");
        exemptBlocks.add("SMITHING_TABLE");
        exemptBlocks.add("SPAWNER");
        exemptBlocks.add("MOB_SPAWNER");
        exemptBlocks.add("BEACON");


        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] InteractionVisualizer has been enabled!");

        Bukkit.getScheduler().runTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PacketManager.playerStatus.put(player, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            }
        });

        InteractionVisualizer.asyncExecutorManager.runTaskLaterAsynchronously(() -> {
            if (updaterEnabled) {
                UpdaterResponse version = Updater.checkUpdate();
                if (!version.getResult().equals("latest")) {
                    Updater.sendUpdateMessage(Bukkit.getConsoleSender(), version.getResult(), version.getSpigotPluginId());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("interactionvisualizer.update")) {
                            Updater.sendUpdateMessage(player, version.getResult(), version.getSpigotPluginId());
                        }
                    }
                }
            }
        }, 100);
		/*
		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.SPAWN_ENTITY_LIVING) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.SPAWN_ENTITY_LIVING)) {
		        	return;
		        }

		        PacketContainer packet = event.getPacket();
		        if (packet.getIntegers().read(1) != 1) {
		        	return;
		        }

		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Spawn Packet to " + event.getPlayer().getName() + " " + packet.getDoubles().read(0) + " " + packet.getDoubles().read(1) + " " + packet.getDoubles().read(2));
		    }
		});

		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_METADATA) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_METADATA)) {
		        	return;
		        }

		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Update Packet to " + event.getPlayer().getName());
		    }
		});

		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ENTITY_DESTROY) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ENTITY_DESTROY)) {
		        	return;
		        }

		        Bukkit.getConsoleSender().sendMessage("Sending ArmorStand Remove Packet to " + event.getPlayer().getName());
		    }
		});

		protocolManager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.MONITOR, PacketType.Play.Server.ANIMATION) {
		    @Override
		    public void onPacketSending(PacketEvent event) {
		        if (!event.getPacketType().equals(PacketType.Play.Server.ANIMATION)) {
		        	return;
		        }

		        Bukkit.getConsoleSender().sendMessage("Sending Animation Packet to " + event.getPlayer().getName());
		    }
		});
		*/
    }

    @Override
    public void onDisable() {
        preferenceManager.close();

        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "[InteractionVisualizer] Plugin reload detected, attempting to despawn all visual entities. If anything went wrong, please restart! (Reloads are always not recommended)");
            int[] entityIdArray = PacketManager.active.keySet().stream().mapToInt(each -> each.getEntityId()).toArray();
            PacketContainer[] packets = NMS.getInstance().createEntityDestroyPacket(entityIdArray);

            for (Player player : Bukkit.getOnlinePlayers()) {
                for (PacketContainer packet : packets) {
                    protocolManager.sendServerPacket(player, packet);
                }
            }
        }

        asyncExecutorManager.close();
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] InteractionVisualizer has been disabled!");
    }

    public YamlConfiguration getConfiguration() {
        return Config.getConfig(CONFIG_ID).getConfiguration();
    }

    public void loadConfig() {
        Config config = Config.getConfig(CONFIG_ID);
        config.reload();

        itemStandEnabled = getConfiguration().getBoolean("Modules.ItemStand.Enabled");
        itemDropEnabled = getConfiguration().getBoolean("Modules.ItemDrop.Enabled");
        hologramsEnabled = getConfiguration().getBoolean("Modules.Hologram.Enabled");

        itemStandDisabled = getConfiguration().getStringList("Modules.ItemStand.OverridingDisabled").stream().map(each -> new EntryKey(each)).collect(Collectors.toSet());
        itemDropDisabled = getConfiguration().getStringList("Modules.ItemDrop.OverridingDisabled").stream().map(each -> new EntryKey(each)).collect(Collectors.toSet());
        hologramsDisabled = getConfiguration().getStringList("Modules.Hologram.OverridingDisabled").stream().map(each -> new EntryKey(each)).collect(Collectors.toSet());

        playerPickupYOffset = getConfiguration().getDouble("Settings.PickupAnimationPlayerYOffset");

        tileEntityCheckingRange = getConfiguration().getInt("TileEntityUpdate.CheckingRange");
        ignoreWalkSquared = getConfiguration().getDouble("TileEntityUpdate.IgnoreMovementSpeed.Normal");
        ignoreWalkSquared *= ignoreWalkSquared;
        ignoreFlySquared = getConfiguration().getDouble("TileEntityUpdate.IgnoreMovementSpeed.Flying");
        ignoreFlySquared *= ignoreFlySquared;
        ignoreGlideSquared = getConfiguration().getDouble("TileEntityUpdate.IgnoreMovementSpeed.Gliding");
        ignoreGlideSquared *= ignoreGlideSquared;

        handMovementEnabled = getConfiguration().getBoolean("Settings.UseHandSwingAnimation");

        disabledWorlds = new HashSet<>(getConfiguration().getStringList("Settings.DisabledWorlds"));
        hideIfObstructed = getConfiguration().getBoolean("Settings.HideIfViewObstructed");

        lightUpdatePeriod = getConfiguration().getInt("LightUpdate.Period");

        updaterEnabled = getConfiguration().getBoolean("Options.Updater");

        language = getConfiguration().getString("Settings.Language");

        playerTrackingRange.clear();
        int defaultRange = getServer().spigot().getConfig().getInt("world-settings.default.entity-tracking-range.players", 64);
        for (World world : getServer().getWorlds()) {
            int range = getServer().spigot().getConfig().getInt("world-settings." + world.getName() + ".entity-tracking-range.players", defaultRange);
            playerTrackingRange.put(world, range);
        }

        allPacketsSync = getConfiguration().getBoolean("Settings.SendAllPacketsInSync");
        defaultDisabledAll = getConfiguration().getBoolean("Settings.DefaultDisableAll");

        LanguageUtils.loadTranslations(language);

        getServer().getPluginManager().callEvent(new InteractionVisualizerReloadEvent());
    }

    public static World getDefaultWorld() {
        if (defaultWorld == null) {
            World world = Bukkit.getWorlds().get(0);
            defaultWorld = new WeakReference<>(world);
            return world;
        } else {
            return defaultWorld.get();
        }
    }

}
