package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gougou.core.entities.Player;

public class HUD {
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private boolean showFps = true;
    private String connectionStatus = "Offline";
    private long latency = 0;
    private int playerCount = 0;

    private static final float BAR_WIDTH = 200;
    private static final float BAR_HEIGHT = 20;
    private static final float PADDING = 10;

    public HUD() {
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
    }

    public void render(SpriteBatch batch, Player player) {
        if (player == null) return;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        // End any active batch to draw shapes
        batch.end();

        // Health bar
        drawBar(PADDING, screenH - PADDING - BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT,
                (float) player.getHealth() / player.getMaxHealth(),
                new Color(0.8f, 0.1f, 0.1f, 0.8f), new Color(0.3f, 0.0f, 0.0f, 0.8f));

        // Mana bar
        drawBar(PADDING, screenH - PADDING * 2 - BAR_HEIGHT * 2, BAR_WIDTH, BAR_HEIGHT,
                (float) player.getMana() / player.getMaxMana(),
                new Color(0.1f, 0.2f, 0.9f, 0.8f), new Color(0.0f, 0.0f, 0.3f, 0.8f));

        // Experience bar
        drawBar(PADDING, screenH - PADDING * 3 - BAR_HEIGHT * 3, BAR_WIDTH, BAR_HEIGHT * 0.6f,
                (float) player.getExperience() / player.getExpForNextLevel(),
                new Color(0.1f, 0.8f, 0.1f, 0.8f), new Color(0.0f, 0.3f, 0.0f, 0.8f));

        batch.begin();

        // Health text
        font.draw(batch, "HP: " + player.getHealth() + "/" + player.getMaxHealth(),
                PADDING + 5, screenH - PADDING - 3);

        // Mana text
        font.draw(batch, "MP: " + player.getMana() + "/" + player.getMaxMana(),
                PADDING + 5, screenH - PADDING * 2 - BAR_HEIGHT - 3);

        // Level
        font.draw(batch, "Lv." + player.getLevel() + " " + player.getName(),
                PADDING, screenH - PADDING * 3 - BAR_HEIGHT * 3 + 2);

        // Connection info (top right)
        String connInfo = connectionStatus;
        if (latency > 0) connInfo += " | " + latency + "ms";
        if (playerCount > 0) connInfo += " | " + playerCount + " players";
        font.draw(batch, connInfo, screenW - 250, screenH - PADDING);

        // FPS (top right corner)
        if (showFps) {
            font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(),
                    screenW - 100, screenH - PADDING - 20);
        }

        // Swimming indicator
        if (player.isSwimming()) {
            font.setColor(Color.CYAN);
            font.draw(batch, "~ Swimming ~", PADDING, PADDING + 40);
            font.setColor(Color.WHITE);
        }

        // Position
        font.draw(batch, String.format("X:%.0f Y:%.0f", player.getX(), player.getY()),
                PADDING, PADDING + 20);

        // Controls hint
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "[T]Chat [I]Inventory [M]Map [ESC]Menu", screenW / 2 - 150, PADDING + 5);
        font.setColor(Color.WHITE);
    }

    private void drawBar(float x, float y, float width, float height, float fill, Color fillColor, Color bgColor) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Background
        shapeRenderer.setColor(bgColor);
        shapeRenderer.rect(x, y, width, height);
        // Fill
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, y, width * Math.max(0, Math.min(1, fill)), height);
        shapeRenderer.end();
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
    }

    public void setShowFps(boolean show) { this.showFps = show; }
    public void setConnectionStatus(String status) { this.connectionStatus = status; }
    public void setLatency(long l) { this.latency = l; }
    public void setPlayerCount(int c) { this.playerCount = c; }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
