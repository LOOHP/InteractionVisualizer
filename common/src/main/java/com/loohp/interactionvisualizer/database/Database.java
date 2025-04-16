/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.database;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import com.loohp.interactionvisualizer.utils.ArrayUtils;
import com.loohp.interactionvisualizer.utils.BitSetUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

public class Database {

    public static final String EMPTY_BITSET = "0";
    public static final Pattern VALID_BITSET = Pattern.compile("^[0-9]*$");
    private static final String preferenceTable = "USER_PERFERENCES";
    private static final String indexMappingTable = "INDEX_MAPPING";
    private static final String statusLockTable = "USE_LOCK";
    private static final Object syncdb = new Object();
    public static boolean isMYSQL = false;
    private static Connection connection;
    private static String host, database, username, password;
    private static int port;

    public static void setup() {
        String type = InteractionVisualizer.plugin.getConfiguration().getString("Database.Type");
        isMYSQL = type.equalsIgnoreCase("MYSQL");
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
        host = InteractionVisualizer.plugin.getConfiguration().getString("Database.MYSQL.Host");
        port = InteractionVisualizer.plugin.getConfiguration().getInt("Database.MYSQL.Port");
        database = InteractionVisualizer.plugin.getConfiguration().getString("Database.MYSQL.Database");
        username = InteractionVisualizer.plugin.getConfiguration().getString("Database.MYSQL.Username");
        password = InteractionVisualizer.plugin.getConfiguration().getString("Database.MYSQL.Password");

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

    private static void sqliteSetup(boolean echo) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:plugins/InteractionVisualizer/database.db");
            if (echo) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[InteractionVisualizer] Opened Sqlite database successfully");
            }

            Statement stmt0 = connection.createStatement();
            String sql0 = "CREATE TABLE IF NOT EXISTS " + preferenceTable + " (UUID TEXT PRIMARY KEY, NAME TEXT NOT NULL, ITEMSTAND TEXT, ITEMDROP TEXT, HOLOGRAM TEXT);";
            stmt0.executeUpdate(sql0);
            stmt0.close();

            Statement stmt1 = connection.createStatement();
            String sql1 = "CREATE TABLE IF NOT EXISTS " + indexMappingTable + " (ENTRY TEXT PRIMARY KEY, BITMASK_INDEX INTEGER);";
            stmt1.executeUpdate(sql1);
            stmt1.close();

            Statement stmt2 = connection.createStatement();
            String sql2 = "CREATE TABLE IF NOT EXISTS " + statusLockTable + " (STATUS_LOCK BOOLEAN PRIMARY KEY);";
            stmt2.executeUpdate(sql2);
            stmt2.close();
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

    private static void createTable() {
        open();
        try {
            PreparedStatement statement0 = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + preferenceTable + " (UUID Text, NAME Text, ITEMSTAND Text, ITEMDROP Text, HOLOGRAM Text)");
            statement0.execute();

            PreparedStatement statement1 = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + indexMappingTable + " (ENTRY Text, BITMASK_INDEX INT)");
            statement1.execute();

