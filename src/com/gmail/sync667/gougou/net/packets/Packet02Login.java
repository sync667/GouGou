package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet02Login extends Packet {

    private final String username;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet02Login(byte[] data) {
        super(02);

        this.username = readData(data);
    }

    /**
     * @param username
     *            Name of player.
     */
    public Packet02Login(String username) {
        super(02);

        this.username = username;
    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("02" + this.username).getBytes();
    }

}
