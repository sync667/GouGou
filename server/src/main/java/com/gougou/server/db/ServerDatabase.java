package com.gougou.server.db;

import com.google.gson.Gson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServerDatabase {
    private Connection connection;
    private static final Gson GSON = new Gson();

    public ServerDatabase(String dataDir) throws SQLException {
        String url = "jdbc:h2:" + dataDir + "/gougou_server;AUTO_RECONNECT=TRUE";
        connection = DriverManager.getConnection(url, "sa", "");
        initializeTables();
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS players (
                    username VARCHAR(32) PRIMARY KEY,
                    character_class INT DEFAULT 0,
                    skin_color INT DEFAULT 0,
                    level INT DEFAULT 1,
                    experience INT DEFAULT 0,
                    health INT DEFAULT 100,
                    max_health INT DEFAULT 100,
                    mana INT DEFAULT 50,
                    max_mana INT DEFAULT 50,
                    inventory_json TEXT DEFAULT '["Sword","Shield","Potion"]',
                    last_x FLOAT DEFAULT 0,
                    last_y FLOAT DEFAULT 0,
                    games_played INT DEFAULT 0,
                    total_play_time_minutes INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS server_bans (
                    username VARCHAR(32) PRIMARY KEY,
                    reason VARCHAR(256) DEFAULT '',
                    banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    public void savePlayer(String username, int characterClass, int skinColor,
                           int level, int experience,
                           int health, int maxHealth, int mana, int maxMana,
                           List<String> inventory, float x, float y) {
        String sql = """
            MERGE INTO players (username, character_class, skin_color, level, experience,
                health, max_health, mana, max_mana, inventory_json, last_x, last_y, last_login)
            KEY (username)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, characterClass);
            ps.setInt(3, skinColor);
            ps.setInt(4, level);
            ps.setInt(5, experience);
            ps.setInt(6, health);
            ps.setInt(7, maxHealth);
            ps.setInt(8, mana);
            ps.setInt(9, maxMana);
            ps.setString(10, GSON.toJson(inventory));
            ps.setFloat(11, x);
            ps.setFloat(12, y);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save player " + username + ": " + e.getMessage());
        }
    }

    public PlayerRecord loadPlayer(String username) {
        String sql = "SELECT * FROM players WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerRecord record = new PlayerRecord();
                    record.username = rs.getString("username");
                    record.characterClass = rs.getInt("character_class");
                    record.skinColor = rs.getInt("skin_color");
                    record.level = rs.getInt("level");
                    record.experience = rs.getInt("experience");
                    record.health = rs.getInt("health");
                    record.maxHealth = rs.getInt("max_health");
                    record.mana = rs.getInt("mana");
                    record.maxMana = rs.getInt("max_mana");
                    String invJson = rs.getString("inventory_json");
                    record.inventory = new ArrayList<>();
                    if (invJson != null) {
                        String[] items = GSON.fromJson(invJson, String[].class);
                        if (items != null) {
                            for (String item : items) record.inventory.add(item);
                        }
                    }
                    record.lastX = rs.getFloat("last_x");
                    record.lastY = rs.getFloat("last_y");
                    record.found = true;
                    return record;
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load player " + username + ": " + e.getMessage());
        }
        return new PlayerRecord(); // not found
    }

    public void banPlayer(String username, String reason) {
        String sql = "MERGE INTO server_bans (username, reason) KEY (username) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, reason);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to ban player: " + e.getMessage());
        }
    }

    public boolean isPlayerBanned(String username) {
        String sql = "SELECT COUNT(*) FROM server_bans WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Failed to check ban: " + e.getMessage());
        }
        return false;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database: " + e.getMessage());
        }
    }

    public static class PlayerRecord {
        public boolean found = false;
        public String username;
        public int characterClass;
        public int skinColor;
        public int level;
        public int experience;
        public int health;
        public int maxHealth;
        public int mana;
        public int maxMana;
        public List<String> inventory = new ArrayList<>();
        public float lastX;
        public float lastY;
    }
}