            PreparedStatement statement2 = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + statusLockTable + " (STATUS_LOCK BOOLEAN)");
            statement2.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLocked() {
        synchronized (syncdb) {
            boolean value = false;
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + statusLockTable);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    value = results.getBoolean("STATUS_LOCK");
                } else {
                    value = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return value;
        }
    }

    public static void setLocked(boolean value) {
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + statusLockTable);
                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    PreparedStatement statement1 = getConnection().prepareStatement("UPDATE " + statusLockTable + " SET STATUS_LOCK=?");
                    statement1.setBoolean(1, value);
                    statement1.executeUpdate();
                } else {
                    PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + statusLockTable + " (STATUS_LOCK) VALUES (?)");
                    insert.setBoolean(1, value);
                    insert.executeUpdate();
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
    }

    public static Map<Integer, EntryKey> getBitIndex() {
        Map<Integer, EntryKey> index = new HashMap<>();
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + indexMappingTable);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    index.put(results.getInt("BITMASK_INDEX"), new EntryKey(results.getString("ENTRY")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return index;
        }
    }

    public static void setBitIndex(Map<Integer, EntryKey> index) {
        synchronized (syncdb) {
            open();
            try {
                for (Entry<Integer, EntryKey> entry : index.entrySet()) {
                    PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + indexMappingTable + " WHERE ENTRY=?");
                    statement.setString(1, entry.getValue().toString());
                    ResultSet results = statement.executeQuery();
                    if (results.next()) {
                        PreparedStatement statement1 = getConnection().prepareStatement("UPDATE " + indexMappingTable + " SET BITMASK_INDEX=? WHERE ENTRY=?");
                        statement1.setInt(1, entry.getKey());
                        statement1.setString(2, entry.getValue().toString());
                        statement1.executeUpdate();
                    } else {
                        PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + indexMappingTable + " (BITMASK_INDEX,ENTRY) VALUES (?,?)");
                        insert.setInt(1, entry.getKey());
                        insert.setString(2, entry.getValue().toString());
                        insert.executeUpdate();
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
    }

    public static boolean playerExists(UUID uuid) {
        synchronized (syncdb) {
            boolean exist = false;
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + preferenceTable + " WHERE UUID=?");
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

    public static void createPlayer(UUID uuid, String name) {
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement insert = getConnection().prepareStatement("INSERT INTO " + preferenceTable + " (UUID,NAME,ITEMSTAND,ITEMDROP,HOLOGRAM) VALUES (?,?,?,?,?)");
                insert.setString(1, uuid.toString());
                insert.setString(2, name);
                insert.setString(3, EMPTY_BITSET);
                insert.setString(4, EMPTY_BITSET);
                insert.setString(5, EMPTY_BITSET);
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

    public static void setItemStand(UUID uuid, BitSet bitset) {
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("UPDATE " + preferenceTable + " SET ITEMSTAND=? WHERE UUID=?");
                statement.setString(1, BitSetUtils.toNumberString(bitset));
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
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

    public static void setItemDrop(UUID uuid, BitSet bitset) {
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("UPDATE " + preferenceTable + " SET ITEMDROP=? WHERE UUID=?");
                statement.setString(1, BitSetUtils.toNumberString(bitset));
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
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

    public static void setHologram(UUID uuid, BitSet bitset) {
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("UPDATE " + preferenceTable + " SET HOLOGRAM=? WHERE UUID=?");
                statement.setString(1, BitSetUtils.toNumberString(bitset));
                statement.setString(2, uuid.toString());
                statement.executeUpdate();
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

    public static Map<Modules, BitSet> getPlayerInfo(UUID uuid) {
        Map<Modules, BitSet> map = new HashMap<>();
        synchronized (syncdb) {
            open();
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM " + preferenceTable + " WHERE UUID=?");
                statement.setString(1, uuid.toString());
                ResultSet results = statement.executeQuery();
                results.next();

                String itemstand = results.getString("ITEMSTAND");
                String itemdrop = results.getString("ITEMDROP");
                String hologram = results.getString("HOLOGRAM");

                try {
                    if (VALID_BITSET.matcher(itemstand).matches()) {
                        map.put(Modules.ITEMSTAND, BitSetUtils.fromNumberString(itemstand));
                    } else {
                        map.put(Modules.ITEMSTAND, BitSet.valueOf(ArrayUtils.fromBase64String(itemstand)));
                    }

                    if (VALID_BITSET.matcher(itemdrop).matches()) {
                        map.put(Modules.ITEMDROP, BitSetUtils.fromNumberString(itemdrop));
                    } else {
                        map.put(Modules.ITEMDROP, BitSet.valueOf(ArrayUtils.fromBase64String(itemdrop)));
                    }

                    if (VALID_BITSET.matcher(hologram).matches()) {
                        map.put(Modules.HOLOGRAM, BitSetUtils.fromNumberString(hologram));
                    } else {
                        map.put(Modules.HOLOGRAM, BitSet.valueOf(ArrayUtils.fromBase64String(hologram)));
                    }
                } catch (Throwable e) {
                    new RuntimeException("Unable to load player preference (" + uuid + ")", e).printStackTrace();
                    map.put(Modules.ITEMSTAND, new BitSet());
                    map.put(Modules.ITEMDROP, new BitSet());
                    map.put(Modules.HOLOGRAM, new BitSet());
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
        return map;
    }

}
