package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet11Ping extends Packet {

    /**
     * @param parsed
     *            packet data.
     */
    public Packet11Ping(byte[] data) {
        super(11);
    }

    public Packet11Ping() {
        super(11);

    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("11").getBytes();
    }

}
