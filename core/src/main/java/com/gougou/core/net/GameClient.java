package com.gougou.core.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GameClient {
    private EventLoopGroup group;
    private Channel channel;
    private boolean connected;
    private int entityId = -1;
    private long latency;
    private final List<ClientListener> listeners = new CopyOnWriteArrayList<>();

    public interface ClientListener {
        default void onConnected(int entityId, float spawnX, float spawnY, long worldSeed) {}
        default void onDisconnected(String reason) {}
        default void onEntitySpawn(int entityId, String name, float x, float y, int type) {}
        default void onEntityDespawn(int entityId) {}
        default void onEntityMove(int entityId, float x, float y, int direction) {}
        default void onChatMessage(int entityId, String message) {}
        default void onPong(long latency) {}
        default void onPlayerState(int entityId, int health, int maxHealth, int mana, int maxMana,
                                    int level, float x, float y, int direction, boolean swimming) {}
        default void onInventoryUpdate(int entityId, String[] items) {}
    }

    public void addListener(ClientListener listener) { listeners.add(listener); }
    public void removeListener(ClientListener listener) { listeners.remove(listener); }

    public void connect(String host, int port, String username, int characterClass, int skinColor) throws IOException {
        group = new NioEventLoopGroup(1);
        CountDownLatch loginLatch = new CountDownLatch(1);
        final IOException[] connectError = {null};

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(Protocol.MAX_PACKET_SIZE, 0, 4, 0, 4),
                            new LengthFieldPrepender(4),
                            new ClientHandler(username, characterClass, skinColor, loginLatch, connectError)
                        );
                    }
                });

            ChannelFuture future = bootstrap.connect(host, port).sync();
            channel = future.channel();

            // Wait for login handshake to complete
            if (!loginLatch.await(10, TimeUnit.SECONDS)) {
                throw new IOException("Login timed out");
            }
            if (connectError[0] != null) {
                throw connectError[0];
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Connection interrupted", e);
        } catch (IOException e) {
            shutdown();
            throw e;
        } catch (Exception e) {
            shutdown();
            throw new IOException("Connection failed: " + e.getMessage(), e);
        }
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter {
        private final String username;
        private final int characterClass;
        private final int skinColor;
        private final CountDownLatch loginLatch;
        private final IOException[] connectError;

        ClientHandler(String username, int characterClass, int skinColor,
                      CountDownLatch loginLatch, IOException[] connectError) {
            this.username = username;
            this.characterClass = characterClass;
            this.skinColor = skinColor;
            this.loginLatch = loginLatch;
            this.connectError = connectError;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // Send handshake on connect
            send(Protocol.createHandshake());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf in = (ByteBuf) msg;
            try {
                if (in.readableBytes() < 1) return;
                byte type = in.readByte();
                switch (type) {
                    case Protocol.HANDSHAKE_ACK -> {
                        boolean accepted = in.readBoolean();
                        byte[] msgBytes = new byte[in.readableBytes()];
                        in.readBytes(msgBytes);
                        String message = new String(msgBytes, StandardCharsets.UTF_8);
                        if (accepted) {
                            send(Protocol.createLogin(username, characterClass, skinColor));
                        } else {
                            connectError[0] = new IOException("Server rejected: " + message);
                            loginLatch.countDown();
                        }
                    }
                    case Protocol.LOGIN_ACK -> {
                        entityId = in.readInt();
                        float spawnX = in.readFloat();
                        float spawnY = in.readFloat();
                        long worldSeed = in.readLong();
                        connected = true;
                        loginLatch.countDown();
                        for (ClientListener l : listeners) l.onConnected(entityId, spawnX, spawnY, worldSeed);
                    }
                    case Protocol.SPAWN -> {
                        int id = in.readInt();
                        int nameLen = Protocol.validateReadLength(in.readInt(), in.readableBytes(), Protocol.MAX_NAME_LENGTH);
                        if (nameLen < 0) return;
                        byte[] nameBytes = new byte[nameLen];
                        in.readBytes(nameBytes);
                        String name = new String(nameBytes, StandardCharsets.UTF_8);
                        float x = in.readFloat();
                        float y = in.readFloat();
                        int entityType = in.readInt();
                        for (ClientListener l : listeners) l.onEntitySpawn(id, name, x, y, entityType);
                    }
                    case Protocol.DESPAWN -> {
                        int id = in.readInt();
                        for (ClientListener l : listeners) l.onEntityDespawn(id);
                    }
                    case Protocol.MOVE -> {
                        int id = in.readInt();
                        float x = in.readFloat();
                        float y = in.readFloat();
                        int dir = in.readInt();
                        for (ClientListener l : listeners) l.onEntityMove(id, x, y, dir);
                    }
                    case Protocol.CHAT -> {
                        int id = in.readInt();
                        int msgLen = Protocol.validateReadLength(in.readInt(), in.readableBytes(), Protocol.MAX_CHAT_LENGTH);
                        if (msgLen < 0) return;
                        byte[] chatBytes = new byte[msgLen];
                        in.readBytes(chatBytes);
                        String chatMsg = new String(chatBytes, StandardCharsets.UTF_8);
                        for (ClientListener l : listeners) l.onChatMessage(id, chatMsg);
                    }
                    case Protocol.PONG -> {
                        long timestamp = in.readLong();
                        latency = System.currentTimeMillis() - timestamp;
                        for (ClientListener l : listeners) l.onPong(latency);
                    }
                    case Protocol.PLAYER_STATE -> {
                        int id = in.readInt();
                        int health = in.readInt();
                        int maxHealth = in.readInt();
                        int mana = in.readInt();
                        int maxMana = in.readInt();
                        int level = in.readInt();
                        float x = in.readFloat();
                        float y = in.readFloat();
                        int dir = in.readInt();
                        boolean swimming = in.readBoolean();
                        for (ClientListener l : listeners)
                            l.onPlayerState(id, health, maxHealth, mana, maxMana, level, x, y, dir, swimming);
                    }
                    case Protocol.INVENTORY_UPDATE -> {
                        int id = in.readInt();
                        int count = in.readInt();
                        if (count < 0 || count > 100) return;
                        String[] items = new String[count];
                        for (int i = 0; i < count; i++) {
                            int itemLen = Protocol.validateReadLength(in.readInt(), in.readableBytes(), Protocol.MAX_ITEM_NAME_LENGTH);
                            if (itemLen < 0) return;
                            byte[] itemBytes = new byte[itemLen];
                            in.readBytes(itemBytes);
                            items[i] = new String(itemBytes, StandardCharsets.UTF_8);
                        }
                        for (ClientListener l : listeners) l.onInventoryUpdate(id, items);
                    }
                    case Protocol.DISCONNECT -> {
                        connected = false;
                        for (ClientListener l : listeners) l.onDisconnected("Server closed");
                    }
                }
            } finally {
                in.release();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (connected) {
                connected = false;
                for (ClientListener l : listeners) l.onDisconnected("Connection lost");
            }
            loginLatch.countDown();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("Client error: " + cause.getMessage());
            ctx.close();
        }
    }

    public void sendMoveInput(boolean up, boolean down, boolean left, boolean right) {
        if (connected) send(Protocol.createMoveInput(up, down, left, right));
    }

    public void sendChat(String message) {
        if (connected) send(Protocol.createChat(entityId, message));
    }

    public void sendPing() {
        if (connected) send(Protocol.createPing(System.currentTimeMillis()));
    }

    public void disconnect() {
        if (connected) {
            send(Protocol.createDisconnect(entityId));
            connected = false;
        }
        shutdown();
    }

    private void send(byte[] data) {
        if (channel != null && channel.isActive()) {
            ByteBuf buf = Unpooled.wrappedBuffer(data);
            channel.writeAndFlush(buf);
        }
    }

    private void shutdown() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public boolean isConnected() { return connected; }
    public int getEntityId() { return entityId; }
    public long getLatency() { return latency; }

    public static List<String> discoverLANServers(int timeoutMs) {
        List<String> servers = new ArrayList<>();
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.setBroadcast(true);
            ds.setSoTimeout(timeoutMs);
            byte[] request = Protocol.DISCOVERY_REQUEST;
            DatagramPacket packet = new DatagramPacket(request, request.length,
                InetAddress.getByName("255.255.255.255"), Protocol.DISCOVERY_PORT);
            ds.send(packet);

            byte[] buf = new byte[256];
            long end = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < end) {
                try {
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    ds.receive(response);
                    String data = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                    String prefix = new String(Protocol.DISCOVERY_RESPONSE_PREFIX, StandardCharsets.UTF_8);
                    if (data.startsWith(prefix)) {
                        servers.add(response.getAddress().getHostAddress() + ":" + data.substring(prefix.length()));
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("LAN discovery failed: " + e.getMessage());
        }
        return servers;
    }
}
