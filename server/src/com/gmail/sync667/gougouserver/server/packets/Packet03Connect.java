package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet03Connect extends Packet {

    private int entityId;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet03Connect(InetAddress senderIp, int port, byte[] data) {
        super(03, senderIp, port);

        try {
            this.entityId = Integer.valueOf(readData(data));
        } catch (NumberFormatException e) {
            this.entityId = 0;
        }
    }

    /**
     * @param senderIp
     * @param port
     * @param entityId
     */
    public Packet03Connect(InetAddress senderIp, int port, int entityId) {
        super(03, senderIp, port);

        this.entityId = entityId;
    }

    @Override
    public void writeData(GouGouServer server) {
        return;
    }

    @Override
    public byte[] getData() {
        return ("03" + this.entityId).getBytes();
    }

    public int getEntityId() {
        return entityId;
    }

}
