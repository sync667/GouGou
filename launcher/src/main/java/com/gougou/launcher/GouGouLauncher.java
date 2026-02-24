package com.gougou.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;

/**
 * Auto-updater launcher that checks GitHub Releases for updates
 * before starting the main game. Downloads the latest release JAR
 * if a newer version is available.
 */
public class GouGouLauncher {
    private static final String GITHUB_OWNER = "sync667";
    private static final String GITHUB_REPO = "GouGou";
    private static final String GITHUB_API = "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
    private static final String VERSION_FILE = "version.properties";
    private static final String GAME_JAR = "GouGou.jar";
    private static final Gson GSON = new Gson();

    private JFrame frame;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public static void main(String[] args) {
        boolean noUpdate = false;
        for (String arg : args) {
            if ("--no-update".equals(arg)) {
                noUpdate = true;
            }
        }

        GouGouLauncher launcher = new GouGouLauncher();
        if (noUpdate) {
            launcher.launchGame();
        } else {
            launcher.run();
        }
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            createUI();
            new Thread(this::checkAndUpdate).start();
        });
    }

    private void createUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("GouGou Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 180);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("GouGou Launcher");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Checking for updates...");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(360, 25));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    private void checkAndUpdate() {
        try {
            String currentVersion = getCurrentVersion();
            updateStatus("Current version: " + currentVersion);
            Thread.sleep(500);

            updateStatus("Checking for updates...");
            ReleaseInfo latest = fetchLatestRelease();

            if (latest == null) {
                updateStatus("Could not check for updates. Starting game...");
                Thread.sleep(1000);
                launchGame();
                return;
            }

            updateStatus("Latest version: " + latest.version);
            Thread.sleep(500);

            if (isNewerVersion(currentVersion, latest.version) && latest.downloadUrl != null) {
                updateStatus("Downloading update " + latest.version + "...");
                downloadUpdate(latest.downloadUrl);
                saveVersion(latest.version);
                updateStatus("Update complete! Starting game...");
            } else {
                updateStatus("Game is up to date. Starting...");
            }

            Thread.sleep(1000);
            launchGame();
        } catch (Exception e) {
            updateStatus("Update check failed: " + e.getMessage());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            launchGame();
        }
    }

    private String getCurrentVersion() {
        try {
            Path versionPath = getAppDir().resolve(VERSION_FILE);
            if (Files.exists(versionPath)) {
                Properties props = new Properties();
                try (InputStream is = Files.newInputStream(versionPath)) {
                    props.load(is);
                }
                return props.getProperty("version", "0.0.0");
            }
        } catch (IOException ignored) {}
        return "0.0.0";
    }

    private void saveVersion(String version) {
        try {
            Path versionPath = getAppDir().resolve(VERSION_FILE);
            Properties props = new Properties();
            props.setProperty("version", version);
            try (OutputStream os = Files.newOutputStream(versionPath)) {
                props.store(os, "GouGou version");
            }
        } catch (IOException e) {
            System.err.println("Failed to save version: " + e.getMessage());
        }
    }

    private ReleaseInfo fetchLatestRelease() {
        HttpURLConnection conn = null;
        try {
            URL url = URI.create(GITHUB_API).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "GouGou-Launcher");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                ReleaseInfo info = new ReleaseInfo();
                info.version = json.get("tag_name").getAsString().replaceFirst("^v", "");

                // Find the JAR asset
                JsonArray assets = json.getAsJsonArray("assets");
                if (assets != null) {
                    for (JsonElement asset : assets) {
                        JsonObject a = asset.getAsJsonObject();
                        String name = a.get("name").getAsString();
                        if (name.endsWith(".jar")) {
                            info.downloadUrl = a.get("browser_download_url").getAsString();
                            break;
                        }
                    }
                }
                return info;
            }
        } catch (Exception e) {
            System.err.println("Release check failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static final String ALLOWED_DOWNLOAD_HOST = "github.com";
    private static final String ALLOWED_DOWNLOAD_HOST_ALT = "objects.githubusercontent.com";

    private void downloadUpdate(String urlStr) throws IOException {
        // Validate URL against allowed hosts to prevent SSRF
        URI uri = URI.create(urlStr);
        String host = uri.getHost();
        if (host == null || (!host.endsWith(ALLOWED_DOWNLOAD_HOST) && !host.endsWith(ALLOWED_DOWNLOAD_HOST_ALT))) {
            throw new IOException("Download URL not from trusted host: " + host);
        }
        if (!"https".equals(uri.getScheme())) {
            throw new IOException("Download URL must use HTTPS");
        }

        HttpURLConnection conn = null;
        try {
            URL url = uri.toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "GouGou-Launcher");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            int totalSize = conn.getContentLength();
            Path gameJar = getAppDir().resolve(GAME_JAR);

            try (InputStream is = conn.getInputStream();
                 OutputStream os = Files.newOutputStream(gameJar)) {
                byte[] buffer = new byte[8192];
                long downloaded = 0;
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;
                    if (totalSize > 0) {
                        int percent = (int) (downloaded * 100 / totalSize);
                        updateProgress(percent);
                    }
                }
            }
            updateProgress(100);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    boolean isNewerVersion(String current, String latest) {
        int[] currentParts = parseVersion(current);
        int[] latestParts = parseVersion(latest);
        for (int i = 0; i < Math.max(currentParts.length, latestParts.length); i++) {
            int c = i < currentParts.length ? currentParts[i] : 0;
            int l = i < latestParts.length ? latestParts[i] : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private int[] parseVersion(String version) {
        if (version == null || version.isEmpty()) return new int[]{0};
        String cleaned = version.replaceFirst("^v", "");
        String[] parts = cleaned.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private void launchGame() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }

        Path gameJar = getAppDir().resolve(GAME_JAR);
        if (Files.exists(gameJar)) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "java", "-jar", gameJar.toAbsolutePath().toString()
                );
                pb.inheritIO();
                pb.start();
                System.exit(0);
            } catch (IOException e) {
                System.err.println("Failed to launch game: " + e.getMessage());
                launchFromClasspath();
            }
        } else {
            launchFromClasspath();
        }
    }

    private void launchFromClasspath() {
        // Try to launch the game directly from classpath (development mode)
        try {
            String javaHome = System.getProperty("java.home");
            String classpath = System.getProperty("java.class.path");
            ProcessBuilder pb = new ProcessBuilder(
                javaHome + File.separator + "bin" + File.separator + "java",
                "-cp", classpath,
                "com.gougou.desktop.DesktopLauncher"
            );
            pb.inheritIO();
            pb.start();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Failed to launch game from classpath: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Could not start GouGou.\nPlease download the game from GitHub Releases.",
                "Launch Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private Path getAppDir() {
        Path dir = Paths.get(System.getProperty("user.home"), ".gougou");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        return dir;
    }

    private void updateStatus(String text) {
        if (statusLabel != null) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(text));
        }
        System.out.println(text);
    }

    private void updateProgress(int percent) {
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> progressBar.setValue(percent));
        }
    }

    private static class ReleaseInfo {
        String version;
        String downloadUrl;
    }
}
