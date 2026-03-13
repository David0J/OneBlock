package me.davidjawhar.oneblock.level;

import me.davidjawhar.oneblock.OneBlockPlugin;
import org.bukkit.Material;

import java.util.*;

public class LevelManager {
    private final OneBlockPlugin plugin;
    private final NavigableMap<Integer, Long> thresholds = new TreeMap<>();
    private final Map<Integer, List<Material>> pools = new HashMap<>();
    private final Random random = new Random();

    public LevelManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        thresholds.clear();
        pools.clear();

        var section = plugin.getConfig().getConfigurationSection("level-thresholds");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                thresholds.put(Integer.parseInt(key), section.getLong(key));
            }
        }
        var poolSection = plugin.getConfig().getConfigurationSection("block-pools");
        if (poolSection != null) {
            for (String key : poolSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                List<Material> mats = new ArrayList<>();
                for (String s : poolSection.getStringList(key)) {
                    Material m = Material.matchMaterial(s);
                    if (m != null && m.isBlock()) mats.add(m);
                }
                pools.put(level, mats);
            }
        }
    }

    public int getLevelForBroken(long broken) {
        int level = 0;
        for (var entry : thresholds.entrySet()) {
            if (broken >= entry.getValue()) level = entry.getKey();
        }
        return level;
    }

    public Material getRandomBlockForLevel(int level) {
        List<Material> cumulative = new ArrayList<>();
        for (int i = 0; i <= level; i++) {
            List<Material> list = pools.get(i);
            if (list != null) cumulative.addAll(list);
        }
        if (cumulative.isEmpty()) return Material.DIRT;
        return cumulative.get(random.nextInt(cumulative.size()));
    }
}
