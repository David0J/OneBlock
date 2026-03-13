package me.davidjawhar.oneblock.player;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String name;
    private int level;
    private long blocksBroken;
    private long dateCreated;
    private long lastLogin;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.level = 0;
        this.blocksBroken = 0;
        this.dateCreated = System.currentTimeMillis();
        this.lastLogin = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(long blocksBroken) { this.blocksBroken = blocksBroken; }
    public long getDateCreated() { return dateCreated; }
    public void setDateCreated(long dateCreated) { this.dateCreated = dateCreated; }
    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }
}
