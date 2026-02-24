package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public abstract class Packet {

    public static enum PacketTypes {
        INVAILD(-1), HANDSHAKE_CLIENT(00), HANDSHAKE_SERVER(01), LOGIN(02), CONNECT(03), DISCONNECT(04), SPAWN_POSITION(
                05), SPAWN_ENTITY(06), DESPAWN_ENTITY(07), ENTITY_MOVE(10), PING(11);

        private int packetId;

        private PacketTypes(int packetId) {
            this.packetId = packetId;
        }

        public int getId() {
            return packetId;
        }
    }

    public byte packetId;
    public InetAddress senderIp;
    public int port;

    public Packet(int packetId, InetAddress senderIp, int port) {
        this.packetId = (byte) packetId;
        this.senderIp = senderIp;
        this.port = port;
    }

    public abstract void writeData(GouGouServer server);

    public String readData(byte[] data) {
        String message = new String(data).trim();
        return message.substring(2);
    }

    public abstract byte[] getData();

    public static PacketTypes lookupPacket(String packetId) {
        try {
            return lookupPacket(Integer.valueOf(packetId));
        } catch (NumberFormatException e) {
            return PacketTypes.INVAILD;
        }
    }

    public static PacketTypes lookupPacket(int id) {
        for (PacketTypes p : PacketTypes.values()) {
            if (p.getId() == id) {
                return p;
            }
        }
        return PacketTypes.INVAILD;
    }
}
