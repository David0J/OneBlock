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

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create players.yml");
            }
        }

        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void load() {
        cache.clear();
        if (yaml.getConfigurationSection("players") == null) return;

        for (String key : yaml.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            String path = "players." + key;
            PlayerData data = new PlayerData(
                    uuid,
                    yaml.getString(path + ".name", "Unknown"),
                    yaml.getInt(path + ".blocksBroken", 0),
                    yaml.getInt(path + ".level", 0)
            );
            data.setIronOresBroken(yaml.getInt(path + ".ironOresBroken", 0));
            data.setAnimalSpawnerGiven(yaml.getBoolean(path + ".animalSpawnerGiven", false));
            data.setHostileSpawnerGiven(yaml.getBoolean(path + ".hostileSpawnerGiven", false));
            data.setEndSpawnerGiven(yaml.getBoolean(path + ".endSpawnerGiven", false));
            data.setNextHungerCheckpoint(yaml.getInt(path + ".nextHungerCheckpoint", 75));
            cache.put(uuid, data);
        }
    }

    public void save() {
        yaml.set("players", null);
        for (PlayerData data : cache.values()) {
            String path = "players." + data.getUuid();
            yaml.set(path + ".name", data.getName());
            yaml.set(path + ".blocksBroken", data.getBlocksBroken());
            yaml.set(path + ".level", data.getLevel());
            yaml.set(path + ".ironOresBroken", data.getIronOresBroken());
            yaml.set(path + ".animalSpawnerGiven", data.isAnimalSpawnerGiven());
            yaml.set(path + ".hostileSpawnerGiven", data.isHostileSpawnerGiven());
            yaml.set(path + ".endSpawnerGiven", data.isEndSpawnerGiven());
            yaml.set(path + ".nextHungerCheckpoint", data.getNextHungerCheckpoint());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.yml");
        }
    }

    public PlayerData getOrCreate(UUID uuid, String name) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            data = new PlayerData(uuid, name, 0, 0);
            cache.put(uuid, data);
            save();
        } else if (name != null && !data.getName().equals(name)) {
            data.setName(name);
        }
        return data;
    }

    public void incrementBlocksBroken(UUID uuid, String name) {
        PlayerData data = getOrCreate(uuid, name);
        data.setBlocksBroken(data.getBlocksBroken() + 1);
    }

    public void incrementIronOresBroken(UUID uuid, String name) {
        PlayerData data = getOrCreate(uuid, name);
        data.setIronOresBroken(data.getIronOresBroken() + 1);
    }

    public int getBlocksBroken(UUID uuid, String name) { return getOrCreate(uuid, name).getBlocksBroken(); }
    public int getLevel(UUID uuid, String name) { return getOrCreate(uuid, name).getLevel(); }
    public int getIronOresBroken(UUID uuid, String name) { return getOrCreate(uuid, name).getIronOresBroken(); }
    public void setLevel(UUID uuid, String name, int level) { getOrCreate(uuid, name).setLevel(level); }
    public PlayerData get(UUID uuid, String name) { return getOrCreate(uuid, name); }

    public void reset(UUID uuid, String name) {
        PlayerData data = getOrCreate(uuid, name);
        data.setBlocksBroken(0);
        data.setLevel(0);
        data.setIronOresBroken(0);
        data.setAnimalSpawnerGiven(false);
        data.setHostileSpawnerGiven(false);
        data.setEndSpawnerGiven(false);
        data.setNextHungerCheckpoint(75);
        save();
    }
}
