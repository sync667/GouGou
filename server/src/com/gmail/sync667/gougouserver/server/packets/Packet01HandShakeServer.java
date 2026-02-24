package com.gmail.sync667.gougouserver.server.packets;

import java.net.InetAddress;

import com.gmail.sync667.gougouserver.GouGouServer;

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
    public Packet01HandShakeServer(InetAddress senderIp, int port, byte[] data) {
        super(01, senderIp, port);

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

    /**
     * @param senderIp
     *            address
     * @param port
     *            of connection
     * @param PROTOCOL_VERSION
     *            to match
     * @param players
     *            on server
     * @param slots
     *            of server
     * @param nextState
     *            (0 - for server list, 1 - for connect)
     * @param motd
     *            of server
     */
    public Packet01HandShakeServer(InetAddress senderIp, int port, int PROTOCOL_VERSION, int players, int slots,
            short nextState, String motd) {
        super(01, senderIp, port);

        this.PROTOCOL_VERSION = PROTOCOL_VERSION;
        this.players = players;
        this.slots = slots;
        this.nextState = nextState;
        this.motd = motd;
    }

    @Override
    public void writeData(GouGouServer server) {
        // server.sendData(getData(), ipAddress, port);
    }

    @Override
    public byte[] getData() {
        return ("01" + this.PROTOCOL_VERSION + "/" + players + "/" + slots + "/" + nextState + "/" + motd).getBytes();
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
