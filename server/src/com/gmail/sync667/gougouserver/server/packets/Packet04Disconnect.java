package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet04Disconnect extends Packet {

    private int entityId;
    private String msg;

    /**
     * @param senderIp
     * @param port
     * @param data
     */
    public Packet04Disconnect(InetAddress senderIp, int port, byte[] data) {
        super(04, senderIp, port);

        String[] dataArray = readData(data).split("/");
        try {
            this.entityId = Integer.valueOf(dataArray[0]);
            this.msg = dataArray[1];
        } catch (NumberFormatException e) {
            this.entityId = 0;
            this.msg = null;
        }
    }

    /**
     * @param senderIp
     * @param port
     * @param entityId
     * @param msg
     */
    public Packet04Disconnect(InetAddress senderIp, int port, int entityId, String msg) {
        super(04, senderIp, port);

        this.entityId = entityId;
        this.msg = msg;
    }

    @Override
    public void writeData(GouGouServer server) {
        return;
    }

    @Override
    public byte[] getData() {
        return ("04" + this.entityId + "/" + this.msg).getBytes();
    }

    public int getEntityId() {
        return entityId;
    }

    public String getMsg() {
        return msg;
    }

}
