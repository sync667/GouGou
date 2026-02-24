package com.gougou.core.net;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean connected;
    private int entityId = -1;
    private long lastPingTime;
    private long latency;
    private Thread receiveThread;
    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();

    public interface ClientListener {
        default void onConnected(int entityId, float spawnX, float spawnY, long worldSeed) {}
        default void onDisconnected(String reason) {}
        default void onEntitySpawn(int entityId, String name, float x, float y, int type) {}
        default void onEntityDespawn(int entityId) {}
        default void onEntityMove(int entityId, float x, float y, int direction) {}
        default void onChatMessage(int entityId, String message) {}
        default void onPong(long latency) {}
    }

    public void addListener(ClientListener listener) { listeners.add(listener); }
    public void removeListener(ClientListener listener) { listeners.remove(listener); }

    public void connect(String host, int port, String username, int characterClass, int skinColor) throws IOException {
        socket = new DatagramSocket();
        socket.setSoTimeout(5000);
        serverAddress = InetAddress.getByName(host);
        serverPort = port;

        // Send handshake
        send(Protocol.createHandshake());

        // Wait for handshake ack
        byte[] buf = new byte[Protocol.MAX_PACKET_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        byte type = bb.get();
        if (type != Protocol.HANDSHAKE_ACK) {
            throw new IOException("Unexpected response from server");
        }
        boolean accepted = bb.get() == 1;
        byte[] msgBytes = new byte[bb.remaining()];
        bb.get(msgBytes);
        String message = new String(msgBytes, StandardCharsets.UTF_8);

        if (!accepted) {
            socket.close();
            throw new IOException("Server rejected connection: " + message);
        }

        // Send login
        send(Protocol.createLogin(username, characterClass, skinColor));

        // Wait for login ack
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
        type = bb.get();
        if (type != Protocol.LOGIN_ACK) {
            socket.close();
            throw new IOException("Login failed");
        }

        entityId = bb.getInt();
        float spawnX = bb.getFloat();
        float spawnY = bb.getFloat();
        long worldSeed = bb.getLong();

        connected = true;
        socket.setSoTimeout(0);

        // Start receive thread
        receiveThread = new Thread(this::receiveLoop, "GouGou-Client-Receive");
        receiveThread.setDaemon(true);
        receiveThread.start();

        for (ClientListener l : listeners) l.onConnected(entityId, spawnX, spawnY, worldSeed);
    }

    private void receiveLoop() {
        byte[] buf = new byte[Protocol.MAX_PACKET_SIZE];
        while (connected && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                handlePacket(ByteBuffer.wrap(packet.getData(), 0, packet.getLength()));
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    for (ClientListener l : listeners) l.onDisconnected(e.getMessage());
                }
            }
        }
    }

    private void handlePacket(ByteBuffer bb) {
        byte type = bb.get();
        switch (type) {
            case Protocol.SPAWN -> {
                int id = bb.getInt();
                int nameLen = Protocol.validateReadLength(bb.getInt(), bb.remaining(), Protocol.MAX_NAME_LENGTH);
                if (nameLen < 0) return;
                byte[] nameBytes = new byte[nameLen];
                bb.get(nameBytes);
                String name = new String(nameBytes, StandardCharsets.UTF_8);
                float x = bb.getFloat();
                float y = bb.getFloat();
                int entityType = bb.getInt();
                for (ClientListener l : listeners) l.onEntitySpawn(id, name, x, y, entityType);
            }
            case Protocol.DESPAWN -> {
                int id = bb.getInt();
                for (ClientListener l : listeners) l.onEntityDespawn(id);
            }
            case Protocol.MOVE -> {
                int id = bb.getInt();
                float x = bb.getFloat();
                float y = bb.getFloat();
                int dir = bb.getInt();
                for (ClientListener l : listeners) l.onEntityMove(id, x, y, dir);
            }
            case Protocol.CHAT -> {
                int id = bb.getInt();
                int msgLen = Protocol.validateReadLength(bb.getInt(), bb.remaining(), Protocol.MAX_CHAT_LENGTH);
                if (msgLen < 0) return;
                byte[] msgBytes = new byte[msgLen];
                bb.get(msgBytes);
                String msg = new String(msgBytes, StandardCharsets.UTF_8);
                for (ClientListener l : listeners) l.onChatMessage(id, msg);
            }
            case Protocol.PONG -> {
                long timestamp = bb.getLong();
                latency = System.currentTimeMillis() - timestamp;
                for (ClientListener l : listeners) l.onPong(latency);
            }
            case Protocol.DISCONNECT -> {
                connected = false;
                for (ClientListener l : listeners) l.onDisconnected("Server closed");
            }
        }
    }

    public void sendMove(float x, float y, int direction) {
        if (connected) send(Protocol.createMove(entityId, x, y, direction));
    }

    public void sendChat(String message) {
        if (connected) send(Protocol.createChat(entityId, message));
    }

    public void sendPing() {
        if (connected) {
            lastPingTime = System.currentTimeMillis();
            send(Protocol.createPing(lastPingTime));
        }
    }

    public void disconnect() {
        if (connected) {
            send(Protocol.createDisconnect(entityId));
            connected = false;
            socket.close();
        }
    }

    private void send(byte[] data) {
        try {
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send packet: " + e.getMessage());
        }
    }

    public boolean isConnected() { return connected; }
    public int getEntityId() { return entityId; }
    public long getLatency() { return latency; }

    public static List<String> discoverLANServers(int timeoutMs) {
        List<String> servers = new ArrayList<>();
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.setBroadcast(true);
            ds.setSoTimeout(timeoutMs);
            byte[] request = Protocol.DISCOVERY_REQUEST;
            DatagramPacket packet = new DatagramPacket(request, request.length,
                InetAddress.getByName("255.255.255.255"), Protocol.DISCOVERY_PORT);
            ds.send(packet);

            byte[] buf = new byte[256];
            long end = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < end) {
                try {
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    ds.receive(response);
                    String data = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                    String prefix = new String(Protocol.DISCOVERY_RESPONSE_PREFIX, StandardCharsets.UTF_8);
                    if (data.startsWith(prefix)) {
                        servers.add(response.getAddress().getHostAddress() + ":" + data.substring(prefix.length()));
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("LAN discovery failed: " + e.getMessage());
        }
        return servers;
    }
}
