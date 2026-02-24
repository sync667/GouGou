package com.gougou.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.gougou.core.config.GameSettings;
import com.gougou.core.profile.ProfileManager;
import com.gougou.core.screens.MainMenuScreen;

public class GouGouGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    private GameSettings settings;
    private ProfileManager profileManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        settings = new GameSettings();
        settings.load();
        profileManager = new ProfileManager();
        profileManager.load();
        setScreen(new MainMenuScreen(this));
    }

    public GameSettings getSettings() { return settings; }
    public ProfileManager getProfileManager() { return profileManager; }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
