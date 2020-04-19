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
import com.loohp.interactionvisualizer.Holder.ArmorStand;
import com.loohp.interactionvisualizer.Manager.MusicManager;
import com.loohp.interactionvisualizer.Manager.PacketManager;

import net.md_5.bungee.api.ChatColor;

public class NoteBlockDisplay implements Listener {
	
	private static ConcurrentHashMap<Block, ConcurrentHashMap<String, Object>> displayingNotes = new ConcurrentHashMap<Block, ConcurrentHashMap<String, Object>>();
	
	@EventHandler(priority=EventPriority.MONITOR)
	public static void onUseNoteBlock(PlayerInteractEvent event) {
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
			
			NoteBlock state = (NoteBlock) block.getBlockData();
			Tone tone = state.getNote().getTone();
			String inst = MusicManager.getMusicConfig().getString("Instruments." + state.getInstrument().toString().toUpperCase());
			String text = ChatColor.GOLD + inst + " " + getColor(tone) + tone.toString().toUpperCase();
			text = state.getNote().isSharped() ? text + "#" : text;
			text = state.getNote().getOctave() == 0 ? text : text + " ^";
			
			stand.setCustomName(text);
			
			PacketManager.sendArmorStandSpawn(InteractionVisualizer.holograms, stand);
			PacketManager.updateArmorStand(InteractionVisualizer.getOnlinePlayers(), stand);
		}, 1);
	}
	
	public static int run() {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(InteractionVisualizer.plugin, () -> {
			Iterator<Entry<Block, ConcurrentHashMap<String, Object>>> itr = displayingNotes.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<Block, ConcurrentHashMap<String, Object>> entry = itr.next();
				long unix = System.currentTimeMillis();
				long timeout = (long) entry.getValue().get("Timeout");
				if (unix > timeout) {
					ArmorStand stand = (ArmorStand) entry.getValue().get("Stand");
					Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.removeArmorStand(InteractionVisualizer.getOnlinePlayers(), stand));
					itr.remove();
				}
			}
		}, 0, 20).getTaskId();
	}
	
	public static void setStand(ArmorStand stand) {
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
	
	public static Location getFaceOffset(Block block, BlockFace face) {
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
	
	public static ChatColor getColor(Tone tone) {
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
