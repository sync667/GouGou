package com.gougou.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.gougou.core.GouGouGame;
import com.gougou.core.config.GameSettings;

public class DesktopLauncher {
    public static void main(String[] args) {
        GameSettings settings = new GameSettings();
        settings.load();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("GouGou - 2D Multiplayer Adventure");
        config.setWindowedMode(settings.getResolutionWidth(), settings.getResolutionHeight());
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowIcon("sprites/Sprite.png");

        if (settings.isFullscreen()) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        }

        new Lwjgl3Application(new GouGouGame(), config);
    }
}
