package me.davidjawhar.oneblock.level;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LevelManager {

    private static class WeightedEntry {
        private final int weight;
        private final GenerationOutcome outcome;

        private WeightedEntry(int weight, GenerationOutcome outcome) {
            this.weight = weight;
            this.outcome = outcome;
        }
    }

    private final OneBlockPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final Random random = new Random();

    public LevelManager(OneBlockPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }

    public int getLevel(int blocksBroken) {
        if (blocksBroken >= 10000) return 5;
        if (blocksBroken >= 5000) return 4;
        if (blocksBroken >= 1500) return 3;
        if (blocksBroken >= 500) return 2;
        if (blocksBroken >= 100) return 1;
        return 0;
    }

    public int getLevel(UUID uuid, String name) {
        int blocksBroken = playerDataManager.getBlocksBroken(uuid, name);
        int level = getLevel(blocksBroken);
        playerDataManager.setLevel(uuid, name, level);
        return level;
    }

    public int getLevelMinBlocks(int level) {
        return switch (level) {
            case 0 -> 0;
            case 1 -> 100;
            case 2 -> 500;
            case 3 -> 1500;
            case 4 -> 5000;
            case 5 -> 10000;
            default -> 0;
        };
    }

    public int getNextLevelMinBlocks(int level) {
        return switch (level) {
            case 0 -> 100;
            case 1 -> 500;
            case 2 -> 1500;
            case 3 -> 5000;
            case 4 -> 10000;
            case 5 -> 10000;
            default -> 10000;
        };
    }

    public GenerationOutcome getNextOutcome(UUID uuid, String name) {
        int level = getLevel(uuid, name);
        int iron = playerDataManager.getIronOresBroken(uuid, name);
        List<WeightedEntry> pool = new ArrayList<>();

        // special event blocks
        pool.add(new WeightedEntry(4, new GenerationOutcome(GenerationOutcome.Type.CHEST_BLOCK, Material.BARREL)));
        pool.add(new WeightedEntry(5, new GenerationOutcome(GenerationOutcome.Type.TREE_BLOCK, Material.MOSS_BLOCK)));
        pool.add(new WeightedEntry(4, new GenerationOutcome(GenerationOutcome.Type.ORE_CLUSTER_BLOCK, Material.TUFF)));

        // TNT is intentionally rarer now
        pool.add(new WeightedEntry(1, new GenerationOutcome(GenerationOutcome.Type.TNT_BLOCK, Material.TNT)));

        // level 0 base
        add(pool, 24, Material.DIRT);
        add(pool, 16, Material.GRASS_BLOCK);
        add(pool, 14, Material.OAK_LOG);
        add(pool, 10, Material.OAK_LEAVES);
        add(pool, 8, Material.COBBLESTONE);
        add(pool, 5, Material.MELON);
        add(pool, 4, Material.PUMPKIN);
        add(pool, 3, Material.SHORT_GRASS);
        add(pool, 3, Material.MOSS_BLOCK);
        add(pool, 2, Material.DANDELION);
        add(pool, 2, Material.POPPY);

        if (level >= 1) {
            add(pool, 10, Material.BIRCH_LOG);
            add(pool, 10, Material.SPRUCE_LOG);
            add(pool, 6, Material.SAND);
            add(pool, 6, Material.GRAVEL);
            add(pool, 5, Material.CLAY);
            add(pool, 5, Material.COAL_ORE);
            add(pool, 4, Material.HAY_BLOCK);
        }

        if (level >= 2) {
            add(pool, 10, Material.STONE);
            add(pool, 8, Material.IRON_ORE);
            add(pool, 6, Material.COPPER_ORE);
            add(pool, 4, Material.ANDESITE);
            add(pool, 4, Material.DIORITE);
            add(pool, 4, Material.GRANITE);
            add(pool, 3, Material.MUD);
        }

        if (level >= 3) {
            add(pool, 6, Material.GOLD_ORE);
            add(pool, 6, Material.REDSTONE_ORE);
            add(pool, 5, Material.LAPIS_ORE);
            add(pool, 4, Material.DEEPSLATE);
            add(pool, 4, Material.NETHERRACK);
            add(pool, 3, Material.SOUL_SAND);
            add(pool, 4, Material.NETHER_QUARTZ_ORE);
        }

        if (level >= 4) {
            add(pool, 5, Material.DIAMOND_ORE);
            add(pool, 3, Material.EMERALD_ORE);
            add(pool, 3, Material.OBSIDIAN);
            add(pool, 2, Material.GLOWSTONE);
            add(pool, 3, Material.END_STONE);
        }

        if (level >= 5) {
            add(pool, 2, Material.ANCIENT_DEBRIS);
            add(pool, 2, Material.CRYING_OBSIDIAN);
            add(pool, 3, Material.NETHER_GOLD_ORE);
        }

        // gated utility liquids represented safely as blocks that can still be broken
        if (iron >= 5) {
            add(pool, 3, Material.ICE);
        }
        if (level >= 3) {
            add(pool, 2, Material.BASALT);
        }

        int total = pool.stream().mapToInt(e -> e.weight).sum();
        int roll = random.nextInt(total);
        int running = 0;
        for (WeightedEntry entry : pool) {
            running += entry.weight;
            if (roll < running) {
                return entry.outcome;
            }
        }
        return new GenerationOutcome(GenerationOutcome.Type.NORMAL, Material.DIRT);
    }

    public List<Material> getOreClusterMaterials(UUID uuid, String name) {
        int level = getLevel(uuid, name);
        List<Material> ores = new ArrayList<>();
        ores.add(Material.COAL_ORE);
        if (level >= 2) {
            ores.add(Material.IRON_ORE);
            ores.add(Material.COPPER_ORE);
        }
        if (level >= 3) {
            ores.add(Material.GOLD_ORE);
            ores.add(Material.REDSTONE_ORE);
            ores.add(Material.LAPIS_ORE);
            ores.add(Material.NETHER_QUARTZ_ORE);
        }
        if (level >= 4) {
            ores.add(Material.DIAMOND_ORE);
            ores.add(Material.EMERALD_ORE);
        }
        if (level >= 5) {
            ores.add(Material.ANCIENT_DEBRIS);
        }
        return ores;
    }

    public Material randomOreForCluster(UUID uuid, String name) {
        List<Material> mats = getOreClusterMaterials(uuid, name);
        return mats.get(random.nextInt(mats.size()));
    }

    public boolean shouldGiveBonusDrops(UUID uuid, String name) {
        int level = getLevel(uuid, name);
        if (level < 2) return false;
        int chance = switch (level) {
            case 2 -> 20;
            case 3 -> 30;
            default -> 40;
        };
        return random.nextInt(100) < chance;
    }

    public int bonusDropAmount(UUID uuid, String name) {
        int level = getLevel(uuid, name);
        if (level <= 2) return 1;
        if (level == 3) return 1 + random.nextInt(2);
        return 1 + random.nextInt(3);
    }

    private void add(List<WeightedEntry> pool, int weight, Material material) {
        pool.add(new WeightedEntry(weight, new GenerationOutcome(GenerationOutcome.Type.NORMAL, material)));
    }
}