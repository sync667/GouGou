package com.gougou.launcher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GouGouLauncherTest {

    private final GouGouLauncher launcher = new GouGouLauncher();

    @Test
    void testNewerVersion() {
        assertTrue(launcher.isNewerVersion("1.0.0", "1.0.1"));
        assertTrue(launcher.isNewerVersion("1.0.0", "1.1.0"));
        assertTrue(launcher.isNewerVersion("1.0.0", "2.0.0"));
    }

    @Test
    void testSameVersion() {
        assertFalse(launcher.isNewerVersion("1.0.0", "1.0.0"));
        assertFalse(launcher.isNewerVersion("2.1.3", "2.1.3"));
    }

    @Test
    void testOlderVersion() {
        assertFalse(launcher.isNewerVersion("1.1.0", "1.0.0"));
        assertFalse(launcher.isNewerVersion("2.0.0", "1.9.9"));
    }

    @Test
    void testVersionWithPrefix() {
        assertTrue(launcher.isNewerVersion("1.0.0", "v2.0.0"));
        assertTrue(launcher.isNewerVersion("v1.0.0", "2.0.0"));
    }

    @Test
    void testDifferentLengthVersions() {
        assertTrue(launcher.isNewerVersion("1.0", "1.0.1"));
        assertFalse(launcher.isNewerVersion("1.0.1", "1.0"));
    }

    @Test
    void testEmptyVersion() {
        assertTrue(launcher.isNewerVersion("", "1.0.0"));
        assertTrue(launcher.isNewerVersion("0.0.0", "1.0.0"));
    }
}
