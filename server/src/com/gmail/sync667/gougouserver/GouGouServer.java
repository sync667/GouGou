package com.gmail.sync667.gougouserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gmail.sync667.gougouserver.config.ServerConfig;
import com.gmail.sync667.gougouserver.log.ConsoleLogger;
import com.gmail.sync667.gougouserver.server.entities.Entity;
import com.gmail.sync667.gougouserver.server.entities.player.Player;
import com.gmail.sync667.gougouserver.server.packets.Packet;
import com.gmail.sync667.gougouserver.server.packets.Packet.PacketTypes;
import com.gmail.sync667.gougouserver.server.packets.Packet00HandShakeClient;
import com.gmail.sync667.gougouserver.server.packets.Packet01HandShakeServer;
import com.gmail.sync667.gougouserver.server.packets.Packet02Login;
import com.gmail.sync667.gougouserver.server.packets.Packet03Connect;
import com.gmail.sync667.gougouserver.server.packets.Packet04Disconnect;
import com.gmail.sync667.gougouserver.server.packets.Packet05SpawnPosition;
import com.gmail.sync667.gougouserver.server.packets.Packet06SpawnEntity;
import com.gmail.sync667.gougouserver.server.packets.Packet07DespawnEntity;
import com.gmail.sync667.gougouserver.server.packets.Packet10EntityMove;

public class GouGouServer extends Thread {

    public String VERSION = "ALPHA-0.1 Build 7";
    public int PROTOCOL_VERSION = 2;
    private DatagramSocket socket;
    public static GouGouServer server;
    public static ConsoleLogger console;
    public static ServerConfig serverConfig;
    public int players;
    public int slots;
    public String MOTD;
    public int port;

    public List<Entity> entities = new ArrayList<Entity>(); // only temp for time when i set up
                                                            // levels
    public int spawnX = 80;
    public int spawnY = 100;

    public int ticksCount = 0;

    public GouGouServer() {
        server = this;
    }

