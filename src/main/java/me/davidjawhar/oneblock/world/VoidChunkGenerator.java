package me.davidjawhar.oneblock.world;

import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {
    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Intentionally empty for a true void world.
    }

    @Override
    public boolean shouldGenerateNoise() { return false; }
    @Override
    public boolean shouldGenerateSurface() { return false; }
    @Override
    public boolean shouldGenerateBedrock() { return false; }
    @Override
    public boolean shouldGenerateCaves() { return false; }
    @Override
    public boolean shouldGenerateDecorations() { return false; }
    @Override
    public boolean shouldGenerateMobs() { return false; }
    @Override
    public boolean shouldGenerateStructures() { return false; }
}
