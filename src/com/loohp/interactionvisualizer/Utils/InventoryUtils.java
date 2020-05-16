package com.loohp.interactionvisualizer.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class InventoryUtils {
	
	public static boolean stillHaveSpace(Inventory inv, Material material) {
		int size = inv.getSize();
		if (inv instanceof PlayerInventory) {
			size = 36;
		}
		for (int i = 0; i < size; i++) {
			ItemStack item = inv.getItem(i);
			if (item == null) {
				return true;
			}
			if (item.getType().equals(Material.AIR)) {
				return true;
			}
			if (item.getType().equals(material)) {
				if (item.getAmount() < item.getType().getMaxStackSize()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String toBase64(Inventory inventory) throws IllegalStateException {
	    try {
	    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
	            
	        // Write the size of the inventory
	        dataOutput.writeInt(inventory.getSize());
	            
	        // Save every element in the list
	        for (int i = 0; i < inventory.getSize(); i++) {
	            dataOutput.writeObject(inventory.getItem(i));
	        }
	            
	        // Serialize that array
	        dataOutput.close();
	        return Base64Coder.encodeLines(outputStream.toByteArray());
	    } catch (Exception e) {
	        throw new IllegalStateException("Unable to save item stacks.", e);
        }
	}
	    
	public static Inventory fromBase64(String data, String title) throws IOException {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
	        Inventory inventory = null;
	        if (title.equals("")) {
	       	 	inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
	        } else {
	       	 	inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), title);
	        }
	   
	        // Read the serialized inventory
	        for (int i = 0; i < inventory.getSize(); i++) {
	            inventory.setItem(i, (ItemStack) dataInput.readObject());
	        }
	            
	        dataInput.close();
	        return inventory;
	    } catch (ClassNotFoundException e) {
	        throw new IOException("Unable to decode class type.", e);
	    }
	}

}
