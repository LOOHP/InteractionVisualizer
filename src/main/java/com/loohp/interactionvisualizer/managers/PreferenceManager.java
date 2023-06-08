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

package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.database.Database;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.SynchronizedFilteredCollection;
import com.loohp.interactionvisualizer.utils.ArrayUtils;
import net.md_5.bungee.api.ChatColor;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public class PreferenceManager implements Listener, AutoCloseable {

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final InteractionVisualizer plugin;
    private final List<EntryKey> entries;
    private final Map<UUID, Map<Modules, BitSet>> preferences;

    private final Collection<Player> backingPlayerList;

    private final AtomicBoolean valid;

    public PreferenceManager(InteractionVisualizer plugin) {
        this.plugin = plugin;
        this.valid = new AtomicBoolean(true);
        this.entries = Collections.synchronizedList(ArrayUtils.putToArrayList(Database.getBitIndex(), new ArrayList<>()));
        this.preferences = new ConcurrentHashMap<>();
        this.backingPlayerList = Collections.synchronizedCollection(new LinkedHashSet<>());
        for (Player player : Bukkit.getOnlinePlayers()) {
            backingPlayerList.add(player);
            loadPlayer(player.getUniqueId(), player.getName(), true);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public synchronized void close() {
        backingPlayerList.clear();
        for (UUID uuid : preferences.keySet()) {
            savePlayer(uuid, true);
        }
        saveBitmaskIndex();
        valid.set(false);
    }

    public boolean isValid() {
        return valid.get();
    }

    public void saveBitmaskIndex() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractionVisualizer] Saving player preferences bitmask index, do not halt the server.");
        try {
            Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).pollDelay(0, TimeUnit.MILLISECONDS).until(() -> !Database.isLocked());
        } catch (ConditionTimeoutException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Tried to save player preference but database is locked for more than 30 seconds, performing save anyway...");
        }
        Database.setLocked(true);
        Database.setBitIndex(ArrayUtils.putToMap(entries, new HashMap<>()));
        Database.setLocked(false);
    }

    public void registerEntry(EntryKey entryKey) {
        registerEntry(Collections.singletonList(entryKey));
    }

    public void registerEntry(EntryKey... entryKeys) {
        if (entryKeys.length == 0) {
            return;
        }
        registerEntry(Arrays.asList(entryKeys));
    }

    public void registerEntry(List<EntryKey> entryKeys) {
        if (!entryKeys.isEmpty()) {
            synchronized (entries) {
                try {
                    Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).pollDelay(0, TimeUnit.MILLISECONDS).until(() -> !Database.isLocked());
                } catch (ConditionTimeoutException e) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Tried to save player preference but database is locked for more than 30 seconds, performing save anyway...");
                }
                Database.setLocked(true);
                List<EntryKey> updatedEntries = ArrayUtils.putToArrayList(Database.getBitIndex(), new ArrayList<>());
                entries.clear();
                entries.addAll(updatedEntries);
                boolean changes = false;
                for (EntryKey entry : entryKeys) {
                    if (!entries.contains(entry)) {
                        changes = true;
                        entries.add(entry);
                    }
                }
                if (changes) {
                    Database.setBitIndex(ArrayUtils.putToMap(entries, new HashMap<>()));
                }
                Database.setLocked(false);
            }
        }
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        backingPlayerList.add(player);
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            loadPlayer(player.getUniqueId(), player.getName(), true);
            updatePlayer(player, false);
        });
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        backingPlayerList.remove(player);
        InteractionVisualizer.asyncExecutorManager.runTaskAsynchronously(() -> {
            savePlayer(event.getPlayer().getUniqueId(), true);
        });
    }

    public void loadPlayer(UUID uuid, String name, boolean createIfNotFound) {
        if (createIfNotFound) {
            boolean newPlayer = false;
            if (!Database.playerExists(uuid)) {
                Database.createPlayer(uuid, name);
                newPlayer = true;
            }
            Map<Modules, BitSet> info = Database.getPlayerInfo(uuid);
            preferences.put(uuid, info);
            if (newPlayer && InteractionVisualizer.defaultDisabledAll) {
                setPlayerAllPreference(uuid, false, false);
                savePlayer(uuid, false);
            }
        } else {
            if (Database.playerExists(uuid)) {
                Map<Modules, BitSet> info = Database.getPlayerInfo(uuid);
                preferences.put(uuid, info);
            }
        }
    }

    public void savePlayer(UUID uuid, boolean unload) {
        Map<Modules, BitSet> info = unload ? preferences.remove(uuid) : preferences.get(uuid);
        if (info != null) {
            for (Entry<Modules, BitSet> entry : info.entrySet()) {
                switch (entry.getKey()) {
                    case HOLOGRAM:
                        Database.setHologram(uuid, entry.getValue());
                        break;
                    case ITEMDROP:
                        Database.setItemDrop(uuid, entry.getValue());
                        break;
                    case ITEMSTAND:
                        Database.setItemStand(uuid, entry.getValue());
                        break;
                }
            }
        }
    }

    public void unloadPlayerWithoutSaving(UUID uuid) {
        preferences.remove(uuid);
    }

    public void updatePlayer(Player player, boolean reset) {
        if (reset) {
            PacketManager.reset(player);
        } else {
            PacketManager.sendPlayerPackets(player);
        }
    }

    public boolean isRegisteredEntry(EntryKey entry) {
        return entries.contains(entry);
    }

    public List<EntryKey> getRegisteredEntries() {
        return Collections.unmodifiableList(entries);
    }

    public boolean getPlayerPreference(UUID uuid, Modules module, EntryKey entry) {
        int i = entries.indexOf(entry);
        if (i < 0) {
            return false;
        }
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            BitSet bitset = info.get(module);
            return !bitset.get(i);
        } else {
            return false;
        }
    }

    public Map<Modules, Map<EntryKey, Boolean>> getPlayerPreferences(UUID uuid) {
        Map<Modules, Map<EntryKey, Boolean>> preferences = new HashMap<>();
        for (Modules module : Modules.values()) {
            Map<EntryKey, Boolean> entryPreference = new HashMap<>();
            for (EntryKey entry : getRegisteredEntries()) {
                entryPreference.put(entry, getPlayerPreference(uuid, module, entry));
            }
            preferences.put(module, entryPreference);
        }
        return preferences;
    }

    public void setPlayerPreference(UUID uuid, Modules module, EntryKey entry, boolean enabled, boolean update) {
        int i = entries.indexOf(entry);
        if (i < 0) {
            return;
        }
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            BitSet bitset = info.get(module);
            bitset.set(i, !enabled);
        }
        if (update) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayer(player, true);
            }
        }
    }

    public void setPlayerAllPreference(UUID uuid, Modules module, boolean enabled, boolean update) {
        for (EntryKey entry : getRegisteredEntries()) {
            setPlayerPreference(uuid, module, entry, enabled, false);
        }
        if (update) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayer(player, true);
            }
        }
    }

    public void setPlayerAllPreference(UUID uuid, EntryKey entry, boolean enabled, boolean update) {
        for (Modules module : Modules.values()) {
            setPlayerPreference(uuid, module, entry, enabled, false);
        }
        if (update) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayer(player, true);
            }
        }
    }

    public void setPlayerAllPreference(UUID uuid, boolean enabled, boolean update) {
        for (Modules module : Modules.values()) {
            for (EntryKey entry : getRegisteredEntries()) {
                setPlayerPreference(uuid, module, entry, enabled, false);
            }
        }
        if (update) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                updatePlayer(player, true);
            }
        }
    }

    public boolean hasAnyPreferenceDisabled(UUID uuid, Modules module) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            BitSet bitset = info.get(module);
            return bitset.cardinality() > 0;
        }
        return false;
    }

    public boolean hasAnyPreferenceDisabled(UUID uuid, EntryKey entry) {
        int i = entries.indexOf(entry);
        if (i < 0) {
            return false;
        }
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                if (bitset.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyPreferenceDisabled(UUID uuid) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                if (bitset.cardinality() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyPreferenceEnabled(UUID uuid, Modules module) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            BitSet bitset = info.get(module);
            for (int i = 0; i < entries.size(); i++) {
                if (!bitset.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyPreferenceEnabled(UUID uuid, EntryKey entry) {
        int i = entries.indexOf(entry);
        if (i < 0) {
            return false;
        }
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                if (!bitset.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnyPreferenceEnabled(UUID uuid) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                for (int i = 0; i < entries.size(); i++) {
                    if (!bitset.get(i)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAllPreferenceEnabled(UUID uuid, Modules module) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            BitSet bitset = info.get(module);
            for (int i = 0; i < entries.size(); i++) {
                if (bitset.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasAllPreferenceEnabled(UUID uuid, EntryKey entry) {
        int i = entries.indexOf(entry);
        if (i < 0) {
            return false;
        }
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                if (bitset.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasAllPreferenceEnabled(UUID uuid) {
        Map<Modules, BitSet> info = preferences.get(uuid);
        if (info != null) {
            for (BitSet bitset : info.values()) {
                for (int i = 0; i < entries.size(); i++) {
                    if (bitset.get(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Collection<Player> getPlayerList(Modules module, EntryKey entry) {
        BooleanSupplier serverSetting;
        switch (module) {
            case HOLOGRAM:
                serverSetting = () -> InteractionVisualizer.hologramsEnabled && !InteractionVisualizer.hologramsDisabled.contains(entry);
                break;
            case ITEMDROP:
                serverSetting = () -> InteractionVisualizer.itemDropEnabled && !InteractionVisualizer.itemDropDisabled.contains(entry);
                break;
            case ITEMSTAND:
                serverSetting = () -> InteractionVisualizer.itemStandEnabled && !InteractionVisualizer.itemStandDisabled.contains(entry);
                break;
            default:
                serverSetting = () -> true;
                break;
        }
        return SynchronizedFilteredCollection.filterSynchronized(backingPlayerList, player -> {
            if (!serverSetting.getAsBoolean()) {
                return false;
            }
            if (!isRegisteredEntry(entry)) {
                return false;
            }
            return getPlayerPreference(player.getUniqueId(), module, entry);
        });
    }

    public Collection<Player> getPlayerListIgnoreServerSetting(Modules module, EntryKey entry) {
        return SynchronizedFilteredCollection.filterSynchronized(backingPlayerList, player -> {
            if (!isRegisteredEntry(entry)) {
                return false;
            }
            return getPlayerPreference(player.getUniqueId(), module, entry);
        });
    }

    public Collection<Player> getPlayerList() {
        return Collections.unmodifiableCollection(backingPlayerList);
    }

}
