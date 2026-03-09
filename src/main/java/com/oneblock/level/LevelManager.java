package com.oneblock.level;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LevelManager {

    private LevelManager() {
    }

    public static Material getRandomBlock(int level) {
        List<Material> pool = new ArrayList<>();

        pool.addAll(level0());
        if (level >= 1) pool.addAll(level1());
        if (level >= 2) pool.addAll(level2());
        if (level >= 3) pool.addAll(level3());
        if (level >= 4) pool.addAll(level4());
        if (level >= 5) pool.addAll(level5());

        return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
    }

    private static List<Material> level0() {
        return List.of(
                Material.DIRT,
                Material.DIRT,
                Material.DIRT,
                Material.GRASS_BLOCK,
                Material.DIRT_PATH,
                Material.OAK_LOG,
                Material.OAK_LEAVES,
                Material.DANDELION,
                Material.POPPY,
                Material.SHORT_GRASS,
                Material.MOSS_BLOCK
        );
    }

    private static List<Material> level1() {
        return List.of(
                Material.COBBLESTONE,
                Material.COBBLESTONE,
                Material.STONE,
                Material.BIRCH_LOG,
                Material.SPRUCE_LOG,
                Material.SAND,
                Material.GRAVEL,
                Material.CLAY,
                Material.COAL_ORE
        );
    }

    private static List<Material> level2() {
        return List.of(
                Material.STONE,
                Material.STONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.GRANITE,
                Material.COPPER_ORE,
                Material.IRON_ORE,
                Material.COAL_ORE,
                Material.TUFF
        );
    }

    private static List<Material> level3() {
        return List.of(
                Material.DEEPSLATE,
                Material.DEEPSLATE_COAL_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.GOLD_ORE,
                Material.REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.NETHERRACK,
                Material.BASALT,
                Material.SOUL_SAND,
                Material.MAGMA_BLOCK,
                Material.OBSIDIAN
        );
    }

    private static List<Material> level4() {
        return List.of(
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.OBSIDIAN,
                Material.OBSIDIAN,
                Material.NETHER_GOLD_ORE,
                Material.QUARTZ_ORE,
                Material.GLOWSTONE,
                Material.END_STONE
        );
    }

    private static List<Material> level5() {
        return List.of(
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.ANCIENT_DEBRIS,
                Material.OBSIDIAN,
                Material.END_STONE,
                Material.PURPUR_BLOCK,
                Material.QUARTZ_BLOCK
        );
    }
}
