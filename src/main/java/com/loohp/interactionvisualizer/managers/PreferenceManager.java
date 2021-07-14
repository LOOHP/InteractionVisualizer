package com.loohp.interactionvisualizer.managers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.database.Database;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.objectholders.SynchronizedFilteredCollection;
import com.loohp.interactionvisualizer.utils.ArrayUtils;

public class PreferenceManager implements Listener, AutoCloseable {
	
	private InteractionVisualizer plugin;
	private List<EntryKey> entries;
	private Map<UUID, Map<Modules, BitSet>> preferences;
	
	private Collection<Player> backingPlayerList;
	
	private AtomicBoolean valid;
	
	public PreferenceManager(InteractionVisualizer plugin) {
		this.plugin = plugin;
		this.valid = new AtomicBoolean(true);
		this.entries = Collections.synchronizedList(ArrayUtils.putToArrayList(Database.getBitIndex(), new ArrayList<>()));
		this.preferences = new ConcurrentHashMap<>();
		this.backingPlayerList = Collections.synchronizedCollection(new ArrayList<>());
		Bukkit.getPluginManager().registerEvents(this, plugin);
		for (Player player : Bukkit.getOnlinePlayers()) {
			backingPlayerList.add(player);
			loadPlayer(player.getUniqueId(), player.getName(), true);
		}
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
		try {
			Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).pollDelay(0, TimeUnit.MILLISECONDS).until(() -> !Database.isLocked());
			Database.setLocked(true);
			Database.setBitIndex(ArrayUtils.putToMap(entries, new HashMap<>()));
			Database.setLocked(false);
		} catch (ConditionTimeoutException e) {
			new RuntimeException("Tried to save player preference but database is locked for more than 5 secionds", e).printStackTrace();
		}
	}
	
	public void registerEntry(EntryKey entryKey, EntryKey... entryKeys) {
		EntryKey[] keys = new EntryKey[entryKeys.length + 1];
		keys[0] = entryKey;
		System.arraycopy(entryKeys, 0, keys, 1, entryKeys.length);
		registerEntry(keys);
	}
	
	public void registerEntry(EntryKey[] entryKeys) {
		if (entryKeys.length > 0) {
			try {
				synchronized (entries) {
					Awaitility.await().atMost(5, TimeUnit.SECONDS).pollInterval(500, TimeUnit.MILLISECONDS).pollDelay(0, TimeUnit.MILLISECONDS).until(() -> !Database.isLocked());
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
			} catch (ConditionTimeoutException e) {
				new RuntimeException("Tried to save player preference but database is locked for more than 5 secionds", e).printStackTrace();
			}
		}
	}
	
	@EventHandler
	public void onJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		backingPlayerList.add(player);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			loadPlayer(player.getUniqueId(), player.getName(), true);
			updatePlayer(player, false);
		});
	}
	
	@EventHandler
	public void onQuitEvent(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		backingPlayerList.remove(player);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
	
	public void updatePlayer(Player player, boolean reset) {
		if (reset) {
			PacketManager.reset(player);
		} else {
			PacketManager.sendPlayerPackets(player);
		}
	}
	
	public boolean isRegistryEntry(EntryKey entry) {
		int i = entries.indexOf(entry);
		return !(i < 0);
	}
	
	public List<EntryKey> getRegisteredEntries() {
		return new ArrayList<>(entries);
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
	
	public Collection<Player> getPlayerList(Modules module, EntryKey entry) {
		Supplier<Boolean> serverSetting;
		switch (module) {
		case HOLOGRAM:
			serverSetting = () -> InteractionVisualizer.hologramsEnabled;
			break;
		case ITEMDROP:
			serverSetting = () -> InteractionVisualizer.itemDropEnabled;
			break;
		case ITEMSTAND:
			serverSetting = () -> InteractionVisualizer.itemStandEnabled;
			break;
		default:
			serverSetting = () -> true;
			break;
		}
		return SynchronizedFilteredCollection.filterSynchronized(backingPlayerList, player -> {
			if (!serverSetting.get()) {
				return false;
			}
			if (!isRegistryEntry(entry)) {
				return false;
			}
			return getPlayerPreference(player.getUniqueId(), module, entry);
		});
	}
	
	public Collection<Player> getPlayerListIgnoreServerSetting(Modules module, EntryKey entry) {
		return SynchronizedFilteredCollection.filterSynchronized(backingPlayerList, player -> {
			if (!isRegistryEntry(entry)) {
				return false;
			}
			return getPlayerPreference(player.getUniqueId(), module, entry);
		});
	}

}
