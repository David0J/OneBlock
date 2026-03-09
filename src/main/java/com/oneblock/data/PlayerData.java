package com.oneblock.data;

public final class PlayerData {
    private int level;
    private int blocksBroken;

    public PlayerData(int level, int blocksBroken) {
        this.level = level;
        this.blocksBroken = blocksBroken;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }
}
