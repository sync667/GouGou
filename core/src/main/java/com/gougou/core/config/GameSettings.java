package com.gougou.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class GameSettings {
    private int resolutionWidth = 1280;
    private int resolutionHeight = 720;
    private boolean fullscreen = false;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;
    private boolean showFps = true;
    private int viewDistance = 10;
    private int serverPort = 7777;
    private String playerName = "Player";
    
    private static final String SETTINGS_FILE = "settings.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public void load() {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".gougou", SETTINGS_FILE);
            if (Files.exists(path)) {
                String json = Files.readString(path);
                GameSettings loaded = GSON.fromJson(json, GameSettings.class);
                if (loaded != null) {
                    this.resolutionWidth = loaded.resolutionWidth;
                    this.resolutionHeight = loaded.resolutionHeight;
                    this.fullscreen = loaded.fullscreen;
                    this.musicVolume = loaded.musicVolume;
                    this.sfxVolume = loaded.sfxVolume;
                    this.showFps = loaded.showFps;
                    this.viewDistance = loaded.viewDistance;
                    this.serverPort = loaded.serverPort;
                    this.playerName = loaded.playerName;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load settings: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".gougou");
            Files.createDirectories(dir);
            Path path = dir.resolve(SETTINGS_FILE);
            Files.writeString(path, GSON.toJson(this));
        } catch (Exception e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }

    public int getResolutionWidth() { return resolutionWidth; }
    public void setResolutionWidth(int w) { this.resolutionWidth = w; }
    public int getResolutionHeight() { return resolutionHeight; }
    public void setResolutionHeight(int h) { this.resolutionHeight = h; }
    public boolean isFullscreen() { return fullscreen; }
    public void setFullscreen(boolean f) { this.fullscreen = f; }
    public float getMusicVolume() { return musicVolume; }
    public void setMusicVolume(float v) { this.musicVolume = v; }
    public float getSfxVolume() { return sfxVolume; }
    public void setSfxVolume(float v) { this.sfxVolume = v; }
    public boolean isShowFps() { return showFps; }
    public void setShowFps(boolean s) { this.showFps = s; }
    public int getViewDistance() { return viewDistance; }
    public void setViewDistance(int d) { this.viewDistance = d; }
    public int getServerPort() { return serverPort; }
    public void setServerPort(int p) { this.serverPort = p; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String n) { this.playerName = n; }
}
