package me.davidjawhar.oneblock.island;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class IslandData {
    private final UUID uuid;
    private final int index;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public IslandData(UUID uuid, int index, String worldName, int x, int y, int z) {
        this.uuid = uuid;
        this.index = index;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public UUID getUuid() { return uuid; }
    public int getIndex() { return index; }
    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    public Location getBaseLocation(World world) {
        return new Location(world, x, y, z);
    }

    public Location getTeleportLocation(World world) {
        return new Location(world, x + 0.5, y + 1.0, z + 0.5);
    }
}
