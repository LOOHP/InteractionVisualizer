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

package com.loohp.interactionvisualizer.blocks;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.api.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.managers.MusicManager;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.LanguageUtils;
import com.loohp.interactionvisualizer.utils.LegacyInstrumentUtils;
import com.loohp.interactionvisualizer.utils.MCVersion;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NoteBlockDisplay extends VisualizerRunnableDisplay implements Listener {

    public static final EntryKey KEY = new EntryKey("note_block");

    public ConcurrentHashMap<Block, ConcurrentHashMap<String, Object>> displayingNotes = new ConcurrentHashMap<>();

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
        return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
            Iterator<Entry<Block, ConcurrentHashMap<String, Object>>> itr = displayingNotes.entrySet().iterator();
            while (itr.hasNext()) {
                Entry<Block, ConcurrentHashMap<String, Object>> entry = itr.next();
                long unix = System.currentTimeMillis();
                long timeout = (long) entry.getValue().get("Timeout");
                if (unix > timeout) {
                    ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
                    Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand));
                    itr.remove();
                }
            }
        }, 0, 20).getTaskId();
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onUseNoteBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!block.getType().equals(Material.NOTE_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode().equals(GameMode.SPECTATOR)) {
            return;
        }

        boolean holdingAir = player.getEquipment().getItemInMainHand() == null || (player.getEquipment().getItemInMainHand().getType().equals(Material.AIR));
        if (player.isSneaking() && !holdingAir) {
            return;
        }

        BlockFace face = event.getBlockFace();
        Location textLocation = getFaceOffset(block, face);
        Bukkit.getScheduler().runTaskLater(InteractionVisualizer.plugin, () -> {
            if (!block.getType().equals(Material.NOTE_BLOCK)) {
                return;
            }
            ConcurrentHashMap<String, Object> map = displayingNotes.get(block);
            ArmorStand stand = map == null ? new ArmorStand(textLocation.clone().add(0.0, -0.3, 0.0)) : (ArmorStand) map.get("Stand");
            stand.teleport(textLocation.clone().add(0.0, -0.3, 0.0));
            setStand(stand);

            map = map == null ? new ConcurrentHashMap<String, Object>() : map;
            map.put("Stand", stand);
            map.put("Timeout", System.currentTimeMillis() + 3000);
            displayingNotes.put(block, map);

            Block topBlock = block.getRelative(BlockFace.UP);
            Collection<ItemStack> topBlockDrops;
            Component component;
            if (InteractionVisualizer.version.isNewerThan(MCVersion.V1_19) && isHead(topBlock) && !(topBlockDrops = topBlock.getDrops()).isEmpty()) {
                ItemStack skull = topBlockDrops.iterator().next();
                String translatable = LanguageUtils.getTranslationKey(skull);
                String owner = NBTEditor.getString(skull, "SkullOwner", "Name");
                if (owner == null) {
                    component = Component.translatable(translatable == null ? "" : translatable).color(NamedTextColor.YELLOW);
                } else {
                    component = Component.translatable(translatable == null ? "" : translatable).args(Component.text(owner)).color(NamedTextColor.YELLOW);
                }
            } else if (!InteractionVisualizer.version.isLegacy()) {
                NoteBlock state = (NoteBlock) block.getBlockData();
                Tone tone = state.getNote().getTone();
                String inst = MusicManager.getMusicConfig().getString("Instruments." + state.getInstrument().toString().toUpperCase());
                String text = ChatColor.GOLD + inst + " " + getColor(tone) + tone.toString().toUpperCase();
                text = state.getNote().isSharped() ? text + "#" : text;
                text = state.getNote().getOctave() == 0 ? text : text + " ^";
                component = LegacyComponentSerializer.legacySection().deserialize(text);
            } else {
                org.bukkit.block.NoteBlock state = (org.bukkit.block.NoteBlock) block.getState();
                Tone tone = state.getNote().getTone();
                String inst = MusicManager.getMusicConfig().getString("Instruments." + LegacyInstrumentUtils.getInstrumentNameFromLegacy(block.getRelative(BlockFace.DOWN).getType().toString().toUpperCase()));
                String text = ChatColor.GOLD + inst + " " + getColor(tone) + tone.toString().toUpperCase();
                text = state.getNote().isSharped() ? text + "#" : text;
                text = state.getNote().getOctave() == 0 ? text : text + " ^";
                component = LegacyComponentSerializer.legacySection().deserialize(text);
            }

            stand.setCustomName(component);

            PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), stand);
            PacketManager.updateArmorStand(stand);
        }, 1);
    }

    public void setStand(ArmorStand stand) {
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setSilent(true);
        stand.setInvulnerable(true);
        stand.setVisible(false);
        stand.setCustomNameVisible(true);
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public Location getFaceOffset(Block block, BlockFace face) {
        Location location = block.getLocation().clone().add(0.5, 0.5, 0.5);
        switch (face) {
            case DOWN:
                return location.add(0.0, -0.8, 0.0);
            case EAST:
                return location.add(0.8, 0.0, 0.0);
            case NORTH:
                return location.add(0.0, 0.0, -0.8);
            case SOUTH:
                return location.add(0.0, 0.0, 0.8);
            case UP:
                return location.add(0.0, 0.8, 0.0);
            case WEST:
                return location.add(-0.8, 0.0, 0.0);
            default:
                return location.add(0.0, 0.8, 0.0);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public ChatColor getColor(Tone tone) {
        switch (tone) {
            case A:
                return ChatColor.RED;
            case B:
                return ChatColor.GOLD;
            case C:
                return ChatColor.YELLOW;
            case D:
                return ChatColor.GREEN;
            case E:
                return ChatColor.AQUA;
            case F:
                return ChatColor.BLUE;
            case G:
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.AQUA;
        }
    }

    public boolean isHead(Block block) {
        return block.getState() instanceof Skull;
    }

}
