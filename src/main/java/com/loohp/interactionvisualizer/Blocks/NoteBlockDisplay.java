package com.loohp.interactionvisualizer.Blocks;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note.Tone;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.API.VisualizerRunnableDisplay;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.Managers.MusicManager;
import com.loohp.interactionvisualizer.Managers.PacketManager;
import com.loohp.interactionvisualizer.Utils.LegacyInstrumentUtils;

import net.md_5.bungee.api.ChatColor;

public class NoteBlockDisplay extends VisualizerRunnableDisplay implements Listener {
	
	public ConcurrentHashMap<Block, ConcurrentHashMap<String, Object>> displayingNotes = new ConcurrentHashMap<Block, ConcurrentHashMap<String, Object>>();
	
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
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onUseNoteBlock(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null) {
			return;
		}
		if (!block.getType().equals(Material.NOTE_BLOCK)) {
			return;
		}
		boolean holdingAir = event.getPlayer().getEquipment().getItemInMainHand() != null ? (event.getPlayer().getEquipment().getItemInMainHand().getType().equals(Material.AIR) ? true : false) : true;
		if (event.getPlayer().isSneaking() && !holdingAir) {
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
			
			String text = "";
			if (!InteractionVisualizer.version.isLegacy()) {
				NoteBlock state = (NoteBlock) block.getBlockData();
				Tone tone = state.getNote().getTone();
				String inst = MusicManager.getMusicConfig().getString("Instruments." + state.getInstrument().toString().toUpperCase());
				text = ChatColor.GOLD + inst + " " + getColor(tone) + tone.toString().toUpperCase();
				text = state.getNote().isSharped() ? text + "#" : text;
				text = state.getNote().getOctave() == 0 ? text : text + " ^";
			} else {
				org.bukkit.block.NoteBlock state = (org.bukkit.block.NoteBlock) block.getState();
				Tone tone = state.getNote().getTone();
				String inst = MusicManager.getMusicConfig().getString("Instruments." + LegacyInstrumentUtils.getInstrumentNameFromLegacy(block.getRelative(BlockFace.DOWN).getType().toString().toUpperCase()));
				text = ChatColor.GOLD + inst + " " + getColor(tone) + tone.toString().toUpperCase();
				text = state.getNote().isSharped() ? text + "#" : text;
				text = state.getNote().getOctave() == 0 ? text : text + " ^";
			}
			
			stand.setCustomName(text);
			
			PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM), stand);
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

}
