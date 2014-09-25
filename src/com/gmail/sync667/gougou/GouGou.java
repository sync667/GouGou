package com.gmail.sync667.gougou;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.gmail.sync667.gougou.entities.player.Player;
import com.gmail.sync667.gougou.gfx.Screen;
import com.gmail.sync667.gougou.gfx.SpriteSheet;
import com.gmail.sync667.gougou.level.Level;
import com.gmail.sync667.gougou.net.GouGouClient;
import com.gmail.sync667.gougou.net.packets.Packet00HandShakeClient;
import com.gmail.sync667.gougou.net.packets.Packet04Disconnect;

public class GouGou extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    public static final int WIDTH = 160;
    public static final int HEIGHT = WIDTH / 12 * 9;
    public static final int SCALE = 3;
    public static final String NAME = "GouGou";
    public static final String VERSION = "ALPHA-0.1 Build 8";
    public static final int PROTOCOL_VERSION = 1;

    private final JFrame frame;

    public boolean running = false;
    public int ticksCount = 0;

    private final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    private final int[] colours = new int[6 * 6 * 6];

    private Screen screen;
    public InputHandler input;

    private static GouGouClient gougouClient;

    public String username;
    public InetAddress serverIp;
    public int port;
    public Level level;
    public static Player player;

    public GouGou() {
        setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

        frame = new JFrame(NAME);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(this, BorderLayout.CENTER);
        frame.pack();

        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void init() {
        int index = 0;
        for (int r = 0; r < 6; r++) {
            for (int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    int rr = (r * 255 / 5);
                    int gg = (g * 255 / 5);
                    int bb = (b * 255 / 5);

                    colours[index++] = rr << 16 | gg << 8 | bb;
                }
            }
        }

        screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/Sprite.png"));
        input = new InputHandler(this);
        level = new Level("/levels/level1.png");

        username = JOptionPane.showInputDialog(this, "Type your username!");
        String inputServerIp = JOptionPane.showInputDialog(this, "Type server ip address!");

        String[] iSIP = inputServerIp.split(":");
        try {
            serverIp = InetAddress.getByName(iSIP[0]);
        } catch (UnknownHostException e) {
            inputServerIp = JOptionPane.showInputDialog(this, "Error! Type server ip address again!");
        }

        port = Short.valueOf(iSIP[1]);

        gougouClient = new GouGouClient(this, serverIp, port);
        gougouClient.start();

        gougouClient.sendData(new Packet00HandShakeClient(PROTOCOL_VERSION, (short) 1).getData());
    }

    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }

    public synchronized void stop() {
        gougouClient.sendData(new Packet04Disconnect(player.entityId, null).getData());
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000D / 60D;

        int ticks = 0;
        int frames = 0;

        long lastTimer = System.currentTimeMillis();
        double delta = 0;

        init();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            boolean shouldRender = true;

            while (delta >= 1) {
                ticks++;
                tick();
                delta -= 1;
                shouldRender = true;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (shouldRender) {
                frames++;
                render();
            }

            if (System.currentTimeMillis() - lastTimer >= 1000) {
                lastTimer += 1000;
                frame.setTitle(ticks + " ticks, " + frames + " frames"); // temp debuging info,
                                                                         // should be rendered on
                                                                         // game screen
                frames = 0;
                ticks = 0;
            }
        }
    }

    public void tick() {
        ticksCount++;
        level.tick();
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        int xOffset;
        int yOffset;

        if (player == null) {
            xOffset = 0 - (screen.width / 2);
            yOffset = 0 - (screen.height / 2);
        } else {
            xOffset = player.x - (screen.width / 2);
            yOffset = player.y - (screen.height / 2);
        }
        level.renderTiles(screen, xOffset, yOffset);
        level.renderEntities(screen);

        for (int y = 0; y < screen.height; y++) {
            for (int x = 0; x < screen.width; x++) {
                int colourCode = screen.pixels[x + y * screen.width];
                if (colourCode < 255) {
                    pixels[x + y * WIDTH] = colours[colourCode];
                }
            }
        }

        Graphics g = bs.getDrawGraphics();
        g.drawRect(0, 0, getWidth(), getHeight());
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();

    }

    public static GouGouClient getClient() {
        return gougouClient;
    }

    public static void main(String[] args) {
        new GouGou().start();
    }

}
