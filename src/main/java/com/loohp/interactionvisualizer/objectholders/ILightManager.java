package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.Location;

public interface ILightManager {
	
	static final ILightManager DUMMY_INSTANCE = new ILightManager() {	
		
		@Override
		public int run() {
			return -1;
		}
		
		@Override
		public void deleteLight(Location location) {
			//do nothing
		}
		
		@Override
		public void createLight(Location location, int lightlevel, LightType lightType) {
			//do nothing			
		}
	};
	
	public void createLight(Location location, int lightlevel, LightType lightType);
	
	public void deleteLight(Location location);
	
	public int run();

}
