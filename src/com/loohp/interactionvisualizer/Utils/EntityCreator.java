package com.loohp.interactionvisualizer.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class EntityCreator {
	
	public static Entity create(Location location, EntityType entityType) {
		Entity entity = createRaw(location, entityType);
		entity.addScoreboardTag("isInteractionVisualizer");
		return entity;
	}

    public static Entity createRaw(Location location, EntityType entityType) {
    	try {
        	if (!entityType.equals(EntityType.DROPPED_ITEM) && !entityType.equals(EntityType.ITEM_FRAME)) {
	            // We get the craftworld class with nms so it can be used in multiple versions
	            Class<?> craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
	
	            // Cast the bukkit world to the craftworld
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());
	
	            // Create variable with the method that creates the entity
	            // https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/CraftWorld.java#896
	            Method createEntityMethod = craftWorldObject.getClass().getMethod("createEntity", Location.class, Class.class);
	
	            // Attempt to invoke the method that creates the entity itself. This returns a net.minecraft.server entity
	            Object entity = createEntityMethod.invoke(craftWorldObject, location, entityType.getEntityClass());
	
	            // finally we run the getBukkitEntity method in the entity class to get a usable object
	            return (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
            
        	} else if (entityType.equals(EntityType.DROPPED_ITEM)) {       		
        		Class<?> craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
        		
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());
	            
	            Class<?> craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
	            
                Class<?> nmsEntityItemClass = getNMSClass("net.minecraft.server.", "EntityItem");           
                
                Class<?> nmsWorldClass = getNMSClass("net.minecraft.server.", "World");           
                
                Class<?> nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");           
                
                Constructor<?> nmsEntityItemConstructor = nmsEntityItemClass.getConstructor(nmsWorldClass, double.class, double.class, double.class, nmsItemStackClass);

                ItemStack dummyitem = new ItemStack(Material.STONE);
                
                Object entity = nmsEntityItemConstructor.newInstance(craftWorldObject.getClass().getMethod("getHandle").invoke(craftWorldObject), location.getX(), location.getY(), location.getZ(), craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(dummyitem, dummyitem));
                
                return (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        	} else if (entityType.equals(EntityType.ITEM_FRAME)) {
        		Class<?> craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
        		
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());
	            
                Class<?> nmsEntityItemFrameClass = getNMSClass("net.minecraft.server.", "EntityItemFrame");           
                
                Class<?> nmsWorldClass = getNMSClass("net.minecraft.server.", "World");
                
                Class<?> nmsBlockPositionClass = getNMSClass("net.minecraft.server.", "BlockPosition");
                
                Constructor<?> nmsBlockPostionConstructor = nmsBlockPositionClass.getConstructor(int.class, int.class, int.class);
                
                Object nmsBlockPostion = nmsBlockPostionConstructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                
                Class<?> nmsEnumDirectionClass = getNMSClass("net.minecraft.server.", "EnumDirection");
                
                Constructor<?> nmsEntityItemFrameConstructor = nmsEntityItemFrameClass.getConstructor(nmsWorldClass, nmsBlockPositionClass, nmsEnumDirectionClass);
                
                Object entity = nmsEntityItemFrameConstructor.newInstance(craftWorldObject.getClass().getMethod("getHandle").invoke(craftWorldObject), nmsBlockPostion, nmsEnumDirectionClass.getEnumConstants()[0]);
                
                return (Entity) entity.getClass().getMethod("getBukkitEntity").invoke(entity);
        	}
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException exception) {
            exception.printStackTrace();
        }

        // If something went wrong we just return null
        return null;
    }

    private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        // Getting the version by splitting the package
       String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

        // Combining the prefix + version + nmsClassString for the full class path
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
}
 