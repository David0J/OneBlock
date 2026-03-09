package com.oneblock.island;

import com.oneblock.OneBlockPlugin;
import com.oneblock.data.PlayerDataManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class IslandManager {

    private final OneBlockPlugin plugin;
    private final Map<UUID, IslandData> islands = new HashMap<>();
    private final int spacing;
    private final int y;
    private int nextIslandIndex;

    public IslandManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.spacing = plugin.getConfig().getInt("world.spacing", 1000);
        this.y = plugin.getConfig().getInt("world.spawn-y", 64);
    }

    public void loadFromData() {
        PlayerDataManager data = plugin.getPlayerDataManager();
        islands.clear();
        int highestIndex = -1;

        for (UUID uuid : data.getKnownPlayers()) {
            Integer index = data.getIslandIndex(uuid);
            Integer x = data.getIslandX(uuid);
            Integer z = data.getIslandZ(uuid);
            if (index == null || x == null || z == null) {
                continue;
            }
            Location blockLocation = new Location(plugin.getOneBlockWorld(), x, y, z);
            islands.put(uuid, new IslandData(uuid, blockLocation));
            highestIndex = Math.max(highestIndex, index);
        }

        nextIslandIndex = highestIndex + 1;
    }

    public boolean hasIsland(UUID uuid) {
        return islands.containsKey(uuid);
    }

    public Optional<IslandData> getIsland(UUID uuid) {
        return Optional.ofNullable(islands.get(uuid));
    }

    public IslandData createIsland(Player player) {
        int index = nextIslandIndex++;
        int[] coords = indexToGrid(index);
        int x = coords[0];
        int z = coords[1];

        Location blockLocation = new Location(plugin.getOneBlockWorld(), x, y, z);
        placeInitialBlocks(blockLocation);

        IslandData islandData = new IslandData(player.getUniqueId(), blockLocation);
        islands.put(player.getUniqueId(), islandData);

        plugin.getPlayerDataManager().createOrResetPlayer(player, index, x, y, z);

        return islandData;
    }

    public void resetIsland(Player player) {
        IslandData island = islands.remove(player.getUniqueId());
        if (island != null) {
            clearIsland(island.getBlockLocation());
        }
        IslandData created = createIsland(player);
        player.teleport(created.getTeleportLocation());
    }

    public void placeInitialBlocks(Location blockLocation) {
        World world = blockLocation.getWorld();
        if (world == null) {
            throw new IllegalStateException("OneBlock world missing");
        }

        world.getBlockAt(blockLocation.clone().add(0, -1, 0)).setType(Material.BEDROCK, false);
        world.getBlockAt(blockLocation).setType(Material.DIRT, false);
    }

    public void clearIsland(Location blockLocation) {
        World world = blockLocation.getWorld();
        if (world == null) {
            return;
        }
        world.getBlockAt(blockLocation).setType(Material.AIR, false);
        world.getBlockAt(blockLocation.clone().add(0, -1, 0)).setType(Material.AIR, false);
    }

    public int getSpacing() {
        return spacing;
    }

    private int[] indexToGrid(int index) {
        if (index == 0) {
            return new int[]{0, 0};
        }

        int layer = 1;
        int remaining = index;
        while ((2 * layer + 1) * (2 * layer + 1) <= index) {
            layer++;
        }

        int sideLength = layer * 2;
        int maxValueInLayer = (2 * layer + 1) * (2 * layer + 1) - 1;
        int diff = maxValueInLayer - index;

        int x;
        int z;
        if (diff < sideLength) {
            x = layer - diff;
            z = -layer;
        } else if (diff < sideLength * 2) {
            x = -layer;
            z = -layer + (diff - sideLength);
        } else if (diff < sideLength * 3) {
            x = -layer + (diff - sideLength * 2);
            z = layer;
        } else {
            x = layer;
            z = layer - (diff - sideLength * 3);
        }

        return new int[]{x * spacing, z * spacing};
    }
}
