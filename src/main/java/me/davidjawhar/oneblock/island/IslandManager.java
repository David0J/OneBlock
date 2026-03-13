package me.davidjawhar.oneblock.island;

import me.davidjawhar.oneblock.OneBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandManager {

    private final OneBlockPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, IslandData> islands = new HashMap<>();
    private int nextIndex = 0;

    public IslandManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "islands.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create islands.yml");
            }
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void load() {
        islands.clear();
        nextIndex = yaml.getInt("next-index", 0);

        if (yaml.getConfigurationSection("islands") == null) return;

        for (String key : yaml.getConfigurationSection("islands").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "islands." + key + ".";

            int index = yaml.getInt(path + "index");
            int x = yaml.getInt(path + "x");
            int y = yaml.getInt(path + "y");
            int z = yaml.getInt(path + "z");
            String worldName = yaml.getString(path + "world", plugin.getConfig().getString("world-name", "oneblock_world"));

            islands.put(uuid, new IslandData(uuid, index, x, y, z, worldName));
        }
    }

    public void save() {
        yaml.set("islands", null);
        yaml.set("next-index", nextIndex);

        for (Map.Entry<UUID, IslandData> entry : islands.entrySet()) {
            UUID uuid = entry.getKey();
            IslandData data = entry.getValue();

            String path = "islands." + uuid + ".";
            yaml.set(path + "index", data.getIndex());
            yaml.set(path + "x", data.getX());
            yaml.set(path + "y", data.getY());
            yaml.set(path + "z", data.getZ());
            yaml.set(path + "world", data.getWorldName());
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save islands.yml");
        }
    }

    public IslandData getIsland(UUID uuid) {
        return islands.get(uuid);
    }

    public IslandData getOrCreateIsland(Player player) {
        IslandData existing = islands.get(player.getUniqueId());
        if (existing != null) {
            ensureIslandBlocks(existing);
            return existing;
        }

        IslandData created = createIsland(player.getUniqueId());
        islands.put(player.getUniqueId(), created);
        generateIsland(created);
        save();
        return created;
    }

    public IslandData createIsland(UUID uuid) {
        int spacing = plugin.getConfig().getInt("island-spacing", 1000);
        int y = plugin.getConfig().getInt("island-y", 64);
        String worldName = plugin.getConfig().getString("world-name", "oneblock_world");

        int index = nextIndex++;
        int gridWidth = 1000; // effectively unlimited for normal usage
        int gridX = index % gridWidth;
        int gridZ = index / gridWidth;

        int x = gridX * spacing;
        int z = gridZ * spacing;

        return new IslandData(uuid, index, x, y, z, worldName);
    }

    public void generateIsland(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return;

        int x = data.getX();
        int y = data.getY();
        int z = data.getZ();

        world.getBlockAt(x, y - 1, z).setType(Material.BEDROCK);
        world.getBlockAt(x, y, z).setType(Material.DIRT);

        // keep space above clear
        world.getBlockAt(x, y + 1, z).setType(Material.AIR);
        world.getBlockAt(x, y + 2, z).setType(Material.AIR);
    }

    public void ensureIslandBlocks(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return;

        int x = data.getX();
        int y = data.getY();
        int z = data.getZ();

        if (world.getBlockAt(x, y - 1, z).getType() != Material.BEDROCK) {
            world.getBlockAt(x, y - 1, z).setType(Material.BEDROCK);
        }

        if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
            world.getBlockAt(x, y, z).setType(Material.DIRT);
        }
    }

    public Location getIslandTeleportLocation(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return null;

        return new Location(world, data.getX() + 0.5, data.getY() + 1.0, data.getZ() + 0.5);
    }

    public World getWorld() {
        String worldName = plugin.getConfig().getString("world-name", "oneblock_world");
        return Bukkit.getWorld(worldName);
    }
    
    public Map<UUID, IslandData> getAllIslands() {
        return islands;
    }

    public void teleportHome(Player player) {
        IslandData data = getOrCreateIsland(player);
        Location location = getIslandTeleportLocation(data);
        if (location != null) {
            player.teleport(location);
        }
    }

    public void resetIsland(Player player) {
        UUID uuid = player.getUniqueId();
        IslandData island = islands.get(uuid);

        if (island == null) {
            island = getOrCreateIsland(player);
        }

        World world = Bukkit.getWorld(island.getWorldName());
        if (world != null) {
            int x = island.getX();
            int y = island.getY();
            int z = island.getZ();

            // clear small area around core
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy <= 3; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx == 0 && dy == -1 && dz == 0) continue; // keep bedrock slot handled below
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                    }
                }
            }

            world.getBlockAt(x, y - 1, z).setType(Material.BEDROCK);
            world.getBlockAt(x, y, z).setType(Material.DIRT);
            world.getBlockAt(x, y + 1, z).setType(Material.AIR);
        }

        save();
    }
}
