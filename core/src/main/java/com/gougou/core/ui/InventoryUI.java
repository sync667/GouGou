package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;

public class InventoryUI {
    private boolean visible = false;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private int selectedSlot = 0;
    private static final int SLOTS = 20;
    private static final int COLS = 5;

    public InventoryUI() {
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
    }

    public void toggle() {
        visible = !visible;
    }

    public void render(SpriteBatch batch, List<String> items) {
        if (!visible) return;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();
        float invW = 320;
        float invH = 340;
        float invX = (screenW - invW) / 2;
        float invY = (screenH - invH) / 2;
        float slotSize = 50;
        float slotPadding = 8;

        batch.end();

        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(invX, invY, invW, invH);
        shapeRenderer.end();

        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(invX, invY, invW, invH);
        shapeRenderer.end();

        // Slots
        for (int i = 0; i < SLOTS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            float sx = invX + slotPadding + col * (slotSize + slotPadding);
            float sy = invY + invH - 50 - row * (slotSize + slotPadding);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (i == selectedSlot) {
                shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 0.8f);
            } else {
                shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 0.8f);
            }
            shapeRenderer.rect(sx, sy, slotSize, slotSize);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(i == selectedSlot ? Color.WHITE : Color.GRAY);
            shapeRenderer.rect(sx, sy, slotSize, slotSize);
            shapeRenderer.end();
        }

        batch.begin();

        // Title
        font.setColor(Color.GOLD);
        font.draw(batch, "INVENTORY", invX + invW / 2 - 40, invY + invH - 10);
        font.setColor(Color.WHITE);

        // Item names in slots
        for (int i = 0; i < Math.min(items.size(), SLOTS); i++) {
            int col = i % COLS;
            int row = i / COLS;
            float sx = invX + slotPadding + col * (slotSize + slotPadding);
            float sy = invY + invH - 50 - row * (slotSize + slotPadding);
            font.draw(batch, items.get(i).substring(0, Math.min(5, items.get(i).length())),
                    sx + 5, sy + slotSize / 2 + 5);
        }

        font.draw(batch, "Press [I] to close", invX + invW / 2 - 60, invY + 15);
    }

    public boolean isVisible() { return visible; }
    public void setSelectedSlot(int slot) { this.selectedSlot = slot; }
    public int getSelectedSlot() { return selectedSlot; }

    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
    }
}
