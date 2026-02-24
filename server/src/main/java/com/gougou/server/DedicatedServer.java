package com.gougou.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

public class DedicatedServer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  GouGou Dedicated Server v1.0");
        System.out.println("=================================");

        ServerConfig config = loadConfig();

        System.out.println("Server Name: " + config.serverName);
        System.out.println("Port: " + config.port);
        System.out.println("Max Players: " + config.maxPlayers);
        System.out.println("World Size: " + config.worldSize + "x" + config.worldSize);
        System.out.println("World Seed: " + config.worldSeed);
        System.out.println();

        // Use a simple UDP server implementation
        Thread serverThread = new Thread(() -> {
            try {
                var socket = new java.net.DatagramSocket(config.port);
                System.out.println("Server listening on port " + config.port);
                System.out.println("Type 'stop' to shut down the server.");

                // Discovery responder thread
                Thread discoveryThread = new Thread(() -> {
                    try (var ds = new java.net.DatagramSocket(config.port + 1)) {
                        byte[] buf = new byte[256];
                        while (!socket.isClosed()) {
                            var packet = new java.net.DatagramPacket(buf, buf.length);
                            ds.receive(packet);
                            String request = new String(packet.getData(), 0, packet.getLength());
                            if (request.equals("GOUGOU_DISCOVER")) {
                                String response = "GOUGOU_SERVER:" + config.serverName + "|0/" + config.maxPlayers + "|" + config.port;
                                byte[] responseData = response.getBytes();
                                var responsePacket = new java.net.DatagramPacket(responseData, responseData.length,
                                    packet.getAddress(), packet.getPort());
                                ds.send(responsePacket);
                            }
                        }
                    } catch (Exception e) {
                        if (!socket.isClosed()) {
                            System.err.println("Discovery error: " + e.getMessage());
                        }
                    }
                }, "Discovery");
                discoveryThread.setDaemon(true);
                discoveryThread.start();

                // Main receive loop
                byte[] buf = new byte[1024];
                while (!socket.isClosed()) {
                    var packet = new java.net.DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    // Handle packet (simplified for dedicated server)
                    System.out.println("Received packet from " + packet.getAddress() + ":" + packet.getPort());
                }
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        }, "Server");
        serverThread.setDaemon(true);
        serverThread.start();

        // Console input for commands
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("quit")) {
                System.out.println("Shutting down server...");
                break;
            } else if (line.equalsIgnoreCase("status")) {
                System.out.println("Server is running on port " + config.port);
            } else if (line.equalsIgnoreCase("help")) {
                System.out.println("Commands: stop, status, help");
            } else if (!line.isEmpty()) {
                System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }
        scanner.close();
        System.out.println("Server stopped.");
        System.exit(0);
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
        // Create default config
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
    }
}
