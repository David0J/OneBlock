package com.oneblock.data;

import com.oneblock.OneBlockPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerDataManager {

    private final OneBlockPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public PlayerDataManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        this.config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("players") == null) {
            config.createSection("players");
            save();
        }
    }

    public void reload() {
        load();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }

    public void createOrResetPlayer(Player player, int islandIndex, int x, int y, int z) {
        String path = path(player.getUniqueId());
        config.set(path + ".name", player.getName());
        config.set(path + ".level", 0);
        config.set(path + ".blocksBroken", 0);
        config.set(path + ".island.index", islandIndex);
        config.set(path + ".island.x", x);
        config.set(path + ".island.y", y);
        config.set(path + ".island.z", z);
        save();
    }

    public void touchPlayerName(Player player) {
        config.set(path(player.getUniqueId()) + ".name", player.getName());
        save();
    }

    public boolean hasData(UUID uuid) {
        return config.contains(path(uuid));
    }

    public void incrementBrokenBlocks(Player player) {
        UUID uuid = player.getUniqueId();
        int broken = getBlocksBroken(uuid) + 1;
        config.set(path(uuid) + ".name", player.getName());
        config.set(path(uuid) + ".blocksBroken", broken);
        config.set(path(uuid) + ".level", calculateLevel(broken));
        save();
    }

    public int getBlocksBroken(UUID uuid) {
        return config.getInt(path(uuid) + ".blocksBroken", 0);
    }

    public int getLevel(UUID uuid) {
        return config.getInt(path(uuid) + ".level", 0);
    }

    public int getLevel(Player player) {
        return getLevel(player.getUniqueId());
    }

    public Integer getIslandIndex(UUID uuid) {
        return config.contains(path(uuid) + ".island.index") ? config.getInt(path(uuid) + ".island.index") : null;
    }

    public Integer getIslandX(UUID uuid) {
        return config.contains(path(uuid) + ".island.x") ? config.getInt(path(uuid) + ".island.x") : null;
    }

    public Integer getIslandY(UUID uuid) {
        return config.contains(path(uuid) + ".island.y") ? config.getInt(path(uuid) + ".island.y") : null;
    }

    public Integer getIslandZ(UUID uuid) {
        return config.contains(path(uuid) + ".island.z") ? config.getInt(path(uuid) + ".island.z") : null;
    }

    public Set<UUID> getKnownPlayers() {
        ConfigurationSection section = config.getConfigurationSection("players");
        if (section == null) {
            return Collections.emptySet();
        }
        Set<UUID> uuids = new HashSet<>();
        for (String key : section.getKeys(false)) {
            try {
                uuids.add(UUID.fromString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return uuids;
    }

    public String getStoredName(UUID uuid) {
        return config.getString(path(uuid) + ".name", "unknown");
    }

    public UUID findUuidByName(String name) {
        for (UUID uuid : getKnownPlayers()) {
            String stored = getStoredName(uuid);
            if (stored != null && stored.equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    public void deletePlayer(UUID uuid) {
        config.set(path(uuid), null);
        save();
    }

    public int calculateLevel(int blocksBroken) {
        if (blocksBroken >= 10000) return 5;
        if (blocksBroken >= 5000) return 4;
        if (blocksBroken >= 1500) return 3;
        if (blocksBroken >= 500) return 2;
        if (blocksBroken >= 100) return 1;
        return 0;
    }

    private String path(UUID uuid) {
        return "players." + uuid;
    }
}
