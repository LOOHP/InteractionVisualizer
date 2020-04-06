package com.loohp.interactionvisualizer.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.Utils.PacketSending;

public class Database {
	
	public static boolean isMYSQL = false;
	
	public static java.sql.Connection connection;
	public static FileConfiguration config = InteractionVisualizer.config;
	public static String host, database, username, password;
	public static String table = "InteractionVisualizer_USER_PERFERENCES";
    public static int port;
	
	public static void setup() {
		String type = config.getString("Database.Type");
		if (type.equalsIgnoreCase("MYSQL")) {
			isMYSQL = true;
		} else {
			isMYSQL = false;
		}
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
	
	public static void open() {
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
			synchronized (Database.class) {
				if (getConnection() != null && !getConnection().isClosed()) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! [getConnection() != null && !getConnection().isClosed()]");
					return;
				}
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password));
				
				if (echo == true) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MYSQL CONNECTED");
				}
			}
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}
	
	public static void sqliteSetup(boolean echo) {	      
		try {
			synchronized (Database.class) {
		         Class.forName("org.sqlite.JDBC");
		         connection = DriverManager.getConnection("jdbc:sqlite:plugins/InteractionVisualizer/database.db");
		         if (echo) {
		        	 Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Opened Sqlite database successfully");
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
			}
	    } catch (Exception e) {
	    	Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to connect to sqlite database!!!");
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
    	open();
        try {
        	PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + table + " (UUID Text, NAME Text, ITEMSTAND BOOLEAN, ITEMDROP BOOLEAN, HOLOGRAM BOOLEAN)");

            statement.execute();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean playerExists(Player player) {
    	return playerExists(player.getUniqueId());
    }
    
	public static boolean playerExists(UUID uuid) {
		open();
		try {
			PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());

			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return true;
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void createPlayer(Player player) {
		open();
		try {
			PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + table + " (UUID,NAME,ITEMSTAND,ITEMDROP,HOLOGRAM) VALUES (?,?,?,?,?)");
			insert.setString(1, player.getUniqueId().toString());
			insert.setString(2, player.getName());
			insert.setBoolean(3, true);
			insert.setBoolean(4, true);
			insert.setBoolean(5, true);
			insert.executeUpdate();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean toggleItemStand(Player player) {
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
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		PacketSending.removeAll(player);
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractionVisualizer.plugin, () -> PacketSending.sendPlayerPackets(player), 10);
		return newvalue;
	}
	
	public static boolean toggleItemDrop(Player player) {
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
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		PacketSending.removeAll(player);
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractionVisualizer.plugin, () -> PacketSending.sendPlayerPackets(player), 10);
		return newvalue;
	}
	
	public static boolean toggleHologram(Player player) {
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
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		PacketSending.removeAll(player);
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractionVisualizer.plugin, () -> PacketSending.sendPlayerPackets(player), 10);
		return newvalue;
	}
	
	public static void loadPlayer(Player player) {
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
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		PacketSending.removeAll(player);
		Bukkit.getScheduler().runTaskLaterAsynchronously(InteractionVisualizer.plugin, () -> PacketSending.sendPlayerPackets(player), 10);
	}

}
