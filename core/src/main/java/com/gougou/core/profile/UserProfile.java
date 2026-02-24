package com.gougou.core.profile;

public class UserProfile {
    private String username;
    private String displayName;
    private int characterClass; // 0=warrior, 1=mage, 2=ranger
    private int skinColor; // 0-5 color index
    private int gamesPlayed;
    private int totalPlayTimeMinutes;
    private long createdAt;

    public UserProfile() {
        this.username = "Player";
        this.displayName = "Player";
        this.characterClass = 0;
        this.skinColor = 0;
        this.gamesPlayed = 0;
        this.totalPlayTimeMinutes = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public UserProfile(String username) {
        this();
        this.username = username;
        this.displayName = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String d) { this.displayName = d; }
    public int getCharacterClass() { return characterClass; }
    public void setCharacterClass(int c) { this.characterClass = c; }
    public int getSkinColor() { return skinColor; }
    public void setSkinColor(int c) { this.skinColor = c; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void incrementGamesPlayed() { this.gamesPlayed++; }
    public int getTotalPlayTimeMinutes() { return totalPlayTimeMinutes; }
    public void addPlayTime(int minutes) { this.totalPlayTimeMinutes += minutes; }
    public long getCreatedAt() { return createdAt; }
    public String getClassName() {
        return switch (characterClass) {
            case 1 -> "Mage";
            case 2 -> "Ranger";
            default -> "Warrior";
        };
    }
}
