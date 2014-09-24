package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet04Disconnect extends Packet {

    private final String msg;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet04Disconnect(byte[] data) {
        super(04);

        msg = readData(data);
    }

    /*
     * Client can not send server side packet!
     */
    @Override
    public void writeData(GouGouClient client) {
        return;
    }

    /*
     * Client can not parse packet data!
     */
    @Override
    public byte[] getData() {
        return null;
    }

    public String getMsg() {
        return msg;
    }

}
