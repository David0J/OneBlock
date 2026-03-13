package me.davidjawhar.oneblock.island;

import java.util.UUID;

public class MobSpawnerData {
    public enum Type {
        ANIMAL,
        HOSTILE,
        END
    }

    private final UUID owner;
    private final Type type;
    private final int x;
    private final int y;
    private final int z;
    private final String worldName;

    public MobSpawnerData(UUID owner, Type type, int x, int y, int z, String worldName) {
        this.owner = owner;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    public UUID getOwner() {
        return owner;
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isCore(int bx, int by, int bz, String world) {
        return x == bx && y == by && z == bz && worldName.equals(world);
    }

    public boolean isTop(int bx, int by, int bz, String world) {
        return x == bx && y + 1 == by && z == bz && worldName.equals(world);
    }
}
