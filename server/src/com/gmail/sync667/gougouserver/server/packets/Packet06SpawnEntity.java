package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet06SpawnEntity extends Packet {

    private int entityId;
    private int x;
    private int y;
    private final String name;
    private final String username;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet06SpawnEntity(InetAddress senderIp, int port, byte[] data) {
        super(06, senderIp, port);

        String[] dataArray = readData(data).split("/");

        try {
            this.entityId = Integer.valueOf(dataArray[0]);
            this.x = Integer.valueOf(dataArray[1]);
            this.y = Integer.valueOf(dataArray[2]);
        } catch (NumberFormatException e) {
            this.entityId = 0;
            this.x = 0;
            this.y = 0;
        }
        this.name = dataArray[3];
        this.username = dataArray[4];

    }

    /**
     * @param senderIp
     * @param port
     * @param entityId
     * @param x
     * @param y
     * @param name
     * @param username
     */
    public Packet06SpawnEntity(InetAddress senderIp, int port, int entityId, int x, int y, String name, String username) {
        super(06, senderIp, port);

        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.name = name;
        this.username = username;

    }

    @Override
    public void writeData(GouGouServer server) {
        return;
    }

    @Override
    public byte[] getData() {
        return ("06" + this.entityId + "/" + this.x + "/" + this.y + "/" + this.name + "/" + this.username).getBytes();
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}
