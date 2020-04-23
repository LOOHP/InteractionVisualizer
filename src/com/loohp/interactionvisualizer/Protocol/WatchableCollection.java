package com.loohp.interactionvisualizer.Protocol;

import java.util.Optional;

import com.comphenix.protocol.wrappers.Vector3F;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.EntityHolders.ArmorStand;
import com.loohp.interactionvisualizer.EntityHolders.Item;
import com.loohp.interactionvisualizer.EntityHolders.ItemFrame;

public class WatchableCollection {
	
	private static String version = InteractionVisualizer.version;
	private static int metaversion = InteractionVisualizer.metaversion;
	private static Serializer booleanSerializer;
	private static Serializer stringSerializer;
	private static Serializer byteSerializer;
	private static Serializer intSerializer;
	private static Serializer itemSerializer;
	private static Serializer optChatSerializer;
	private static Serializer vectorSerializer;
	
	public static void setup() {
		booleanSerializer = Registry.get(Boolean.class);
		stringSerializer = Registry.get(String.class);
		byteSerializer = Registry.get(Byte.class);
		intSerializer = Registry.get(Integer.class);
		itemSerializer = Registry.getItemStackSerializer(false);
		if (!version.contains("legacy")) {
			optChatSerializer = Registry.getChatComponentSerializer(true);
		}
		vectorSerializer = Registry.getVectorSerializer();
	}
	
	public static WrappedDataWatcher getWatchableCollection(ArmorStand stand) {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		
		byte bitmask = (byte) 0;
		bitmask = !stand.isVisible() ? (byte) (bitmask | 0x20) : bitmask;
		watcher.setObject(new WrappedDataWatcherObject(0, byteSerializer), bitmask);
		
		switch (metaversion) {
		case 0:
			if (stand.getCustomName() != null && !stand.getCustomName().equals("")) {
				watcher.setObject(new WrappedDataWatcherObject(2, stringSerializer), stand.getCustomName());
			} else {
				watcher.setObject(new WrappedDataWatcherObject(2, stringSerializer), "");
			}
			break;
		case 1:
		case 2:
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(2, optChatSerializer), Optional.of(WrappedChatComponent.fromChatMessage(stand.getCustomName())[0].getHandle()));
			break;
		}
		
		watcher.setObject(new WrappedDataWatcherObject(3, booleanSerializer), stand.isCustomNameVisible());
		watcher.setObject(new WrappedDataWatcherObject(4, booleanSerializer), stand.isSilent());
		watcher.setObject(new WrappedDataWatcherObject(5, booleanSerializer), !stand.hasGravity());
		
		byte standbitmask = (byte) 0;
		standbitmask = stand.isSmall() ? (byte) (standbitmask | 0x01) : standbitmask;
		standbitmask = stand.hasArms() ? (byte) (standbitmask | 0x04) : standbitmask;
		standbitmask = !stand.hasBasePlate() ? (byte) (standbitmask | 0x08) : standbitmask;
		standbitmask = stand.isMarker() ? (byte) (standbitmask | 0x10) : standbitmask;
		
		switch (metaversion) {
		case 0:
		case 1:
			watcher.setObject(new WrappedDataWatcherObject(11, byteSerializer), standbitmask);
			break;
		case 2:
			watcher.setObject(new WrappedDataWatcherObject(13, byteSerializer), standbitmask);
			break;
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(14, byteSerializer), standbitmask);
			break;
		}

		Vector3F headrotation = new Vector3F();
		headrotation.setX((float) Math.toDegrees(stand.getHeadPose().getX()));
		headrotation.setY((float) Math.toDegrees(stand.getHeadPose().getY()));
		headrotation.setZ((float) Math.toDegrees(stand.getHeadPose().getZ()));
		
		switch (metaversion) {
		case 0:
		case 1:
			watcher.setObject(new WrappedDataWatcherObject(12, vectorSerializer), headrotation);
			break;
		case 2:
			watcher.setObject(new WrappedDataWatcherObject(14, vectorSerializer), headrotation);
			break;
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(15, vectorSerializer), headrotation);
			break;
		}
		
		Vector3F rightarmrotation = new Vector3F();
		rightarmrotation.setX((float) Math.toDegrees(stand.getRightArmPose().getX()));
		rightarmrotation.setY((float) Math.toDegrees(stand.getRightArmPose().getY()));
		rightarmrotation.setZ((float) Math.toDegrees(stand.getRightArmPose().getZ()));
		
		switch (metaversion) {
		case 0:
		case 1:
			watcher.setObject(new WrappedDataWatcherObject(15, vectorSerializer), rightarmrotation);
			break;
		case 2:
			watcher.setObject(new WrappedDataWatcherObject(17, vectorSerializer), rightarmrotation);
			break;
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(18, vectorSerializer), rightarmrotation);
			break;
		}
		
		return watcher;
	}
	
	public static WrappedDataWatcher getWatchableCollection(Item item) {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
			
		byte bitmask = (byte) 0;
		bitmask = item.isGlowing() ? (byte) (bitmask | 0x40) : bitmask;
		watcher.setObject(new WrappedDataWatcherObject(0, byteSerializer), bitmask);
		
		switch (metaversion) {
		case 0:
			if (item.getCustomName() != null && !item.getCustomName().equals("")) {
				watcher.setObject(new WrappedDataWatcherObject(2, stringSerializer), item.getCustomName());
			} else {
				watcher.setObject(new WrappedDataWatcherObject(2, stringSerializer), "");
			}
			break;
		case 1:
		case 2:
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(2, optChatSerializer), Optional.of(WrappedChatComponent.fromChatMessage(item.getCustomName())[0].getHandle()));
			break;
		}
		
			watcher.setObject(new WrappedDataWatcherObject(3, booleanSerializer), item.isCustomNameVisible());

		watcher.setObject(new WrappedDataWatcherObject(5, booleanSerializer), !item.hasGravity());
		
		switch (metaversion) {
		case 0:
		case 1:
			watcher.setObject(new WrappedDataWatcherObject(6, itemSerializer), item.getItemStack());
			break;
		case 2:
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(7, itemSerializer), item.getItemStack());
			break;
		}
		
		return watcher;
	}
	
	public static WrappedDataWatcher getWatchableCollection(ItemFrame frame) {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(new WrappedDataWatcherObject(4, booleanSerializer), frame.isSilent());
		switch (metaversion) {
		case 0:
		case 1:
			watcher.setObject(new WrappedDataWatcherObject(6, itemSerializer), frame.getItem());
			watcher.setObject(new WrappedDataWatcherObject(7, intSerializer), frame.getFrameRotation());
			break;
		case 2:
		case 3:
			watcher.setObject(new WrappedDataWatcherObject(7, itemSerializer), frame.getItem());
			watcher.setObject(new WrappedDataWatcherObject(8, intSerializer), frame.getFrameRotation());
			break;
		}
		return watcher;
	}

}
