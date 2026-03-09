package com.oneblock.data;

import com.oneblock.OneBlockPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerDataManager {

    private final OneBlockPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> dataMap = new HashMap<>();
    private YamlConfiguration config;

    public PlayerDataManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }

        try {
            if (!file.exists() && !file.createNewFile()) {
                plugin.getLogger().warning("Could not create players.yml");
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to create players.yml: " + exception.getMessage());
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.dataMap.clear();

        ConfigurationSection players = config.getConfigurationSection("players");
        if (players == null) {
            return;
        }

        for (String key : players.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int level = config.getInt("players." + key + ".level", 0);
                int blocksBroken = config.getInt("players." + key + ".blocksBroken", 0);
                dataMap.put(uuid, new PlayerData(level, blocksBroken));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping invalid UUID in players.yml: " + key);
            }
        }
    }

    public void save() {
        if (config == null) {
            config = new YamlConfiguration();
        }

        config.set("players", null);
        for (Map.Entry<UUID, PlayerData> entry : dataMap.entrySet()) {
            String base = "players." + entry.getKey();
            PlayerData data = entry.getValue();
            config.set(base + ".level", data.getLevel());
            config.set(base + ".blocksBroken", data.getBlocksBroken());
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to save players.yml: " + exception.getMessage());
        }
    }

    public void ensurePlayer(Player player) {
        dataMap.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerData(0, 0));
    }

    public PlayerData getData(UUID uuid) {
        return dataMap.computeIfAbsent(uuid, ignored -> new PlayerData(0, 0));
    }

    public PlayerData getData(Player player) {
        return getData(player.getUniqueId());
    }

    public LevelUpdateResult addBrokenBlock(Player player) {
        PlayerData data = getData(player);
        int oldLevel = data.getLevel();

        data.setBlocksBroken(data.getBlocksBroken() + 1);

        int newLevel = plugin.getLevelManager().getLevelForBrokenBlocks(data.getBlocksBroken());
        data.setLevel(newLevel);

        return new LevelUpdateResult(oldLevel, newLevel, data.getBlocksBroken());
    }

    public void reset(Player player) {
        dataMap.put(player.getUniqueId(), new PlayerData(0, 0));
    }
}
