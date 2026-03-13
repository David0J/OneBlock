package me.davidjawhar.oneblock.island;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IslandData {

    private final UUID owner;
    private final int index;
    private final int x;
    private final int y;
    private final int z;
    private final String worldName;
    private final Set<UUID> trusted = new HashSet<>();

    public IslandData(UUID owner, int index, int x, int y, int z, String worldName) {
        this.owner = owner;
        this.index = index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getIndex() {
        return index;
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

    public Set<UUID> getTrusted() {
        return trusted;
    }

    public void trust(UUID uuid) {
        trusted.add(uuid);
    }

    public void untrust(UUID uuid) {
        trusted.remove(uuid);
    }

    public boolean isTrusted(UUID uuid) {
        return owner.equals(uuid) || trusted.contains(uuid);
    }

    public boolean isOneBlock(Location location) {
        if (location == null || location.getWorld() == null) return false;
        if (!location.getWorld().getName().equals(worldName)) return false;

        return location.getBlockX() == x
                && location.getBlockY() == y
                && location.getBlockZ() == z;
    }
}
