package com.gougou.core.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class ProfileManager {
    private UserProfile currentProfile;
    private static final String PROFILE_FILE = "profile.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ProfileManager() {
        this.currentProfile = new UserProfile();
    }

    public void load() {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".gougou", PROFILE_FILE);
            if (Files.exists(path)) {
                String json = Files.readString(path);
                UserProfile loaded = GSON.fromJson(json, UserProfile.class);
                if (loaded != null) {
                    currentProfile = loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load profile: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".gougou");
            Files.createDirectories(dir);
            Path path = dir.resolve(PROFILE_FILE);
            Files.writeString(path, GSON.toJson(currentProfile));
        } catch (Exception e) {
            System.err.println("Failed to save profile: " + e.getMessage());
        }
    }

    public UserProfile getCurrentProfile() { return currentProfile; }
    public void setCurrentProfile(UserProfile profile) { this.currentProfile = profile; }

    public boolean hasProfile() {
        Path path = Paths.get(System.getProperty("user.home"), ".gougou", PROFILE_FILE);
        return Files.exists(path);
    }
}