    @Override
    public void run() {

        console = new ConsoleLogger(System.console());
        console.infoC("Starting GouGou Server [" + VERSION + "]...");

        serverConfig = new ServerConfig();

        slots = Integer.valueOf((String) serverConfig.getEntry("ServerSlots"));
        MOTD = (String) serverConfig.getEntry("ServerMOTD");

        try {
            this.port = Integer.valueOf(serverConfig.getEntry("ServerPort").toString());
            this.socket = new DatagramSocket(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            console.infoC("Failed to bind socket! Port is already used!");
            return;
        }

        console.infoC("Server started on " + serverConfig.getEntry("ServerIP") + ":" + port);

        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000D / 20D;

        int ticks = 0;

        long lastTimer = System.currentTimeMillis();
        double delta = 0;

        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1) {
                ticks++;
                tick();
                delta -= 1;
            }

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                ticks = 0;
            }

            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData());
            if (!message.startsWith("10")) {
                console.info("CLIENT > SERVER [" + message + "]");
            }
            parsePacket(packet.getData(), packet.getAddress(), packet.getPort());
        }
    }

    private void parsePacket(byte[] data, InetAddress ipAddress, int port) {
        String message = new String(data).trim();
        PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
        switch (type) {
            default:
            case INVAILD:
                break;
            case HANDSHAKE_CLIENT:
                Packet00HandShakeClient packet00 = new Packet00HandShakeClient(ipAddress, port, data);

                if (packet00.getNextState() == 1) {
                    if (packet00.getPROTOCOL_VERSION() == PROTOCOL_VERSION) {
                        if (players != slots) {
                            server.sendData(ipAddress, port, new Packet01HandShakeServer(ipAddress, port,
                                    PROTOCOL_VERSION, players, slots, (short) 1, MOTD).getData());
                        } else {
                            server.sendData(ipAddress, port, new Packet04Disconnect(ipAddress, port, 0,
                                    "Serwer jest pelny!").getData());
                        }
                    } else {
                        server.sendData(ipAddress, port, new Packet04Disconnect(ipAddress, port, 0,
                                "Serwer posiada niekompatybilny protokol!").getData());
                    }
                }
                break;
            case HANDSHAKE_SERVER:
                break;
            case LOGIN:
                Packet02Login packet02 = new Packet02Login(ipAddress, port, data);
                for (Entity e : entities) {
                    if (e != null) {
                        if (e.getPlayerName() != null) {
                            if (e.getPlayerName().equalsIgnoreCase(packet02.getUsername())) {
                                server.sendData(ipAddress, port, new Packet04Disconnect(ipAddress, port, 0,
                                        "Taki gracz jest juz podlaczony!").getData());
                                break;
                            }
                        }
                    }
                }

                int entityId = 0;
                boolean randoming = true;
                int indexR = 0;
                while (randoming) {
                    indexR++;
                    randoming = false;
                    entityId = new Random().nextInt((10000 - 1) + 1) + 1;
                    for (Entity e : entities) {
                        if (e != null) {
                            if (e.getEntityId() == entityId) {
                                randoming = true;
                            }
                        }
                    }
                    if (indexR > 100) {
                        randoming = false;
                        console.warning("Error on randoming entityId!");
                        break;
                    }

                }

                for (Entity e : entities) {
                    if (e != null && e.entityId != entityId) {
                        server.sendData(ipAddress, port, new Packet06SpawnEntity(ipAddress, port, e.entityId, e.x, e.y,
                                e.getName(), e.getPlayerName()).getData());
                    }
                }

                entities.add(new Player(entityId, 0, 0, packet02.getUsername(), ipAddress, port));
                server.sendData(ipAddress, port, new Packet03Connect(ipAddress, port, entityId).getData());
                server.sendData(ipAddress, port, new Packet05SpawnPosition(ipAddress, port, spawnX, spawnY).getData());
                players++;
                server.sendDataToAllPlayers(new Packet06SpawnEntity(null, 0, entityId, spawnX, spawnY, "Gracz",
                        packet02.getUsername()).getData());
                console.info("[" + ipAddress + ":" + port + "] " + packet02.getUsername() + " join to the game!");
                break;
            case CONNECT:
                break;
            case DISCONNECT:
                Packet04Disconnect packet04 = new Packet04Disconnect(ipAddress, port, data);
                String username = "";
                if (packet04.getEntityId() != 0) {
                    int index = 0;
                    for (Entity e : entities) {
                        index++;
                        if (e.entityId == packet04.getEntityId()) {
                            username = e.getPlayerName();
                            break;
                        }
                    }

                    if (index != 0) {
                        entities.remove(index);
                        server.sendDataToAllPlayers(new Packet07DespawnEntity(null, 0, packet04.getEntityId())
                                .getData());

                        players--;
                        console.info("[" + ipAddress + ":" + port + "] " + username + " leave to the game!");
                    }
                }
                break;
            case SPAWN_POSITION:
                break;
            case SPAWN_ENTITY:
                break;
            case DESPAWN_ENTITY:
                break;
            case ENTITY_MOVE:
                Packet10EntityMove packet10 = new Packet10EntityMove(ipAddress, port, data);

                for (Entity e : entities) {
                    if (e.getEntityId() == packet10.getEntityId()) {
                        boolean moved = false;
                        if (e.x != packet10.getX()) {
                            e.x = packet10.getX();
                            moved = true;
                        }
                        if (e.y != packet10.getY()) {
                            e.y = packet10.getY();
                            moved = true;
                        }

                        if (e.getMovingDir() != packet10.getMovingDir()) {
                            e.setMovingDir(packet10.getMovingDir());
                            moved = true;
                        }

                        if (moved) {
                            server.sendDataToAllPlayers(new Packet10EntityMove(null, 0, e.getEntityId(), e.x, e.y, e
                                    .getMovingDir()).getData());
                        }
                        break;
                    }
                }
                break;
        }
    }

    public void tick() {
        ticksCount++;
        // Level.tick();
    }

    public static GouGouServer getServer() {
        return server;
    }

    public static ConsoleLogger getConsole() {
        return console;
    }

    public void sendData(InetAddress ipAddress, int port, byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        String message = new String(packet.getData());
        if (!message.startsWith("10")) {
            console.info("SERVER > CLIENT [" + message + "]");
        }
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDataToAllPlayers(byte[] data) {
        for (Entity e : entities) {
            sendData(e.getIpAddress(), e.getPort(), data);
        }
    }

    @Override
    public synchronized void start() {
        new Thread(this).start();
    }

    public static void main(String[] args) {
        new GouGouServer().start();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }
}
