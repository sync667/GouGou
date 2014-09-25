package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet01HandShakeServer extends Packet {

    private int PROTOCOL_VERSION;
    private int players = 0;
    private int slots = 0;
    private final String motd;
    private short nextState;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet01HandShakeServer(byte[] data) {
        super(01);

        String[] dataArray = readData(data).split("/");
        try {
            this.PROTOCOL_VERSION = Integer.valueOf(dataArray[0]);
            this.players = Integer.valueOf(dataArray[1]);
            this.slots = Integer.valueOf(dataArray[2]);
            this.nextState = Short.valueOf(dataArray[3]);
        } catch (NumberFormatException e) {
            this.PROTOCOL_VERSION = 0;
            this.players = 0;
            this.slots = 0;
        }
        this.motd = dataArray[4];
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

    public int getPROTOCOL_VERSION() {
        return PROTOCOL_VERSION;
    }

    public int getPlayers() {
        return players;
    }

    public int getSlots() {
        return slots;
    }

    public short getNextState() {
        return nextState;
    }

    public String getMotd() {
        return motd;
    }

}
