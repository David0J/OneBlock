package com.oneblock.data;

public record LevelUpdateResult(int oldLevel, int newLevel, int blocksBroken) {
    public boolean leveledUp() {
        return newLevel > oldLevel;
    }
}
