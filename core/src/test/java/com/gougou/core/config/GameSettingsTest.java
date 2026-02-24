package com.gougou.core.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameSettingsTest {

    @Test
    void testDefaultSettings() {
        GameSettings settings = new GameSettings();
        assertEquals(1280, settings.getResolutionWidth());
        assertEquals(720, settings.getResolutionHeight());
        assertFalse(settings.isFullscreen());
        assertEquals(0.7f, settings.getMusicVolume(), 0.01f);
        assertEquals(0.8f, settings.getSfxVolume(), 0.01f);
        assertTrue(settings.isShowFps());
        assertEquals(10, settings.getViewDistance());
        assertEquals(7777, settings.getServerPort());
        assertEquals("Player", settings.getPlayerName());
    }

    @Test
    void testSetAndGet() {
        GameSettings settings = new GameSettings();
        settings.setResolutionWidth(1920);
        settings.setResolutionHeight(1080);
        settings.setFullscreen(true);
        settings.setMusicVolume(0.5f);
        settings.setSfxVolume(0.3f);
        settings.setShowFps(false);
        settings.setViewDistance(15);
        settings.setServerPort(8888);
        settings.setPlayerName("Hero");

        assertEquals(1920, settings.getResolutionWidth());
        assertEquals(1080, settings.getResolutionHeight());
        assertTrue(settings.isFullscreen());
        assertEquals(0.5f, settings.getMusicVolume(), 0.01f);
        assertEquals(0.3f, settings.getSfxVolume(), 0.01f);
        assertFalse(settings.isShowFps());
        assertEquals(15, settings.getViewDistance());
        assertEquals(8888, settings.getServerPort());
        assertEquals("Hero", settings.getPlayerName());
    }
}
