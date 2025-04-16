/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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

package com.loohp.interactionvisualizer.entities;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.api.events.InteractionVisualizerReloadEvent;
import com.loohp.interactionvisualizer.managers.PlayerLocationManager;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.WrappedIterable;
import com.loohp.interactionvisualizer.utils.ChatColorUtils;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import com.loohp.interactionvisualizer.utils.ItemNameUtils;
import com.loohp.interactionvisualizer.utils.LineOfSightUtils;
import com.loohp.interactionvisualizer.utils.SyncUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ItemDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("item");

    private final Map<Item, Set<Player>> outOfRangePlayersMap = Collections.synchronizedMap(new WeakHashMap<>());

    private String regularFormatting;
    private String singularFormatting;
    private String toolsFormatting;
    private String highColor = "";
    private String mediumColor = "";
    private String lowColor = "";
    private int cramp = 6;
    private int updateRate = 20;
    private boolean stripColorBlacklist;
    private BiPredicate<String, Material> blacklist;

    public ItemDisplay() {
        onReload(new InteractionVisualizerReloadEvent());
    }

    @EventHandler
    public void onReload(InteractionVisualizerReloadEvent event) {
        regularFormatting = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.RegularFormat"));
        singularFormatting = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.SingularFormat"));
        toolsFormatting = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.ToolsFormat"));
        highColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.Color.High"));
        mediumColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.Color.Medium"));
        lowColor = ChatColorUtils.translateAlternateColorCodes('&', InteractionVisualizer.plugin.getConfiguration().getString("Entities.Item.Options.Color.Low"));
        cramp = InteractionVisualizer.plugin.getConfiguration().getInt("Entities.Item.Options.Cramping");
        updateRate = InteractionVisualizer.plugin.getConfiguration().getInt("Entities.Item.Options.UpdateRate");
        stripColorBlacklist = InteractionVisualizer.plugin.getConfiguration().getBoolean("Entities.Item.Options.Blacklist.StripColorWhenMatching");
        blacklist = InteractionVisualizer.plugin.getConfiguration().getList("Entities.Item.Options.Blacklist.List").stream().map(each -> {
            @SuppressWarnings("unchecked")
            List<String> entry = (List<String>) each;
            Pattern pattern = Pattern.compile(entry.get(0));
            Predicate<String> name = str -> pattern.matcher(str).matches();
            Predicate<Material> material;
            if (entry.size() > 1 && !entry.get(1).equals("*")) {
                try {
                    Material m = Material.valueOf(entry.get(1).toUpperCase());
                    material = e -> e.equals(m);
                } catch (Exception er) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] " + entry.get(1).toUpperCase() + " is not a valid material");
                    material = e -> true;
                }
            } else {
                material = e -> true;
            }
            Predicate<Material> finalmaterial = material;
            return (BiPredicate<String, Material>) (s, m) -> name.test(s) && finalmaterial.test(m);
        }).reduce(BiPredicate::or).orElse((s, m) -> false);
    }

    @Override
    public EntryKey key() {
        return KEY;
    }

    @Override
    public int gc() {
        return -1;
    }

    @Override
    public int run() {
        return new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (--i > 0) {
                    return;
                }
                i = updateRate;
                for (World world : Bukkit.getWorlds()) {
                    WrappedIterable<?, Entity> itr = NMS.getInstance().getEntities(world);
                    Set<Item> items = new HashSet<>();
                    for (Entity entity : itr) {
                        if (entity instanceof Item) {
                            items.add((Item) entity);
                        }
                    }
                    for (Item item : items) {
                        SyncUtils.runAsyncWithSyncCondition(item::isValid, () -> tick(item, items));
                    }
                }
            }
        }.runTaskTimer(InteractionVisualizer.plugin, 0, 1).getTaskId();
    }

    private void tick(Item item, Collection<Item> items) {
        try {
            World world = item.getWorld();
            Location location = item.getLocation();
            BoundingBox area = BoundingBox.of(item.getLocation(), 0.5, 0.5, 0.5);
            int ticks = NMS.getInstance().getItemAge(item);
            ItemStack itemstack = item.getItemStack();
            if (itemstack == null) {
                itemstack = new ItemStack(Material.AIR);
            } else {
                itemstack = itemstack.clone();
            }
            Component name = ItemNameUtils.getDisplayName(itemstack);
            String matchingname = getMatchingName(itemstack, stripColorBlacklist);

            if (!blacklist.test(matchingname, itemstack.getType())) {
                if (item.getPickupDelay() >= Short.MAX_VALUE || ticks < 0 || isCramping(world, area, items)) {
                    List<?> watcher = NMS.getInstance().resetCustomNameWatchableCollection(item);
                    Object defaultPacket = NMS.getInstance().createEntityMetadataPacket(item.getEntityId(), watcher);
                    Collection<Player> players = InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY);
                    for (Player player : players) {
                        NMS.getInstance().sendPacket(player, defaultPacket);
                    }
                } else {
                    int amount = itemstack.getAmount();
                    String durDisplay = null;

                    if (itemstack.getType().getMaxDurability() > 0) {
                        int durability = itemstack.getType().getMaxDurability() - ((Damageable) itemstack.getItemMeta()).getDamage();
                        int maxDur = itemstack.getType().getMaxDurability();
                        double percentage = ((double) durability / (double) maxDur) * 100;
                        String color;
                        if (percentage > 66.666) {
                            color = highColor;
                        } else if (percentage > 33.333) {
                            color = mediumColor;
                        } else {
                            color = lowColor;
                        }
                        durDisplay = color + durability + "/" + maxDur;
                    }

                    int despawnRate = NMS.getInstance().getItemDespawnRate(item);
                    int ticksLeft = despawnRate - ticks;
                    int secondsLeft = ticksLeft / 20;
                    String timerColor;
                    if (secondsLeft <= 30) {
                        timerColor = lowColor;
                    } else if (secondsLeft <= 120) {
                        timerColor = mediumColor;
                    } else {
                        timerColor = highColor;
                    }

                    String timer = timerColor + String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);

                    Component display;
                    String line1;
                    if (ticksLeft >= 600 && durDisplay != null) {
                        line1 = toolsFormatting.replace("{Amount}", amount + "").replace("{Timer}", timer).replace("{Durability}", durDisplay);
                    } else {
                        if (amount == 1) {
                            line1 = singularFormatting.replace("{Amount}", amount + "").replace("{Timer}", timer);
                        } else {
                            line1 = regularFormatting.replace("{Amount}", amount + "").replace("{Timer}", timer);
                        }
                    }
                    display = ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(line1));
                    display = display.replaceText(TextReplacementConfig.builder().matchLiteral("{Item}").replacement(name).build());

                    List<?> modifiedWatcher = NMS.getInstance().createCustomNameWatchableCollection(display);
                    List<?> defaultWatcher = NMS.getInstance().resetCustomNameWatchableCollection(item);

                    Object modifiedPacket = NMS.getInstance().createEntityMetadataPacket(item.getEntityId(), modifiedWatcher);
                    Object defaultPacket = NMS.getInstance().createEntityMetadataPacket(item.getEntityId(), defaultWatcher);

                    Location entityCenter = location.clone();
                    entityCenter.setY(entityCenter.getY() + item.getHeight() * 1.7);

                    Set<Player> outOfRangePlayers;
                    synchronized (outOfRangePlayersMap) {
                        outOfRangePlayers = outOfRangePlayersMap.get(item);
                        if (outOfRangePlayers == null) {
                            outOfRangePlayers = ConcurrentHashMap.newKeySet();
                            outOfRangePlayersMap.put(item, outOfRangePlayers);
                        }
                    }

                    Collection<Player> players = location.getWorld().getPlayers();
                    Collection<Player> enabledPlayers = InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY);
                    Collection<Player> playersInRange = PlayerLocationManager.filterOutOfRange(players, location, player -> !InteractionVisualizer.hideIfObstructed || LineOfSightUtils.hasLineOfSight(player.getEyeLocation(), entityCenter));
                    for (Player player : players) {
                        if (playersInRange.contains(player) && enabledPlayers.contains(player)) {
                            NMS.getInstance().sendPacket(player, modifiedPacket);
                            outOfRangePlayers.remove(player);
                        } else if (!outOfRangePlayers.contains(player)) {
                            NMS.getInstance().sendPacket(player, defaultPacket);
                            outOfRangePlayers.add(player);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMatchingName(ItemStack item, boolean stripColor) {
        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.getDisplayName() != null) {
                if (stripColor) {
                    return ChatColorUtils.stripColor(meta.getDisplayName());
                } else {
                    return meta.getDisplayName();
                }
            }
        }
        return "";
    }

    private boolean isCramping(World world, BoundingBox area, Collection<? extends Entity> items) {
        if (cramp <= 0) {
            return false;
        }
        try {
            return items.stream().filter(each -> each != null && each.getWorld().equals(world) && area.contains(each.getLocation().toVector())).skip(cramp).findAny().isPresent();
        } catch (Throwable e) {
            return false;
        }
    }

}
