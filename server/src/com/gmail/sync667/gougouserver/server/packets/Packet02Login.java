package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet02Login extends Packet {

    private final String username;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet02Login(InetAddress senderIp, int port, byte[] data) {
        super(02, senderIp, port);

        this.username = readData(data);
    }

    /**
     * @param username
     *            of player.
     */
    public Packet02Login(InetAddress senderIp, int port, String username) {
        super(02, senderIp, port);

        this.username = username;
    }

    /*
     * Server can not send client side packet!
     */
    @Override
    public void writeData(GouGouServer server) {
        return;
    }

    /*
     * Server can not parse packet data!
     */
    @Override
    public byte[] getData() {
        return null;
    }

    public String getUsername() {
        return username.trim();
    }

}
