package me.davidjawhar.oneblock.player;

import me.davidjawhar.oneblock.OneBlockPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final OneBlockPlugin plugin;
    private final File file;
    private final YamlConfiguration yaml;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load() {
        var section = yaml.getConfigurationSection("players");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "players." + key + ".";
            PlayerData data = new PlayerData(uuid, yaml.getString(path + "name", "Unknown"));
            data.setLevel(yaml.getInt(path + "level", 0));
            data.setBlocksBroken(yaml.getLong(path + "blocksBroken", 0));
            data.setDateCreated(yaml.getLong(path + "dateCreated", System.currentTimeMillis()));
            data.setLastLogin(yaml.getLong(path + "lastLogin", System.currentTimeMillis()));
            cache.put(uuid, data);
        }
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            data = new PlayerData(uuid, name);
            cache.put(uuid, data);
        }
        data.setName(name);
        data.setLastLogin(System.currentTimeMillis());
        return data;
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public Map<UUID, PlayerData> all() { return cache; }

    public void save() {
        yaml.set("players", null);
        for (PlayerData data : cache.values()) {
            String path = "players." + data.getUuid() + ".";
            yaml.set(path + "name", data.getName());
            yaml.set(path + "level", data.getLevel());
            yaml.set(path + "blocksBroken", data.getBlocksBroken());
            yaml.set(path + "dateCreated", data.getDateCreated());
            yaml.set(path + "lastLogin", data.getLastLogin());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save players.yml: " + e.getMessage());
        }
    }
}
