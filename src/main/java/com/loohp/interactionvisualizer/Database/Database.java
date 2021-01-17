package com.loohp.interactionvisualizer.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.API.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.Managers.PacketManager;

public class Database {
	
	public static boolean isMYSQL = false;
	
	private static Connection connection;
	private static FileConfiguration config = InteractionVisualizer.config;
	private static String host, database, username, password;
	private static String table = "InteractionVisualizer_USER_PERFERENCES";
	private static int port;
    
    private static Object syncdb = new Object();
	
	public static void setup() {
		String type = config.getString("Database.Type");
		if (type.equalsIgnoreCase("MYSQL")) {
			isMYSQL = true;
		} else {
			isMYSQL = false;
		}
		synchronized (syncdb) {
			if (isMYSQL) {
				mysqlSetup(true);
				createTable();
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				sqliteSetup(true);
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void open() {
		if (isMYSQL) {
			mysqlSetup(false);
		} else {
			sqliteSetup(false);
		}
	}
	
	public static void mysqlSetup(boolean echo) {
        host = config.getString("Database.MYSQL.Host");
        port =  config.getInt("Database.MYSQL.Port");
        database = config.getString("Database.MYSQL.Database");
        username = config.getString("Database.MYSQL.Username");
        password = config.getString("Database.MYSQL.Password");

        try {
			if (getConnection() != null && !getConnection().isClosed()) {
				return;
			}
			
			Class.forName("com.mysql.jdbc.Driver");
			setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
			
			if (echo == true) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] MYSQL CONNECTED");
			}
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}
	
	public static void sqliteSetup(boolean echo) {	   
		try {
			Class.forName("org.sqlite.JDBC");
	        connection = DriverManager.getConnection("jdbc:sqlite:plugins/InteractionVisualizer/database.db");
	        if (echo) {
	        	Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] Opened Sqlite database successfully");
	        }

	        Statement stmt = connection.createStatement();
	        String sql = "CREATE TABLE IF NOT EXISTS " + table + " " +
	                      "(UUID TEXT PRIMARY KEY, " +
	                       "NAME TEXT NOT NULL, " + 
	                       "ITEMSTAND BOOLEAN, " + 
	                       "ITEMDROP BOOLEAN, " + 
	                       "HOLOGRAM BOOLEAN);"; 
	        stmt.executeUpdate(sql);
	        stmt.close(); 
	    } catch (Exception e) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[InteractionVisualizer] Unable to connect to sqlite database!!!");
	    	e.printStackTrace();
	    	InteractionVisualizer.plugin.getPluginLoader().disablePlugin(InteractionVisualizer.plugin);
	    }
	}

	public static Connection getConnection() {
		return connection;
	}

	public static void setConnection(Connection connection) {
		Database.connection = connection;
	}
	
