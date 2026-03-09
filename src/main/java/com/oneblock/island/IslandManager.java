package com.oneblock.island;

import com.oneblock.OneBlockPlugin;
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

public final class IslandManager {

    private final OneBlockPlugin plugin;
    private final File file;
    private final Map<UUID, IslandData> islands = new HashMap<>();
    private YamlConfiguration config;
    private int nextIndex;

    public IslandManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "islands.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }

        try {
            if (!file.exists() && !file.createNewFile()) {
                plugin.getLogger().warning("Could not create islands.yml");
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to create islands.yml: " + exception.getMessage());
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.islands.clear();

        World world = plugin.getOneBlockWorld();
        this.nextIndex = config.getInt("next-index", 0);

        if (config.getConfigurationSection("islands") == null) {
            return;
        }

        for (String key : config.getConfigurationSection("islands").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String base = "islands." + key;
                int x = config.getInt(base + ".x");
                int y = config.getInt(base + ".y");
                int z = config.getInt(base + ".z");
                int index = config.getInt(base + ".index");
                islands.put(uuid, new IslandData(new Location(world, x, y, z), index));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping invalid UUID in islands.yml: " + key);
            }
        }
    }

    public void save() {
        if (config == null) {
            config = new YamlConfiguration();
        }

        config.set("islands", null);
        config.set("next-index", nextIndex);

        for (Map.Entry<UUID, IslandData> entry : islands.entrySet()) {
            String base = "islands." + entry.getKey();
            IslandData data = entry.getValue();
            config.set(base + ".x", data.blockLocation().getBlockX());
            config.set(base + ".y", data.blockLocation().getBlockY());
            config.set(base + ".z", data.blockLocation().getBlockZ());
            config.set(base + ".index", data.index());
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save islands.yml: " + exception.getMessage());
        }
    }

    public boolean hasIsland(UUID uuid) {
        return islands.containsKey(uuid);
    }

    public IslandData getIsland(UUID uuid) {
        return islands.get(uuid);
    }

    public IslandData getIsland(Player player) {
        return islands.get(player.getUniqueId());
    }

    public void ensureIsland(Player player) {
        if (hasIsland(player.getUniqueId())) {
            ensureIslandBlocks(player.getUniqueId());
            return;
        }

        int spawnY = plugin.getConfig().getInt("world.spawn-y", 64);
        int spacing = plugin.getConfig().getInt("world.spacing", 1000);
        int gridWidth = 1000;

        int index = nextIndex++;
        int gridX = index % gridWidth;
        int gridZ = index / gridWidth;

        int x = gridX * spacing;
        int z = gridZ * spacing;

        Location blockLocation = new Location(plugin.getOneBlockWorld(), x, spawnY, z);
        islands.put(player.getUniqueId(), new IslandData(blockLocation, index));
        ensureIslandBlocks(player.getUniqueId());
        save();
    }

    public void ensureIslandBlocks(UUID uuid) {
        IslandData island = islands.get(uuid);
        if (island == null) {
            return;
        }

        Location blockLocation = island.blockLocation();
        Location bedrockLocation = blockLocation.clone().subtract(0, 1, 0);

        bedrockLocation.getBlock().setType(Material.BEDROCK, false);
        if (blockLocation.getBlock().getType().isAir()) {
            blockLocation.getBlock().setType(Material.DIRT, false);
        }
    }

    public Location getSpawnLocation(UUID uuid) {
        IslandData island = islands.get(uuid);
        if (island == null) {
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        return island.blockLocation().clone().add(0.5, 1.0, 0.5);
    }

    public void reset(Player target) {
        IslandData island = islands.remove(target.getUniqueId());
        if (island != null) {
            Location block = island.blockLocation();
            block.getBlock().setType(Material.AIR, false);
            block.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR, false);
        }

        plugin.getPlayerDataManager().reset(target);
        ensureIsland(target);
        save();
    }
}
