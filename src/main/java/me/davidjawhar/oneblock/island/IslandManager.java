package me.davidjawhar.oneblock.island;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.world.VoidChunkGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandManager {
    private final OneBlockPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, IslandData> islands = new HashMap<>();
    private int nextIndex;
    private World world;

    public IslandManager(OneBlockPlugin plugin, PlayerDataManager playerDataManager, LevelManager levelManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
        this.file = new File(plugin.getDataFolder(), "islands.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void reload() {
        ensureWorldExists();
    }

    public void ensureWorldExists() {
        String worldName = plugin.getConfig().getString("world-name", "oneblock_world");
        World existing = Bukkit.getWorld(worldName);
        if (existing != null) {
            this.world = existing;
            configureWorld(existing);
            return;
        }
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.FLAT);
        creator.generator(new VoidChunkGenerator());
        creator.generateStructures(false);
        World created = creator.createWorld();
        if (created != null) {
            configureWorld(created);
            this.world = created;
        }
    }

    private void configureWorld(World world) {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setStorm(false);
        world.setSpawnLocation(0, plugin.getConfig().getInt("world-y", 64), 0);
    }

    private void load() {
        nextIndex = yaml.getInt("next-index", 0);
        var section = yaml.getConfigurationSection("islands");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "islands." + key + ".";
            islands.put(uuid, new IslandData(
                    uuid,
                    yaml.getInt(path + "index"),
                    yaml.getString(path + "world", plugin.getConfig().getString("world-name", "oneblock_world")),
                    yaml.getInt(path + "x"),
                    yaml.getInt(path + "y"),
                    yaml.getInt(path + "z")
            ));
        }
    }

    public void save() {
        yaml.set("islands", null);
        for (IslandData data : islands.values()) {
            String path = "islands." + data.getUuid() + ".";
            yaml.set(path + "world", data.getWorldName());
            yaml.set(path + "index", data.getIndex());
            yaml.set(path + "x", data.getX());
            yaml.set(path + "y", data.getY());
            yaml.set(path + "z", data.getZ());
        }
        yaml.set("next-index", nextIndex);
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save islands.yml: " + e.getMessage());
        }
    }

    public IslandData getOrCreateIsland(Player player) {
        IslandData data = islands.get(player.getUniqueId());
        if (data == null) {
            data = allocate(player.getUniqueId());
            islands.put(player.getUniqueId(), data);
            save();
            generateIsland(data, Material.DIRT);
            player.sendMessage(ChatColor.GREEN + "Your OneBlock island has been created.");
        } else {
            ensureIslandIntegrity(data, Material.DIRT);
        }
        return data;
    }

    public IslandData getIsland(UUID uuid) {
        return islands.get(uuid);
    }

    public Map<UUID, IslandData> getIslands() { return islands; }

    private IslandData allocate(UUID uuid) {
        int spacing = plugin.getConfig().getInt("island-spacing", 1000);
        int rowSize = 3;
        int index = nextIndex++;
        int row = index / rowSize;
        int col = index % rowSize;
        int x = col * spacing;
        int z = row * spacing;
        int y = plugin.getConfig().getInt("world-y", 64);
        return new IslandData(uuid, index, plugin.getConfig().getString("world-name", "oneblock_world"), x, y, z);
    }

    public void generateIsland(IslandData data, Material topMaterial) {
        ensureWorldExists();
        Location top = data.getBaseLocation(world);
        Block bedrock = top.clone().add(0, -1, 0).getBlock();
        bedrock.setType(Material.BEDROCK, false);
        top.getBlock().setType(topMaterial, false);
    }

    public void ensureIslandIntegrity(IslandData data, Material fallbackTop) {
        ensureWorldExists();
        Location top = data.getBaseLocation(world);
        if (top.clone().add(0, -1, 0).getBlock().getType() != Material.BEDROCK) {
            top.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK, false);
        }
        if (top.getBlock().getType().isAir()) {
            top.getBlock().setType(fallbackTop, false);
        }
    }

    public void teleportHome(Player player) {
        IslandData data = getOrCreateIsland(player);
        player.teleport(data.getTeleportLocation(world));
    }

    public boolean isPlayerOneBlock(Player player, Block block) {
        IslandData data = islands.get(player.getUniqueId());
        if (data == null) return false;
        return block.getWorld().getName().equals(data.getWorldName())
                && block.getX() == data.getX()
                && block.getY() == data.getY()
                && block.getZ() == data.getZ();
    }

    public boolean isOfficialOneBlock(Block block) {
        for (IslandData data : islands.values()) {
            if (block.getWorld().getName().equals(data.getWorldName())
                    && block.getX() == data.getX()
                    && block.getY() == data.getY()
                    && block.getZ() == data.getZ()) return true;
        }
        return false;
    }

    public UUID getOwner(Block block) {
        for (var entry : islands.entrySet()) {
            IslandData data = entry.getValue();
            if (block.getWorld().getName().equals(data.getWorldName())
                    && block.getX() == data.getX()
                    && block.getY() == data.getY()
                    && block.getZ() == data.getZ()) return entry.getKey();
        }
        return null;
    }

    public void regenerateOneBlock(Player owner, Block brokenBlock) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            var pdata = playerDataManager.getOrCreate(owner.getUniqueId(), owner.getName());
            Material next = levelManager.getRandomBlockForLevel(pdata.getLevel());
            if (brokenBlock.getType().isAir()) {
                brokenBlock.setType(next, false);
            }
        }, plugin.getConfig().getLong("regeneration-delay-ticks", 1L));
    }

    public void resetIsland(UUID uuid, String name) {
        IslandData old = islands.get(uuid);
        if (old == null) {
            islands.put(uuid, allocate(uuid));
        }
        IslandData data = islands.get(uuid);
        if (data == null) {
            data = allocate(uuid);
            islands.put(uuid, data);
        }
        playerDataManager.getOrCreate(uuid, name).setBlocksBroken(0);
        playerDataManager.getOrCreate(uuid, name).setLevel(0);
        generateIsland(data, Material.DIRT);
        save();
        playerDataManager.save();
    }

    public World getWorld() { return world; }
    public boolean isInOneBlockWorld(Player player) { return world != null && player.getWorld().equals(world); }
}
