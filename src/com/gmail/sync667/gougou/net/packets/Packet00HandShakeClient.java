package com.gmail.sync667.gougou.net.packets;

import com.gmail.sync667.gougou.net.GouGouClient;

public class Packet00HandShakeClient extends Packet {

    private int PROTOCOL_VERSION;
    private short nextState;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet00HandShakeClient(byte[] data) {
        super(00);

        String[] dataArray = readData(data).split("/");
        try {
            this.PROTOCOL_VERSION = Integer.valueOf(dataArray[0]);
            this.nextState = Short.valueOf(dataArray[1]);
        } catch (NumberFormatException e) {
            this.PROTOCOL_VERSION = 0;
            this.nextState = 0;
        }
    }

    /**
     * @param int PROTOCOL_VERSION of client
     * @param short nextState (0 - list status, 1 - connecting to server)
     */
    public Packet00HandShakeClient(int PROTOCOL_VERSION, short nextState) {
        super(00);

        this.PROTOCOL_VERSION = PROTOCOL_VERSION;
        this.nextState = nextState;
    }

    @Override
    public void writeData(GouGouClient client) {
        client.sendData(getData());
    }

    @Override
    public byte[] getData() {
        return ("00" + this.PROTOCOL_VERSION + "/" + nextState).getBytes();
    }

}