    public static void createTable() {
    	synchronized (syncdb) {
	    	open();
	        try {
	        	PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (UUID Text, NAME Text, ITEMSTAND BOOLEAN, ITEMDROP BOOLEAN, HOLOGRAM BOOLEAN)");
	
	            statement.execute();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public static boolean playerExists(Player player) {
    	return playerExists(player.getUniqueId());
    }
    
	public static boolean playerExists(UUID uuid) {
		synchronized (syncdb) {
			boolean exist = false;
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
	
				ResultSet results = statement.executeQuery();
				if (results.next()) {
					exist = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return exist;
		}
	}

	public static void createPlayer(Player player) {
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + table + " (UUID,NAME,ITEMSTAND,ITEMDROP,HOLOGRAM) VALUES (?,?,?,?,?)");
				insert.setString(1, player.getUniqueId().toString());
				insert.setString(2, player.getName());
				insert.setBoolean(3, true);
				insert.setBoolean(4, true);
				insert.setBoolean(5, true);
				insert.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean toggleItemStand(Player player) {
		synchronized (syncdb) {
			open();
			boolean newvalue = true;
			if (InteractionVisualizer.itemStand.contains(player)) {
				newvalue = false;
				InteractionVisualizer.itemStand.remove(player);
			} else {
				InteractionVisualizer.itemStand.add(player);
			}
			try {
				PreparedStatement statement = getConnection().prepareStatement("UPDATE " + table + " SET ITEMSTAND=? WHERE UUID=?");
				statement.setBoolean(1, newvalue);
				statement.setString(2, player.getUniqueId().toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
			return newvalue;
		}
	}
	
	public static boolean toggleItemDrop(Player player) {
		synchronized (syncdb) {
			open();
			boolean newvalue = true;
			if (InteractionVisualizer.itemDrop.contains(player)) {
				newvalue = false;
				InteractionVisualizer.itemDrop.remove(player);
			} else {
				InteractionVisualizer.itemDrop.add(player);
			}
			try {
				PreparedStatement statement = getConnection().prepareStatement("UPDATE " + table + " SET ITEMDROP=? WHERE UUID=?");
				statement.setBoolean(1, newvalue);
				statement.setString(2, player.getUniqueId().toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
			return newvalue;
		}
	}
	
	public static boolean toggleHologram(Player player) {
		synchronized (syncdb) {
			open();
			boolean newvalue = true;
			if (InteractionVisualizer.holograms.contains(player)) {
				newvalue = false;
				InteractionVisualizer.holograms.remove(player);
			} else {
				InteractionVisualizer.holograms.add(player);
			}
			try {
				PreparedStatement statement = getConnection().prepareStatement("UPDATE " + table + " SET HOLOGRAM=? WHERE UUID=?");
				statement.setBoolean(1, newvalue);
				statement.setString(2, player.getUniqueId().toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
			return newvalue;
		}
	}
	
	public static HashMap<Modules, Boolean> getPlayerInfo(UUID uuid) {
		HashMap<Modules, Boolean> map = new HashMap<Modules, Boolean>();
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
				ResultSet results = statement.executeQuery();
				results.next();
				
				map.put(Modules.ITEMSTAND, results.getBoolean("ITEMSTAND"));
				map.put(Modules.ITEMDROP, results.getBoolean("ITEMDROP"));
				map.put(Modules.HOLOGRAM, results.getBoolean("HOLOGRAM"));				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	
	public static HashMap<Modules, Boolean> getPlayerInfo(Player player) {
		return getPlayerInfo(player.getUniqueId());
	}
	
	public static void loadPlayer(Player player, boolean justJoined) {
		synchronized (syncdb) {
			open();
			try {
				PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
				statement.setString(1, player.getUniqueId().toString());
				ResultSet results = statement.executeQuery();
				results.next();
				
				boolean itemstand = results.getBoolean("ITEMSTAND");
				boolean itemdrop = results.getBoolean("ITEMDROP");
				boolean hologram = results.getBoolean("HOLOGRAM");
				
				if (InteractionVisualizer.itemStandEnabled) {
					if (itemstand) {
						if (!InteractionVisualizer.itemStand.contains(player)) {
							InteractionVisualizer.itemStand.add(player);
						}
					} else {
						InteractionVisualizer.itemStand.remove(player);
					}
				}
				if (InteractionVisualizer.itemDropEnabled) {
					if (itemdrop) {
						if (!InteractionVisualizer.itemDrop.contains(player)) {
							InteractionVisualizer.itemDrop.add(player);
						}
					} else {
						InteractionVisualizer.itemDrop.remove(player);
					}
				}
				if (InteractionVisualizer.hologramsEnabled) {
					if (hologram) {
						if (!InteractionVisualizer.holograms.contains(player)) {
							InteractionVisualizer.holograms.add(player);
						}
					} else {
						InteractionVisualizer.holograms.remove(player);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (justJoined) {
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.sendPlayerPackets(player));
		} else {
			Bukkit.getScheduler().runTask(InteractionVisualizer.plugin, () -> PacketManager.reset(player));
		}
	}

}
