package com.gougou.core.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renders the FLARE-art menu background (CC-BY-SA 3.0) with a dark vignette overlay.
 *
 * Background art from FLARE RPG — https://github.com/flareteam/flare-game
 * License: CC-BY-SA 3.0 — Copyright ©2010-2013 Clint Bellanger
 */
public class ScreenBackground {

    private final Texture background;
    private final SpriteBatch batch;
    private final Texture darkOverlay;

    public ScreenBackground() {
        background  = new Texture(Gdx.files.internal("ui/bg_menu.png"));
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // 1×1 black pixel for overlay
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(0f, 0f, 0f, 0.55f);
        px.fill();
        darkOverlay = new Texture(px);
        px.dispose();

        batch = new SpriteBatch();
    }

    /** Call once per frame, before stage.draw(). */
    public void render() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        Gdx.gl.glClearColor(OldSchoolSkin.BG.r, OldSchoolSkin.BG.g, OldSchoolSkin.BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Background image — stretch to fill screen
        batch.setColor(Color.WHITE);
        batch.draw(background, 0, 0, w, h);

        // Dark overlay for readability
        batch.setColor(0f, 0f, 0f, 0.55f);
        batch.draw(darkOverlay, 0, 0, w, h);

        batch.setColor(Color.WHITE);
        batch.end();
    }

    public void dispose() {
        background.dispose();
        darkOverlay.dispose();
        batch.dispose();
    }
}
