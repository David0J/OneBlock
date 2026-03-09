package com.oneblock.level;

import com.oneblock.OneBlockPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public final class LevelManager {

    private final OneBlockPlugin plugin;
    private final TreeMap<Integer, Integer> thresholds = new TreeMap<>();
    private final Map<Integer, List<Material>> blockPools = new HashMap<>();
    private final Random random = new Random();

    public LevelManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        thresholds.clear();
        blockPools.clear();

        ConfigurationSection thresholdSection = plugin.getConfig().getConfigurationSection("progression.thresholds");
        if (thresholdSection != null) {
            for (String key : thresholdSection.getKeys(false)) {
                try {
                    thresholds.put(Integer.parseInt(key), thresholdSection.getInt(key));
                } catch (NumberFormatException ignored) {
                    plugin.getLogger().warning("Skipping invalid level threshold key: " + key);
                }
            }
        }

        ConfigurationSection poolSection = plugin.getConfig().getConfigurationSection("block-pools");
        if (poolSection != null) {
            for (String key : poolSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(key);
                    List<Material> materials = new ArrayList<>();
                    for (String raw : poolSection.getStringList(key)) {
                        Material material = Material.matchMaterial(raw);
                        if (material != null && material.isBlock()) {
                            materials.add(material);
                        } else {
                            plugin.getLogger().warning("Skipping invalid block material in config: " + raw);
                        }
                    }
                    if (!materials.isEmpty()) {
                        blockPools.put(level, List.copyOf(materials));
                    }
                } catch (NumberFormatException ignored) {
                    plugin.getLogger().warning("Skipping invalid block pool key: " + key);
                }
            }
        }
    }

    public int getLevelForBrokenBlocks(int blocksBroken) {
        int level = 0;
        for (Map.Entry<Integer, Integer> entry : thresholds.entrySet()) {
            if (blocksBroken >= entry.getValue()) {
                level = entry.getKey();
            }
        }
        return level;
    }

    public Material getRandomBlock(int level) {
        List<Integer> availableLevels = blockPools.keySet().stream()
            .filter(poolLevel -> poolLevel <= level)
            .sorted(Comparator.naturalOrder())
            .toList();

        if (availableLevels.isEmpty()) {
            return Material.DIRT;
        }

        List<Material> mergedPool = new ArrayList<>();
        for (int poolLevel : availableLevels) {
            mergedPool.addAll(blockPools.get(poolLevel));
        }

        return mergedPool.get(random.nextInt(mergedPool.size()));
    }
}
