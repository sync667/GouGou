package com.gougou.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gougou.core.net.GameServer;
import com.gougou.server.db.ServerDatabase;

import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class DedicatedServer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static GameServer gameServer;
    private static ServerDatabase database;

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  GouGou Dedicated Server v1.0");
        System.out.println("  Powered by Netty + H2 Database");
        System.out.println("=================================");

        ServerConfig config = loadConfig();

        System.out.println("Server Name: " + config.serverName);
        System.out.println("Port: " + config.port);
        System.out.println("Max Players: " + config.maxPlayers);
        System.out.println("World Size: " + config.worldSize + "x" + config.worldSize);
        System.out.println("World Seed: " + config.worldSeed);
        System.out.println();

        // Initialize database
        try {
            String dataDir = config.dataDirectory;
            Files.createDirectories(Paths.get(dataDir));
            database = new ServerDatabase(dataDir);
            System.out.println("Database initialized at: " + dataDir);
        } catch (SQLException | IOException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            System.exit(1);
        }

        // Start game server
        gameServer = new GameServer(config.port, config.serverName);
        gameServer.setMaxPlayers(config.maxPlayers);

        try {
            gameServer.start(config.worldSeed, config.worldSize, config.worldSize);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            database.close();
            System.exit(1);
        }

        // Periodic player data save (every 60 seconds)
        Timer saveTimer = new Timer("PlayerSave", true);
        saveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                saveAllPlayers();
            }
        }, 60000, 60000);

        System.out.println("Type 'help' for available commands.");

        // Console input
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            handleCommand(line);
            if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("quit")) {
                break;
            }
        }
        scanner.close();

        // Shutdown
        saveAllPlayers();
        saveTimer.cancel();
        gameServer.stop();
        database.close();
        System.out.println("Server stopped.");
        System.exit(0);
    }

    private static void handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "stop", "quit" -> System.out.println("Shutting down server...");
            case "status" -> {
                if (gameServer == null) { System.out.println("Server not initialized."); break; }
                System.out.println("Server: " + (gameServer.isRunning() ? "RUNNING" : "STOPPED"));
                System.out.println("Players: " + gameServer.getPlayerCount() + "/" + gameServer.getMaxPlayers());
            }
            case "players" -> {
                if (gameServer == null) { System.out.println("Server not initialized."); break; }
                System.out.println("Online players:");
                for (var player : gameServer.getPlayers()) {
                    System.out.printf("  %s (ID:%d) at (%.1f, %.1f) HP:%d/%d%n",
                        player.username, player.entityId, player.x, player.y, player.health, player.maxHealth);
                }
            }
            case "save" -> {
                saveAllPlayers();
                System.out.println("All player data saved.");
            }
            case "help" -> {
                System.out.println("Commands:");
                System.out.println("  stop/quit  - Shut down the server");
                System.out.println("  status     - Show server status");
                System.out.println("  players    - List online players");
                System.out.println("  save       - Save all player data");
                System.out.println("  help       - Show this help");
            }
            default -> {
                if (!command.isEmpty()) {
                    System.out.println("Unknown command. Type 'help' for available commands.");
                }
            }
        }
    }

    private static void saveAllPlayers() {
        if (database == null || gameServer == null) return;
        for (var player : gameServer.getPlayers()) {
            database.savePlayer(
                player.username, player.characterClass, player.skinColor,
                player.level, player.experience,
                player.health, player.maxHealth, player.mana, player.maxMana,
                player.inventory, player.x, player.y
            );
        }
    }

    private static ServerConfig loadConfig() {
        Path configPath = Paths.get("server-config.json");
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                ServerConfig config = GSON.fromJson(json, ServerConfig.class);
                if (config != null) return config;
            } catch (Exception e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        }
        ServerConfig config = new ServerConfig();
        try {
            Files.writeString(configPath, GSON.toJson(config));
            System.out.println("Created default config file: server-config.json");
        } catch (Exception e) {
            System.err.println("Failed to save default config: " + e.getMessage());
        }
        return config;
    }

    static class ServerConfig {
        String serverName = "GouGou Server";
        int port = 7777;
        int maxPlayers = 10;
        int worldSize = 256;
        long worldSeed = 12345;
        String motd = "Welcome to GouGou!";
        String dataDirectory = "server-data";
    }
}
