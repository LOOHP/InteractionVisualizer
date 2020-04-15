package com.loohp.interactionvisualizer.EntityHolder;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Utils.EntityCreator;

public class ArmorStand {
	
	int id;
	UUID uuid;
	Location location;
	boolean hasArms;
	boolean hasBasePlate;
	boolean isMarker;
	boolean hasGravity;
	boolean isSmall;
	boolean isInvulnerable;
	boolean isVisible;
	boolean isSilent;
	EulerAngle rightArmPose;
	EulerAngle headPose;
	ItemStack helmet;
	ItemStack mainhand;
	String customName;
	boolean custonNameVisible;
	Vector velocity;
	boolean lock;
	
	public ArmorStand(Location location) {
		this.id = (int) (Math.random() * Integer.MAX_VALUE);
		this.uuid = UUID.randomUUID();
		this.location = location;
		this.hasArms = false;
		this.hasBasePlate = true;
		this.isMarker = false;
		this.hasGravity = true;
		this.isSmall = false;
		this.isInvulnerable = false;
		this.isVisible = true;
		this.isSilent = false;
		this.rightArmPose = new EulerAngle(0.0, 0.0, 0.0);
		this.headPose = new EulerAngle(0.0, 0.0, 0.0);
		this.helmet = new ItemStack(Material.AIR);
		this.mainhand = new ItemStack(Material.AIR);
		this.customName = "";
		this.custonNameVisible = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
		this.lock = false;
	}
	
	public void setRotation(float yaw, float pitch) {
		if (lock) {
			return;
		}
		teleport(location.getWorld(), location.getX(), location.getY(), location.getZ(), yaw, pitch);
	}
	
	public World getWorld() {
		return location.getWorld();
	}
	
	public void teleport(Location location) {
		setLocation(location);
	}
	
	public void teleport(World world, double x, double y, double z) {
		setLocation(new Location(world, x, y, z, location.getYaw(), location.getPitch()));
	}
	
	public void teleport(World world, double x, double y, double z, float yaw, float pitch) {
		setLocation(new Location(world, x, y, z, yaw, pitch));
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}
	public String getCustomName() {
		return customName;
	}
	
	public void setCustomNameVisible(boolean bool) {
		this.custonNameVisible = bool;
	}	
	public boolean isCustomNameVisible() {
		return custonNameVisible;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	public Location getLocation() {
		return location;
	}
	
	public void setArms(boolean bool) {
		this.hasArms = bool;
	}	
	public boolean hasArms() {
		return hasArms;
	}
	
	public void setLocked(boolean bool) {
		this.lock = bool;
	}	
	public boolean isLocked() {
		return lock;
	}

	public void setBasePlate(boolean bool) {
		this.hasBasePlate = bool;
	}	
	public boolean hasBasePlate() {
		return hasBasePlate;
	}

	public void setMarker(boolean bool) {
		this.isMarker = bool;
	}	
	public boolean isMarker() {
		return isMarker;
	}

	public void setGravity(boolean bool) {
		this.hasGravity = bool;
	}	
	public boolean hasGravity() {
		return hasGravity;
	}

	public void setSmall(boolean bool) {
		this.isSmall = bool;
	}	
	public boolean isSmall() {
		return isSmall;
	}

	public void setInvulnerable(boolean bool) {
		this.isInvulnerable = bool;
	}	
	public boolean isInvulnerable() {
		return isInvulnerable;
	}

	public void setVisible(boolean bool) {
		this.isVisible = bool;
	}	
	public boolean isVisible() {
		return isVisible;
	}

	public void setSilent(boolean bool) {
		this.isSilent = bool;
	}	
	public boolean isSilent() {
		return isSilent;
	}

	public void setRightArmPose(EulerAngle angle) {
		if (lock) {
			return;
		}
		this.rightArmPose = angle;
	}	
	public EulerAngle getRightArmPose() {
		return rightArmPose;
	}
	
	public void setHeadPose(EulerAngle angle) {
		if (lock) {
			return;
		}
		this.headPose = angle;
	}	
	public EulerAngle getHeadPose() {
		return headPose;
	}

	public void setHelmet(ItemStack item) {
		if (lock) {
			return;
		}
		this.helmet = item.clone();
	}	
	public ItemStack getHelmet() {
		return helmet;
	}

	public void setItemInMainHand(ItemStack item) {
		if (lock) {
			return;
		}
		this.mainhand = item.clone();
	}	
	public ItemStack getItemInMainHand() {
		return mainhand;
	}
	
	public void setVelocity(Vector vector) {
		this.velocity = vector.clone();
	}
	public Vector getVelocity() {
		return velocity;
	}
	
	public UUID getUniqueId() {
		return uuid;
	}
	
	public int getEntityId() {
		return id;
	}
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand) EntityCreator.create(new Location(location.getWorld(), 0, 0, 0), EntityType.ARMOR_STAND);
		stand.setArms(hasArms);
		stand.setBasePlate(hasBasePlate);
		stand.setMarker(isMarker);
		stand.setGravity(hasGravity);
		stand.setSmall(isSmall);
		stand.setInvulnerable(isInvulnerable);
		stand.setVisible(isVisible);
		stand.setSilent(isSilent);
		stand.setRightArmPose(rightArmPose);
		stand.setHeadPose(headPose);
		stand.getEquipment().setHelmet(helmet);
		stand.getEquipment().setItemInMainHand(mainhand);
		stand.setCustomName(customName);
		stand.setCustomNameVisible(custonNameVisible);
		stand.remove();
		return WrappedDataWatcher.getEntityWatcher(stand);
	}
	
	public void remove() {
		
	}

}
