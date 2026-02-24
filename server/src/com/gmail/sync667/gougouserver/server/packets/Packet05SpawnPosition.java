package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet05SpawnPosition extends Packet {

    private int x;
    private int y;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet05SpawnPosition(InetAddress senderIp, int port, byte[] data) {
        super(05, senderIp, port);

        String[] dataArray = readData(data).split("/");

        try {
            this.x = Integer.valueOf(dataArray[0]);
            this.y = Integer.valueOf(dataArray[1]);
        } catch (NumberFormatException e) {
            this.x = 0;
            this.y = 0;
        }
    }

    /**
     * @param senderIp
     * @param port
     * @param x
     * @param y
     */
    public Packet05SpawnPosition(InetAddress senderIp, int port, int x, int y) {
        super(05, senderIp, port);

        this.x = x;
        this.y = y;
    }

    @Override
    public void writeData(GouGouServer server) {
        return;
    }

    @Override
    public byte[] getData() {
        return ("05" + this.x + "/" + this.y).getBytes();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
