package com.gougou.core.net;

import com.gougou.core.world.World;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private DatagramSocket socket;
    private DatagramSocket discoverySocket;
    private int port;
    private String serverName;
    private boolean running;
    private World world;
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private int nextEntityId = 1;
    private int maxPlayers = 10;

    private static class ClientInfo {
        InetAddress address;
        int port;
        int entityId;
        String username;
        float x, y;
        int direction;
        long lastActivity;

        ClientInfo(InetAddress addr, int port, int entityId, String username) {
            this.address = addr;
            this.port = port;
            this.entityId = entityId;
            this.username = username;
            this.lastActivity = System.currentTimeMillis();
        }

        String getKey() { return address.getHostAddress() + ":" + port; }
    }

    public GameServer(int port, String serverName) {
        this.port = port;
        this.serverName = serverName;
    }

    public void start(long worldSeed, int worldWidth, int worldHeight) throws IOException {
        world = new World(worldWidth, worldHeight, worldSeed);
        socket = new DatagramSocket(port);
        running = true;

        // Game receive thread
        Thread receiveThread = new Thread(this::receiveLoop, "GouGou-Server-Receive");
        receiveThread.setDaemon(true);
        receiveThread.start();

        // LAN discovery responder
        Thread discoveryThread = new Thread(this::discoveryLoop, "GouGou-Server-Discovery");
        discoveryThread.setDaemon(true);
        discoveryThread.start();

        System.out.println("Server started on port " + port);
    }

    private void receiveLoop() {
        byte[] buf = new byte[Protocol.MAX_PACKET_SIZE];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                handlePacket(packet);
            } catch (IOException e) {
                if (running) System.err.println("Server receive error: " + e.getMessage());
            }
        }
    }

    private void discoveryLoop() {
        try {
            discoverySocket = new DatagramSocket(Protocol.DISCOVERY_PORT);
            byte[] buf = new byte[256];
            while (running) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    discoverySocket.receive(packet);
                    String request = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    if (request.equals("GOUGOU_DISCOVER")) {
                        String response = "GOUGOU_SERVER:" + serverName + "|" + clients.size() + "/" + maxPlayers + "|" + port;
                        byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                            packet.getAddress(), packet.getPort());
                        discoverySocket.send(responsePacket);
                    }
                } catch (IOException e) {
                    if (running) System.err.println("Discovery error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start discovery listener: " + e.getMessage());
        }
    }

    private void handlePacket(DatagramPacket packet) {
        ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        byte type = bb.get();
        String clientKey = packet.getAddress().getHostAddress() + ":" + packet.getPort();

        switch (type) {
            case Protocol.HANDSHAKE -> {
                int version = bb.getInt();
                boolean accepted = version == Protocol.VERSION;
                String msg = accepted ? "Welcome to " + serverName : "Version mismatch";
                sendTo(packet.getAddress(), packet.getPort(), Protocol.createHandshakeAck(accepted, msg));
            }
            case Protocol.LOGIN -> {
                int nameLen = bb.getInt();
                byte[] nameBytes = new byte[nameLen];
                bb.get(nameBytes);
                String username = new String(nameBytes, StandardCharsets.UTF_8);
                int charClass = bb.getInt();
                int skinColor = bb.getInt();

                if (clients.size() >= maxPlayers) {
                    sendTo(packet.getAddress(), packet.getPort(),
                        Protocol.createHandshakeAck(false, "Server full"));
                    return;
                }

                int eid = nextEntityId++;
                ClientInfo client = new ClientInfo(packet.getAddress(), packet.getPort(), eid, username);
                client.x = world.getSpawnX();
                client.y = world.getSpawnY();
                clients.put(clientKey, client);

                // Send login ack with spawn position and world seed
                sendTo(packet.getAddress(), packet.getPort(),
                    Protocol.createLoginAck(eid, client.x, client.y, world.getSeed()));

                // Notify existing clients about new player
                byte[] spawnPacket = Protocol.createSpawn(eid, username, client.x, client.y, 0);
                broadcastExcept(clientKey, spawnPacket);

                // Send existing entities to new client
                for (ClientInfo existing : clients.values()) {
                    if (!existing.getKey().equals(clientKey)) {
                        sendTo(packet.getAddress(), packet.getPort(),
                            Protocol.createSpawn(existing.entityId, existing.username, existing.x, existing.y, 0));
                    }
                }

                System.out.println(username + " joined the server (ID: " + eid + ")");
            }
            case Protocol.DISCONNECT -> {
                ClientInfo client = clients.remove(clientKey);
                if (client != null) {
                    broadcastExcept(clientKey, Protocol.createDisconnect(client.entityId));
                    System.out.println(client.username + " left the server");
                }
            }
            case Protocol.MOVE -> {
                ClientInfo client = clients.get(clientKey);
                if (client != null) {
                    client.lastActivity = System.currentTimeMillis();
                    int eid = bb.getInt();
                    client.x = bb.getFloat();
                    client.y = bb.getFloat();
                    client.direction = bb.getInt();
                    broadcastExcept(clientKey, Protocol.createMove(eid, client.x, client.y, client.direction));
                }
            }
            case Protocol.CHAT -> {
                ClientInfo client = clients.get(clientKey);
                if (client != null) {
                    int eid = bb.getInt();
                    int msgLen = bb.getInt();
                    byte[] msgBytes = new byte[msgLen];
                    bb.get(msgBytes);
                    String msg = new String(msgBytes, StandardCharsets.UTF_8);
                    // Broadcast to all including sender
                    broadcast(Protocol.createChat(eid, msg));
                    System.out.println("[Chat] " + client.username + ": " + msg);
                }
            }
            case Protocol.PING -> {
                long timestamp = bb.getLong();
                sendTo(packet.getAddress(), packet.getPort(), Protocol.createPong(timestamp));
            }
        }
    }

    private void sendTo(InetAddress address, int port, byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send to " + address + ":" + port);
        }
    }

    private void broadcast(byte[] data) {
        for (ClientInfo client : clients.values()) {
            sendTo(client.address, client.port, data);
        }
    }

    private void broadcastExcept(String excludeKey, byte[] data) {
        for (Map.Entry<String, ClientInfo> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeKey)) {
                sendTo(entry.getValue().address, entry.getValue().port, data);
            }
        }
    }

    public void stop() {
        running = false;
        broadcast(Protocol.createDisconnect(0));
        if (socket != null) socket.close();
        if (discoverySocket != null) discoverySocket.close();
        System.out.println("Server stopped");
    }

    public boolean isRunning() { return running; }
    public int getPlayerCount() { return clients.size(); }
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int max) { this.maxPlayers = max; }
    public World getWorld() { return world; }
    public int getPort() { return port; }
}
