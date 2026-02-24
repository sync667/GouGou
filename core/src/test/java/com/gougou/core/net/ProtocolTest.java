package com.gougou.core.net;

import org.junit.jupiter.api.Test;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {

    @Test
    void testCreateHandshake() {
        byte[] data = Protocol.createHandshake();
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.HANDSHAKE, bb.get());
        assertEquals(Protocol.VERSION, bb.getInt());
    }

    @Test
    void testCreateHandshakeAck() {
        byte[] data = Protocol.createHandshakeAck(true, "Welcome");
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.HANDSHAKE_ACK, bb.get());
        assertEquals(1, bb.get());
        byte[] msgBytes = new byte[bb.remaining()];
        bb.get(msgBytes);
        assertEquals("Welcome", new String(msgBytes, StandardCharsets.UTF_8));
    }

    @Test
    void testCreateHandshakeAckRejected() {
        byte[] data = Protocol.createHandshakeAck(false, "Version mismatch");
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.HANDSHAKE_ACK, bb.get());
        assertEquals(0, bb.get());
    }

    @Test
    void testCreateMove() {
        byte[] data = Protocol.createMove(5, 10.5f, 20.3f, 2);
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.MOVE, bb.get());
        assertEquals(5, bb.getInt());
        assertEquals(10.5f, bb.getFloat(), 0.01f);
        assertEquals(20.3f, bb.getFloat(), 0.01f);
        assertEquals(2, bb.getInt());
    }

    @Test
    void testCreateChat() {
        byte[] data = Protocol.createChat(3, "Hello world!");
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.CHAT, bb.get());
        assertEquals(3, bb.getInt());
        int msgLen = bb.getInt();
        byte[] msgBytes = new byte[msgLen];
        bb.get(msgBytes);
        assertEquals("Hello world!", new String(msgBytes, StandardCharsets.UTF_8));
    }

    @Test
    void testCreatePingPong() {
        long timestamp = System.currentTimeMillis();
        byte[] ping = Protocol.createPing(timestamp);
        ByteBuffer bb = ByteBuffer.wrap(ping);
        assertEquals(Protocol.PING, bb.get());
        assertEquals(timestamp, bb.getLong());

        byte[] pong = Protocol.createPong(timestamp);
        bb = ByteBuffer.wrap(pong);
        assertEquals(Protocol.PONG, bb.get());
        assertEquals(timestamp, bb.getLong());
    }

    @Test
    void testCreateLogin() {
        byte[] data = Protocol.createLogin("TestPlayer", 1, 3);
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.LOGIN, bb.get());
        int nameLen = bb.getInt();
        byte[] nameBytes = new byte[nameLen];
        bb.get(nameBytes);
        assertEquals("TestPlayer", new String(nameBytes, StandardCharsets.UTF_8));
        assertEquals(1, bb.getInt());
        assertEquals(3, bb.getInt());
    }

    @Test
    void testCreateLoginAck() {
        byte[] data = Protocol.createLoginAck(42, 10.0f, 20.0f, 12345L);
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.LOGIN_ACK, bb.get());
        assertEquals(42, bb.getInt());
        assertEquals(10.0f, bb.getFloat(), 0.01f);
        assertEquals(20.0f, bb.getFloat(), 0.01f);
        assertEquals(12345L, bb.getLong());
    }

    @Test
    void testCreateDisconnect() {
        byte[] data = Protocol.createDisconnect(7);
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.DISCONNECT, bb.get());
        assertEquals(7, bb.getInt());
    }

    @Test
    void testCreateSpawn() {
        byte[] data = Protocol.createSpawn(1, "Goblin", 5.0f, 10.0f, 2);
        ByteBuffer bb = ByteBuffer.wrap(data);
        assertEquals(Protocol.SPAWN, bb.get());
        assertEquals(1, bb.getInt());
        int nameLen = bb.getInt();
        byte[] nameBytes = new byte[nameLen];
        bb.get(nameBytes);
        assertEquals("Goblin", new String(nameBytes, StandardCharsets.UTF_8));
        assertEquals(5.0f, bb.getFloat(), 0.01f);
        assertEquals(10.0f, bb.getFloat(), 0.01f);
        assertEquals(2, bb.getInt());
    }
}
