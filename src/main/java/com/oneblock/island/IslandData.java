package com.oneblock.island;

import org.bukkit.Location;

import java.util.UUID;

public final class IslandData {
    private final UUID owner;
    private final Location blockLocation;

    public IslandData(UUID owner, Location blockLocation) {
        this.owner = owner;
        this.blockLocation = blockLocation;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getBlockLocation() {
        return blockLocation.clone();
    }

    public Location getTeleportLocation() {
        return blockLocation.clone().add(0.5, 1.0, 0.5);
    }
}
