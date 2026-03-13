package me.davidjawhar.oneblock.player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private int blocksBroken;
    private int level;
    private int ironOresBroken;
    private boolean animalSpawnerGiven;
    private boolean hostileSpawnerGiven;
    private boolean endSpawnerGiven;
    private int nextHungerCheckpoint;

    public PlayerData(UUID uuid, String name, int blocksBroken, int level) {
        this.uuid = uuid;
        this.name = name;
        this.blocksBroken = blocksBroken;
        this.level = level;
        this.nextHungerCheckpoint = 75;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public int getBlocksBroken() { return blocksBroken; }
    public int getLevel() { return level; }
    public int getIronOresBroken() { return ironOresBroken; }
    public boolean isAnimalSpawnerGiven() { return animalSpawnerGiven; }
    public boolean isHostileSpawnerGiven() { return hostileSpawnerGiven; }
    public boolean isEndSpawnerGiven() { return endSpawnerGiven; }
    public int getNextHungerCheckpoint() { return nextHungerCheckpoint; }

    public void setName(String name) { this.name = name; }
    public void setBlocksBroken(int blocksBroken) { this.blocksBroken = blocksBroken; }
    public void setLevel(int level) { this.level = level; }
    public void setIronOresBroken(int ironOresBroken) { this.ironOresBroken = ironOresBroken; }
    public void setAnimalSpawnerGiven(boolean animalSpawnerGiven) { this.animalSpawnerGiven = animalSpawnerGiven; }
    public void setHostileSpawnerGiven(boolean hostileSpawnerGiven) { this.hostileSpawnerGiven = hostileSpawnerGiven; }
    public void setEndSpawnerGiven(boolean endSpawnerGiven) { this.endSpawnerGiven = endSpawnerGiven; }
    public void setNextHungerCheckpoint(int nextHungerCheckpoint) { this.nextHungerCheckpoint = nextHungerCheckpoint; }
}
