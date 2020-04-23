package com.loohp.interactionvisualizer.EntityHolders;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.Protocol.WatchableCollection;

public class ArmorStand extends VisualizerEntity {
	
	boolean hasArms;
	boolean hasBasePlate;
	boolean isMarker;
	boolean hasGravity;
	boolean isSmall;
	boolean isInvulnerable;
	boolean isVisible;
	EulerAngle rightArmPose;
	EulerAngle headPose;
	ItemStack helmet;
	ItemStack mainhand;
	String customName;
	boolean custonNameVisible;
	Vector velocity;
	
	public ArmorStand(Location location, int id, UUID uuid) {
		super(location, id, uuid);
		this.hasArms = false;
		this.hasBasePlate = true;
		this.isMarker = false;
		this.hasGravity = true;
		this.isSmall = false;
		this.isInvulnerable = false;
		this.isVisible = true;
		this.rightArmPose = new EulerAngle(0.0, 0.0, 0.0);
		this.headPose = new EulerAngle(0.0, 0.0, 0.0);
		this.helmet = new ItemStack(Material.AIR);
		this.mainhand = new ItemStack(Material.AIR);
		this.customName = "";
		this.custonNameVisible = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
	}
	
	public ArmorStand(Location location) {
		super(location);
		this.hasArms = false;
		this.hasBasePlate = true;
		this.isMarker = false;
		this.hasGravity = true;
		this.isSmall = false;
		this.isInvulnerable = false;
		this.isVisible = true;
		this.rightArmPose = new EulerAngle(0.0, 0.0, 0.0);
		this.headPose = new EulerAngle(0.0, 0.0, 0.0);
		this.helmet = new ItemStack(Material.AIR);
		this.mainhand = new ItemStack(Material.AIR);
		this.customName = "";
		this.custonNameVisible = false;
		this.velocity = new Vector(0.0, 0.0, 0.0);
	}
	
	public ArmorStand deepClone() {
		ArmorStand newstand = new ArmorStand(location, id, uuid);
		newstand.setSilent(isSilent);
		newstand.setArms(hasArms);
		newstand.setBasePlate(hasBasePlate);
		newstand.setMarker(isMarker);
		newstand.setGravity(hasGravity);
		newstand.setSmall(isSmall);
		newstand.setInvulnerable(isInvulnerable);
		newstand.setVisible(isVisible);
		newstand.setRightArmPose(rightArmPose);
		newstand.setHeadPose(headPose);
		newstand.setHelmet(helmet);
		newstand.setItemInMainHand(mainhand);
		newstand.setCustomName(customName);
		newstand.setCustomNameVisible(custonNameVisible);
		newstand.setVelocity(velocity);
		newstand.setLocked(lock);
		return newstand;
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
	
	public void setArms(boolean bool) {
		this.hasArms = bool;
	}	
	public boolean hasArms() {
		return hasArms;
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
	
	public WrappedDataWatcher getWrappedDataWatcher() {
		return WatchableCollection.getWatchableCollection(this);
	}

}
