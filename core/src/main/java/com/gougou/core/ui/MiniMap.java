package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.gougou.core.entities.Entity;
import com.gougou.core.entities.Player;
import com.gougou.core.world.TileType;
import com.gougou.core.world.World;
import java.util.Collection;

public class MiniMap {
    private boolean visible = true;
    private Texture mapTexture;
    private final ShapeRenderer shapeRenderer;
    private static final int MAP_SIZE = 160;
    private static final int VIEW_RADIUS = 40;

    public MiniMap() {
        shapeRenderer = new ShapeRenderer();
    }

    public void generateTexture(World world) {
        if (mapTexture != null) mapTexture.dispose();

        int w = world.getWidth();
        int h = world.getHeight();
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                TileType tile = world.getTile(x, y);
                Color c = tile.getColor();
                pixmap.setColor(c);
                pixmap.drawPixel(x, h - 1 - y);
            }
        }

        mapTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void render(SpriteBatch batch, Player player, Collection<Entity> entities) {
        if (!visible || mapTexture == null || player == null) return;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float mapX = screenW - MAP_SIZE - 10;
        float mapY = screenH - MAP_SIZE - 10;

        // Draw map texture (showing area around player)
        float playerRatioX = player.getX() / mapTexture.getWidth();
        float playerRatioY = player.getY() / mapTexture.getHeight();
        float viewRatioW = (float) VIEW_RADIUS * 2 / mapTexture.getWidth();
        float viewRatioH = (float) VIEW_RADIUS * 2 / mapTexture.getHeight();

        float srcX = Math.max(0, playerRatioX - viewRatioW / 2) * mapTexture.getWidth();
        float srcY = Math.max(0, (1 - playerRatioY) - viewRatioH / 2) * mapTexture.getHeight();
        float srcW = viewRatioW * mapTexture.getWidth();
        float srcH = viewRatioH * mapTexture.getHeight();

        // Clamp
        srcX = Math.max(0, Math.min(mapTexture.getWidth() - srcW, srcX));
        srcY = Math.max(0, Math.min(mapTexture.getHeight() - srcH, srcY));

        batch.draw(mapTexture, mapX, mapY, MAP_SIZE, MAP_SIZE,
                (int) srcX, (int) srcY, (int) srcW, (int) srcH, false, false);

        batch.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(mapX, mapY, MAP_SIZE, MAP_SIZE);
        shapeRenderer.end();

        // Player dot (center of minimap)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(mapX + MAP_SIZE / 2f, mapY + MAP_SIZE / 2f, 3);

        // Other entities
        if (entities != null) {
            for (Entity e : entities) {
                if (e == player) continue;
                float relX = (e.getX() - player.getX()) / (VIEW_RADIUS * 2) * MAP_SIZE + MAP_SIZE / 2f;
                float relY = (e.getY() - player.getY()) / (VIEW_RADIUS * 2) * MAP_SIZE + MAP_SIZE / 2f;
                if (relX >= 0 && relX <= MAP_SIZE && relY >= 0 && relY <= MAP_SIZE) {
                    shapeRenderer.setColor(e instanceof Player ? Color.GREEN : Color.YELLOW);
                    shapeRenderer.circle(mapX + relX, mapY + relY, 2);
                }
            }
        }
        shapeRenderer.end();

        batch.begin();
    }

    public void toggle() { visible = !visible; }
    public boolean isVisible() { return visible; }

    public void dispose() {
        if (mapTexture != null) mapTexture.dispose();
        shapeRenderer.dispose();
    }
}
