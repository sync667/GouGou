package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;

/**
 * Inventory window using FLARE RPG slot textures (CC-BY-SA 3.0).
 *
 * Slot art from FLARE RPG — https://github.com/flareteam/flare-game
 * License: CC-BY-SA 3.0 — Copyright ©2010-2013 Clint Bellanger
 */
public class InventoryUI {

    private boolean visible = false;

    private final Texture slotEmpty;
    private final Texture slotSelected;
    private final Texture panelBg;         // 1×1 dark pixel for panel bg
    private final BitmapFont font;
    private final FreeTypeFontGenerator gen;
    private final ShapeRenderer shape;

    private int selectedSlot = 0;
    private static final int SLOTS   = 20;
    private static final int COLS    = 5;
    private static final int SLOT_SZ = 64;  // render size
    private static final int GAP     = 8;

    public InventoryUI() {
        slotEmpty    = tex("ui/slot_empty.png");
        slotSelected = tex("ui/slot_selected.png");

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(OldSchoolSkin.PANEL_DARK);
        px.fill();
        panelBg = new Texture(px);
        px.dispose();

        shape = new ShapeRenderer();

        gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Metamorphous-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 16;
        p.color = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        font = gen.generateFont(p);
    }

    private Texture tex(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    public void toggle()          { visible = !visible; }
    public boolean isVisible()    { return visible; }
    public void setSelectedSlot(int s) { selectedSlot = s; }
    public int  getSelectedSlot() { return selectedSlot; }

    public void render(SpriteBatch batch, List<String> items) {
        if (!visible) return;

        int rows = (SLOTS + COLS - 1) / COLS;
        float invW = COLS * SLOT_SZ + (COLS + 1) * GAP;
        float invH = rows * SLOT_SZ + (rows + 1) * GAP + 60;  // +60 for header+footer
        float invX = (Gdx.graphics.getWidth()  - invW) / 2f;
        float invY = (Gdx.graphics.getHeight() - invH) / 2f;

        // ── Panel background ─────────────────────────────────────────────
        batch.setColor(OldSchoolSkin.PANEL_DARK);
        batch.draw(panelBg, invX, invY, invW, invH);
        batch.setColor(Color.WHITE);

        // ── Gold border (shape renderer) ──────────────────────────────────
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(OldSchoolSkin.GOLD);
        shape.rect(invX, invY, invW, invH);
        shape.rect(invX + 2, invY + 2, invW - 4, invH - 4);
        shape.setColor(OldSchoolSkin.GOLD_DARK);
        shape.rect(invX + 5, invY + 5, invW - 10, invH - 10);
        shape.end();
        batch.begin();

        // ── Slots ─────────────────────────────────────────────────────────
        for (int i = 0; i < SLOTS; i++) {
            int col = i % COLS;
            int row = i / COLS;
            float sx = invX + GAP + col * (SLOT_SZ + GAP);
            float sy = invY + invH - 50 - GAP - row * (SLOT_SZ + GAP) - SLOT_SZ;

            Texture slotTex = (i == selectedSlot) ? slotSelected : slotEmpty;
            batch.setColor(Color.WHITE);
            batch.draw(slotTex, sx, sy, SLOT_SZ, SLOT_SZ);

            // Item name (truncated)
            if (i < items.size()) {
                String name = items.get(i);
                String truncatedName = name.length() > 6 ? name.substring(0, 6) : name;
                font.setColor(OldSchoolSkin.PARCHMENT);
                font.draw(batch, truncatedName, sx + 4, sy + 18);
            }
        }

        // ── Title & hint ──────────────────────────────────────────────────
        font.setColor(OldSchoolSkin.GOLD_TEXT);
        font.draw(batch, "INVENTORY", invX + invW / 2f - 40, invY + invH - 14);
        font.setColor(OldSchoolSkin.GOLD_DARK);
        font.draw(batch, "Press [I] to close", invX + invW / 2f - 60, invY + 18);
        font.setColor(Color.WHITE);
    }

    public void dispose() {
        slotEmpty.dispose();
        slotSelected.dispose();
        panelBg.dispose();
        shape.dispose();
        font.dispose();
        gen.dispose();
    }
}
