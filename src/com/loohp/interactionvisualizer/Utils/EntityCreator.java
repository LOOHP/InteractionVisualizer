package com.loohp.interactionvisualizer.Utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class EntityCreator {
	
	private static Class<?> craftWorldClass;
	private static Class<?> craftEntityClass;
	private static Class<?> craftItemStackClass;
	private static Class<?> nmsEntityClass;
	private static Class<?> nmsWorldServerClass;
	private static Class<?> nmsEntityItemClass;
	private static Class<?> nmsWorldClass;
	private static Class<?> nmsItemStackClass;
	private static MethodHandle nmsEntityItemConstructor;
	private static Class<?> nmsEntityItemFrameClass;
	private static Class<?> nmsBlockPositionClass;
	private static MethodHandle nmsBlockPostionConstructor;
	private static Class<?> nmsEnumDirectionClass;
	private static MethodHandle nmsEntityItemFrameConstructor;
	private static MethodHandle createEntityMethod;
	private static MethodHandle craftWorldGetHandleMethod;
	private static MethodHandle nmsEntityGetBukkitEntityMethod;
	private static ItemStack dummyitem;
	
	public static void setup() {
		try {
			craftWorldClass = getNMSClass("org.bukkit.craftbukkit.", "CraftWorld");
			craftEntityClass = getNMSClass("org.bukkit.craftbukkit.", "entity.CraftEntity");
			craftItemStackClass = getNMSClass("org.bukkit.craftbukkit.", "inventory.CraftItemStack");
			nmsEntityClass = getNMSClass("net.minecraft.server.", "Entity");
			nmsWorldServerClass = getNMSClass("net.minecraft.server.", "WorldServer");
			nmsEntityItemClass = getNMSClass("net.minecraft.server.", "EntityItem");
			nmsWorldClass = getNMSClass("net.minecraft.server.", "World");
			nmsItemStackClass = getNMSClass("net.minecraft.server.", "ItemStack");
			nmsEntityItemConstructor = MethodHandles.lookup().findConstructor(nmsEntityItemClass, MethodType.methodType(void.class, nmsWorldClass, double.class, double.class, double.class, nmsItemStackClass));
			nmsEntityItemFrameClass = getNMSClass("net.minecraft.server.", "EntityItemFrame");
			nmsBlockPositionClass = getNMSClass("net.minecraft.server.", "BlockPosition");
			nmsBlockPostionConstructor = MethodHandles.lookup().findConstructor(nmsBlockPositionClass, MethodType.methodType(void.class, int.class, int.class, int.class));
			nmsEnumDirectionClass = getNMSClass("net.minecraft.server.", "EnumDirection");
			nmsEntityItemFrameConstructor = MethodHandles.lookup().findConstructor(nmsEntityItemFrameClass, MethodType.methodType(void.class, nmsWorldClass, nmsBlockPositionClass, nmsEnumDirectionClass));
			createEntityMethod = MethodHandles.lookup().findVirtual(craftWorldClass, "createEntity", MethodType.methodType(nmsEntityClass, Location.class, Class.class));
			craftWorldGetHandleMethod = MethodHandles.lookup().findVirtual(craftWorldClass, "getHandle", MethodType.methodType(nmsWorldServerClass));
			nmsEntityGetBukkitEntityMethod = MethodHandles.lookup().findVirtual(nmsEntityClass, "getBukkitEntity", MethodType.methodType(craftEntityClass));
			dummyitem = new ItemStack(Material.STONE);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static Entity create(Location location, EntityType entityType) {
		Entity entity = createRaw(location, entityType);
		entity.addScoreboardTag("isInteractionVisualizer");
		return entity;
	}

    public static Entity createRaw(Location location, EntityType entityType) throws SecurityException {
    	try {
        	if (entityType.equals(EntityType.DROPPED_ITEM)) {       		       		
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());                 
                
                Object entity = nmsEntityItemConstructor.invoke(craftWorldGetHandleMethod.invoke(craftWorldObject), location.getX(), location.getY(), location.getZ(), craftItemStackClass.getMethod("asNMSCopy", ItemStack.class).invoke(dummyitem, dummyitem));
                
                return (Entity) nmsEntityGetBukkitEntityMethod.invoke(entity);
        	} else if (entityType.equals(EntityType.ITEM_FRAME)) {
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());         
                
                Object nmsBlockPostion = nmsBlockPostionConstructor.invoke(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                
                Object entity = nmsEntityItemFrameConstructor.invoke(craftWorldGetHandleMethod.invoke(craftWorldObject), nmsBlockPostion, nmsEnumDirectionClass.getEnumConstants()[0]);
                
                return (Entity) nmsEntityGetBukkitEntityMethod.invoke(entity);
        	} else {
	            Object craftWorldObject = craftWorldClass.cast(location.getWorld());

	            Object entity = createEntityMethod.invoke(craftWorldObject, location, entityType.getEntityClass());
	            
	            return (Entity) nmsEntityGetBukkitEntityMethod.invoke(entity);
        	}
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Class<?> getNMSClass(String prefix, String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = prefix + version + nmsClassString;
        return Class.forName(name);
    }
}
 