package me.davidjawhar.oneblock.island;

import me.davidjawhar.oneblock.OneBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandManager {

    private final OneBlockPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, IslandData> islands = new HashMap<>();
    private final List<MobSpawnerData> spawners = new ArrayList<>();
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
        spawners.clear();
        nextIndex = yaml.getInt("next-index", 0);

        if (yaml.getConfigurationSection("islands") != null) {
            for (String key : yaml.getConfigurationSection("islands").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                String path = "islands." + key + ".";

                IslandData data = new IslandData(
                        uuid,
                        yaml.getInt(path + "index"),
                        yaml.getInt(path + "x"),
                        yaml.getInt(path + "y"),
                        yaml.getInt(path + "z"),
                        yaml.getString(path + "world", plugin.getConfig().getString("world-name", "oneblock_world"))
                );

                List<String> trusted = yaml.getStringList(path + "trusted");
                for (String entry : trusted) {
                    try {
                        data.trust(UUID.fromString(entry));
                    } catch (IllegalArgumentException ignored) {}
                }
                islands.put(uuid, data);
            }
        }

        if (yaml.getConfigurationSection("spawners") != null) {
            for (String key : yaml.getConfigurationSection("spawners").getKeys(false)) {
                String path = "spawners." + key + ".";
                try {
                    spawners.add(new MobSpawnerData(
                            UUID.fromString(yaml.getString(path + "owner")),
                            MobSpawnerData.Type.valueOf(yaml.getString(path + "type", "ANIMAL")),
                            yaml.getInt(path + "x"),
                            yaml.getInt(path + "y"),
                            yaml.getInt(path + "z"),
                            yaml.getString(path + "world", plugin.getConfig().getString("world-name", "oneblock_world"))
                    ));
                } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        yaml.set("islands", null);
        yaml.set("spawners", null);
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
            List<String> trusted = data.getTrusted().stream().map(UUID::toString).toList();
            yaml.set(path + "trusted", trusted);
        }

        for (int i = 0; i < spawners.size(); i++) {
            MobSpawnerData spawner = spawners.get(i);
            String path = "spawners." + i + ".";
            yaml.set(path + "owner", spawner.getOwner().toString());
            yaml.set(path + "type", spawner.getType().name());
            yaml.set(path + "x", spawner.getX());
            yaml.set(path + "y", spawner.getY());
            yaml.set(path + "z", spawner.getZ());
            yaml.set(path + "world", spawner.getWorldName());
        }

        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save islands.yml");
        }
    }

    public IslandData getIsland(UUID uuid) { return islands.get(uuid); }
    public Map<UUID, IslandData> getAllIslands() { return islands; }

    public IslandData findIslandByTrustedPlayer(UUID uuid) {
        for (IslandData island : islands.values()) {
            if (island.isTrusted(uuid)) return island;
        }
        return null;
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
        int y = plugin.getConfig().getInt("world-y", 64);
        String worldName = plugin.getConfig().getString("world-name", "oneblock_world");

        int index = nextIndex++;
        int gridWidth = 1000;
        int gridX = index % gridWidth;
        int gridZ = index / gridWidth;

        return new IslandData(uuid, index, gridX * spacing, y, gridZ * spacing, worldName);
    }

    public void generateIsland(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return;
        world.getBlockAt(data.getX(), data.getY() - 1, data.getZ()).setType(Material.BEDROCK);
        world.getBlockAt(data.getX(), data.getY(), data.getZ()).setType(Material.DIRT);
        clearAbove(world, data.getX(), data.getY(), data.getZ());
    }

    public void ensureIslandBlocks(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return;
        if (world.getBlockAt(data.getX(), data.getY() - 1, data.getZ()).getType() != Material.BEDROCK) {
            world.getBlockAt(data.getX(), data.getY() - 1, data.getZ()).setType(Material.BEDROCK);
        }
        if (world.getBlockAt(data.getX(), data.getY(), data.getZ()).getType() == Material.AIR) {
            world.getBlockAt(data.getX(), data.getY(), data.getZ()).setType(Material.DIRT);
        }
    }

    public void clearAbove(World world, int x, int y, int z) {
        world.getBlockAt(x, y + 1, z).setType(Material.AIR);
        world.getBlockAt(x, y + 2, z).setType(Material.AIR);
        world.getBlockAt(x, y + 3, z).setType(Material.AIR);
    }

    public Location getIslandTeleportLocation(IslandData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) return null;
        return new Location(world, data.getX() + 0.5, data.getY() + 1.0, data.getZ() + 0.5);
    }

    public void teleportHome(Player player) {
        IslandData data = getOrCreateIsland(player);
        Location location = getIslandTeleportLocation(data);
        if (location != null) player.teleport(location);
    }

    public void resetIsland(Player player) {
        UUID uuid = player.getUniqueId();
        IslandData island = islands.get(uuid);
        if (island == null) island = getOrCreateIsland(player);

        World world = Bukkit.getWorld(island.getWorldName());
        if (world != null) {
            int x = island.getX();
            int y = island.getY();
            int z = island.getZ();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy <= 4; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx == 0 && dy == -1 && dz == 0) continue;
                        world.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                    }
                }
            }
            generateIsland(island);
        }

        spawners.removeIf(s -> s.getOwner().equals(uuid));
        save();
    }

    public void trustPlayer(UUID owner, UUID trusted) {
        IslandData island = islands.get(owner);
        if (island != null) {
            island.trust(trusted);
            save();
        }
    }

    public void untrustPlayer(UUID owner, UUID trusted) {
        IslandData island = islands.get(owner);
        if (island != null) {
            island.untrust(trusted);
            save();
        }
    }

    public void addSpawner(MobSpawnerData spawner) {
        spawners.removeIf(existing -> existing.isCore(spawner.getX(), spawner.getY(), spawner.getZ(), spawner.getWorldName()));
        spawners.add(spawner);
        save();
    }

    public MobSpawnerData getSpawnerByTop(Block block) {
        String world = block.getWorld().getName();
        for (MobSpawnerData data : spawners) {
            if (data.isTop(block.getX(), block.getY(), block.getZ(), world)) {
                return data;
            }
        }
        return null;
    }

    public boolean isAnyIslandCoreBlock(Block block) {
        Location loc = block.getLocation();
        for (IslandData island : islands.values()) {
            if (loc.getWorld() == null || !loc.getWorld().getName().equals(island.getWorldName())) continue;
            if (island.isOneBlock(loc)) return true;
            if (loc.getBlockX() == island.getX() && loc.getBlockY() == island.getY() - 1 && loc.getBlockZ() == island.getZ()) return true;
        }
        for (MobSpawnerData spawner : spawners) {
            if (loc.getWorld() == null || !loc.getWorld().getName().equals(spawner.getWorldName())) continue;
            if (spawner.isCore(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName())) return true;
            if (spawner.isTop(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName())) return true;
        }
        return false;
    }
}
