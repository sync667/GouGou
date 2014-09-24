package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet05SpawnPosition extends Packet {

    private int x;
    private int y;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet05SpawnPosition(byte[] data) {
        super(05);

        String[] dataArray = readData(data).split("/");

        try {
            this.x = Integer.valueOf(dataArray[0]);
            this.y = Integer.valueOf(dataArray[1]);
        } catch (NumberFormatException e) {
            this.x = 0;
            this.y = 0;
        }
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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
