package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.gougou.core.entities.Player;

/**
 * In-game HUD — renders HP/MP/XP bars using FLARE RPG art assets (CC-BY-SA 3.0).
 *
 * Bar textures from FLARE RPG — https://github.com/flareteam/flare-game
 * License: CC-BY-SA 3.0 — Copyright ©2010-2013 Clint Bellanger
 */
public class HUD {

    // ── Bar textures (FLARE CC-BY-SA 3.0) ────────────────────────────────
    private final Texture hpFill, hpBg, mpFill, mpBg, xpFill, xpBg;

    // ── Fonts ─────────────────────────────────────────────────────────────
    private final BitmapFont labelFont;
    private final FreeTypeFontGenerator fontGen;

    private boolean showFps       = true;
    private String  connStatus    = "Offline";
    private long    latency       = 0;
    private int     playerCount   = 0;

    // HP bar: 118×18 bg, 100×12 fill.  MP bar same.  XP bar: 106×10.
    private static final float PAD   = 12f;
    private static final float BAR_X = 14f;

    public HUD() {
        hpFill = load("ui/bar_hp.png");
        hpBg   = load("ui/bar_hp_bg.png");
        mpFill = load("ui/bar_mp.png");
        mpBg   = load("ui/bar_mp_bg.png");
        xpFill = load("ui/bar_xp.png");
        xpBg   = load("ui/bar_xp_bg.png");

        fontGen  = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Metamorphous-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size      = 17;
        p.color     = Color.WHITE;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        labelFont   = fontGen.generateFont(p);
    }

    private Texture load(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    // ─────────────────────────────────────────────────────────────────────

    public void render(SpriteBatch batch, Player player) {
        if (player == null) return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        // ── HP bar ───────────────────────────────────────────────────────
        float hpRatio = clamp((float) player.getHealth() / player.getMaxHealth());
        float hpBgW = hpBg.getWidth(), hpBgH = hpBg.getHeight();
        float hpFillW = hpFill.getWidth(), hpFillH = hpFill.getHeight();
        float hpY = sh - PAD - hpBgH;

        batch.setColor(Color.WHITE);
        batch.draw(hpBg, BAR_X, hpY, hpBgW * 2, hpBgH * 2);
        // Clip fill by ratio — draw sub-region of the fill texture
        batch.draw(hpFill,
                BAR_X + (hpBgW - hpFillW),        // x offset to centre within bg
                hpY + (hpBgH - hpFillH),           // y offset
                hpFillW * 2 * hpRatio, hpFillH * 2,
                0, 0, (int)(hpFill.getWidth() * hpRatio), hpFill.getHeight(),
                false, false);

        // ── MP bar ───────────────────────────────────────────────────────
        float mpRatio = clamp((float) player.getMana() / player.getMaxMana());
        float mpBgH = mpBg.getHeight();
        float mpY = hpY - PAD - mpBgH * 2;

        batch.draw(mpBg, BAR_X, mpY, mpBg.getWidth() * 2, mpBgH * 2);
        batch.draw(mpFill,
                BAR_X + (mpBg.getWidth() - mpFill.getWidth()),
                mpY + (mpBgH - mpFill.getHeight()),
                mpFill.getWidth() * 2 * mpRatio, mpFill.getHeight() * 2,
                0, 0, (int)(mpFill.getWidth() * mpRatio), mpFill.getHeight(),
                false, false);

        // ── XP bar ───────────────────────────────────────────────────────
        // XP bar — show full bar when at max level (expForNextLevel == 0)
        long expNeeded = player.getExpForNextLevel();
        float xpRatio = (expNeeded <= 0) ? 1f : clamp((float) player.getExperience() / expNeeded);
        float xpY = mpY - PAD * 0.6f - xpBg.getHeight() * 1.5f;

        batch.draw(xpBg, BAR_X, xpY, xpBg.getWidth() * 2, xpBg.getHeight() * 1.5f);
        batch.draw(xpFill,
                BAR_X, xpY,
                xpFill.getWidth() * 2 * xpRatio, xpFill.getHeight() * 1.5f,
                0, 0, (int)(xpFill.getWidth() * xpRatio), xpFill.getHeight(),
                false, false);
        batch.setColor(Color.WHITE);

        // ── Labels ───────────────────────────────────────────────────────
        labelFont.setColor(OldSchoolSkin.PARCHMENT);
        labelFont.draw(batch,
                "HP " + player.getHealth() + "/" + player.getMaxHealth(),
                BAR_X + hpBgW * 2 + 8, hpY + hpBgH * 2 - 4);
        labelFont.draw(batch,
                "MP " + player.getMana() + "/" + player.getMaxMana(),
                BAR_X + mpBg.getWidth() * 2 + 8, mpY + mpBgH * 2 - 4);

        labelFont.setColor(OldSchoolSkin.GOLD_TEXT);
        labelFont.draw(batch,
                "Lv." + player.getLevel() + "  " + player.getName(),
                BAR_X, xpY - 4);

        // ── Connection info (top-right) ───────────────────────────────────
        labelFont.setColor(OldSchoolSkin.GOLD_DARK);
        String conn = connStatus;
        if (latency > 0)      conn += "  |  " + latency + "ms";
        if (playerCount > 0)  conn += "  |  " + playerCount + " online";
        labelFont.draw(batch, conn, sw - 280, sh - PAD);

        if (showFps) {
            labelFont.setColor(OldSchoolSkin.PARCHMENT);
            labelFont.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), sw - 90, sh - PAD - 22);
        }

        // ── Misc overlays ─────────────────────────────────────────────────
        if (player.isSwimming()) {
            labelFont.setColor(Color.CYAN);
            labelFont.draw(batch, "~ Swimming ~", BAR_X, PAD + 50);
        }

        labelFont.setColor(OldSchoolSkin.GOLD_DARK);
        labelFont.draw(batch, String.format("X:%.0f  Y:%.0f", player.getX(), player.getY()),
                BAR_X, PAD + 24);

        labelFont.setColor(new Color(0.7f, 0.65f, 0.5f, 0.85f));
        labelFont.draw(batch, "[T] Chat   [I] Inventory   [M] Map   [ESC] Menu",
                sw / 2f - 180, PAD + 6);

        labelFont.setColor(Color.WHITE);
    }

    private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }

    public void setShowFps(boolean v)        { showFps = v; }
    public void setConnectionStatus(String s){ connStatus = s; }
    public void setLatency(long l)           { latency = l; }
    public void setPlayerCount(int c)        { playerCount = c; }

    public void dispose() {
        hpFill.dispose(); hpBg.dispose();
        mpFill.dispose(); mpBg.dispose();
        xpFill.dispose(); xpBg.dispose();
        labelFont.dispose();
        fontGen.dispose();
    }
}
