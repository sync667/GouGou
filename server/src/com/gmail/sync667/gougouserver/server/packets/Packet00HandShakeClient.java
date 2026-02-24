package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

public class Packet00HandShakeClient extends Packet {

    private int PROTOCOL_VERSION;
    private short nextState;

    /**
     * @param parsed
     *            packet data.
     */
    public Packet00HandShakeClient(InetAddress senderIp, int port, byte[] data) {
        super(00, senderIp, port);

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
    public Packet00HandShakeClient(InetAddress senderIp, int port, int PROTOCOL_VERSION, short nextState) {
        super(00, senderIp, port);

        this.PROTOCOL_VERSION = PROTOCOL_VERSION;
        this.nextState = nextState;
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

    public int getPROTOCOL_VERSION() {
        return PROTOCOL_VERSION;
    }

    public short getNextState() {
        return nextState;
    }

}
