package com.gmail.sync667.gougou.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Objects;

import com.gmail.sync667.gougou.GouGou;
import com.gmail.sync667.gougou.entities.Entity;
import com.gmail.sync667.gougou.entities.Player;
import com.gmail.sync667.gougou.net.packets.Packet;
import com.gmail.sync667.gougou.net.packets.Packet.PacketTypes;
import com.gmail.sync667.gougou.net.packets.Packet01HandShakeServer;
import com.gmail.sync667.gougou.net.packets.Packet02Login;
import com.gmail.sync667.gougou.net.packets.Packet03Connect;
import com.gmail.sync667.gougou.net.packets.Packet04Disconnect;
import com.gmail.sync667.gougou.net.packets.Packet05SpawnPosition;
import com.gmail.sync667.gougou.net.packets.Packet06SpawnEntity;
import com.gmail.sync667.gougou.net.packets.Packet07DespawnEntity;
import com.gmail.sync667.gougou.net.packets.Packet10EntityMove;

public class GouGouClient extends Thread {

    private InetAddress ipAddress;
    private int port;
    private DatagramSocket socket;
    private final GouGou gougou;

    public GouGouClient(GouGou gougou, String ipAddress, int port) {
        this.gougou = gougou;
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
            this.port = port;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public GouGouClient(GouGou gougou, InetAddress ipAddress, int port) {
        this.gougou = gougou;
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = ipAddress;
            this.port = port;
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData());

            parsePacket(packet.getData());

            System.out.println("SERVER > " + message);

        }
    }

    private void parsePacket(byte[] data) {
        String message = new String(data).trim();
        PacketTypes type = Packet.lookupPacket(message.substring(0, 2));
        switch (type) {
            default:
            case INVAILD:
                break;
            case HANDSHAKE_CLIENT:
                break;
            case HANDSHAKE_SERVER:
                Packet01HandShakeServer packet01 = new Packet01HandShakeServer(data);

                if (packet01.getNextState() == 1) {
                    this.sendData(new Packet02Login(gougou.username).getData());
                }
                break;
            case LOGIN:
                break;
            case CONNECT:
                Packet03Connect packet03 = new Packet03Connect(data);

                for (Entity e : gougou.level.entities) {
                    if (e != null) {
                        if (e.getEntityId() == packet03.getEntityId()) {
                            break;
                        }
                    }
                }

                gougou.player = new Player(packet03.getEntityId(), gougou.level, 0, 0, gougou.input, gougou.username);

                break;
            case DISCONNECT:
                Packet04Disconnect packet04 = new Packet04Disconnect(data);
                break;
            case SPAWN_POSITION:
                Packet05SpawnPosition packet05 = new Packet05SpawnPosition(data);

                gougou.player.x = packet05.getX();
                gougou.player.y = packet05.getY();

                gougou.level.addEntity(gougou.player);
                break;
            case SPAWN_ENTITY:
                Packet06SpawnEntity packet06 = new Packet06SpawnEntity(data);

                if (packet06.getName().equalsIgnoreCase("Gracz")) {
                    Player player = new Player(packet06.getEntityId(), gougou.level, packet06.getX(), packet06.getY(),
                            gougou.input, packet06.getUsername());

                    gougou.level.addEntity(player);
                }
                break;
            case DESPAWN_ENTITY:
                Packet07DespawnEntity packet07 = new Packet07DespawnEntity(data);

                gougou.level.removeEntity(packet07.getEntityId());

                break;
            case ENTITY_MOVE:
                Packet10EntityMove packet10 = new Packet10EntityMove(data);

                for (Entity e : gougou.level.entities) {
                    if (!Objects.equals(e, gougou.player)) {
                        if (e.getEntityId() == packet10.getEntityId()) {
                            if (e.x != packet10.getX()) {
                                e.x = packet10.getX();
                            }
                            if (e.y != packet10.getY()) {
                                e.y = packet10.getY();
                            }
                            break;
                        }
                    }
                }
                break;
        }
    }

    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = new String(packet.getData());

        System.out.println("CLIENT > " + message);
    }
}
