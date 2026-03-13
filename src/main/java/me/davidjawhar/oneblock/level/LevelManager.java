package me.davidjawhar.oneblock.level;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class LevelManager {

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

    public Material getNextBlock(UUID uuid, String name) {
        int level = getLevel(uuid, name);
        List<Material> pool = getCumulativePool(level);

        if (pool.isEmpty()) {
            return Material.DIRT;
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private List<Material> getCumulativePool(int level) {
        List<Material> pool = new ArrayList<>();

        if (level >= 0) {
            pool.add(Material.DIRT);
            pool.add(Material.GRASS_BLOCK);
            pool.add(Material.OAK_LOG);
            pool.add(Material.OAK_LEAVES);
            pool.add(Material.DANDELION);
            pool.add(Material.POPPY);
            pool.add(Material.SHORT_GRASS);
            pool.add(Material.MOSS_BLOCK);
        }

        if (level >= 1) {
            pool.add(Material.BIRCH_LOG);
            pool.add(Material.SPRUCE_LOG);
            pool.add(Material.COBBLESTONE);
            pool.add(Material.COAL_ORE);
            pool.add(Material.SAND);
            pool.add(Material.GRAVEL);
            pool.add(Material.CLAY);
        }

        if (level >= 2) {
            pool.add(Material.STONE);
            pool.add(Material.IRON_ORE);
            pool.add(Material.COPPER_ORE);
            pool.add(Material.ANDESITE);
            pool.add(Material.DIORITE);
            pool.add(Material.GRANITE);
            pool.add(Material.MUD);
        }

        if (level >= 3) {
            pool.add(Material.GOLD_ORE);
            pool.add(Material.REDSTONE_ORE);
            pool.add(Material.LAPIS_ORE);
            pool.add(Material.DEEPSLATE);
            pool.add(Material.TUFF);
            pool.add(Material.NETHERRACK);
            pool.add(Material.SOUL_SAND);
            pool.add(Material.NETHER_QUARTZ_ORE);
        }

        if (level >= 4) {
            pool.add(Material.DIAMOND_ORE);
            pool.add(Material.EMERALD_ORE);
            pool.add(Material.OBSIDIAN);
            pool.add(Material.MAGMA_BLOCK);
            pool.add(Material.GLOWSTONE);
            pool.add(Material.END_STONE);
        }

        if (level >= 5) {
            pool.add(Material.DIAMOND_ORE);
            pool.add(Material.EMERALD_ORE);
            pool.add(Material.OBSIDIAN);
            pool.add(Material.ANCIENT_DEBRIS);
            pool.add(Material.END_STONE);
            pool.add(Material.CRYING_OBSIDIAN);
            pool.add(Material.NETHER_GOLD_ORE);
            pool.add(Material.NETHER_QUARTZ_ORE);
        }

        return pool;
    }
}
