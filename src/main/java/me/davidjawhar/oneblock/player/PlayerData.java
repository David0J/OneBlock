package me.davidjawhar.oneblock.player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private int blocksBroken;
    private int level;

    public PlayerData(UUID uuid, String name, int blocksBroken, int level) {
        this.uuid = uuid;
        this.name = name;
        this.blocksBroken = blocksBroken;
        this.level = level;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public int getLevel() {
        return level;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
