package com.oneblock;

import com.oneblock.commands.OBCommand;
import com.oneblock.data.PlayerDataManager;
import com.oneblock.island.IslandManager;
import com.oneblock.listeners.BlockBreakListener;
import com.oneblock.listeners.JoinListener;
import com.oneblock.listeners.QuitListener;
import com.oneblock.ui.LevelBarManager;
import com.oneblock.world.VoidChunkGenerator;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlockPlugin extends JavaPlugin {

    private static OneBlockPlugin instance;

    private PlayerDataManager playerDataManager;
    private IslandManager islandManager;
    private LevelBarManager levelBarManager;
    private World oneBlockWorld;

    public static OneBlockPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResourceIfMissing("players.yml");

        createVoidWorld();

        this.playerDataManager = new PlayerDataManager(this);
        this.islandManager = new IslandManager(this);
        this.levelBarManager = new LevelBarManager(this);

        this.playerDataManager.load();
        this.islandManager.loadFromData();

        registerListeners();
        registerCommand();

        for (Player player : getServer().getOnlinePlayers()) {
            levelBarManager.show(player);
        }

        getLogger().info("OneBlock enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.save();
        }
        if (levelBarManager != null) {
            levelBarManager.removeAll();
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    private void registerCommand() {
        PluginCommand command = getCommand("ob");
        if (command == null) {
            throw new IllegalStateException("/ob command not found in plugin.yml");
        }
        OBCommand executor = new OBCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void createVoidWorld() {
        String worldName = getConfig().getString("world.name", "oneblock_world");

        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new VoidChunkGenerator());
        this.oneBlockWorld = creator.createWorld();

        if (oneBlockWorld == null) {
            throw new IllegalStateException("Failed to create OneBlock world");
        }

        oneBlockWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
        oneBlockWorld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        oneBlockWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        oneBlockWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        oneBlockWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
        oneBlockWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        oneBlockWorld.setSpawnLocation(0, getConfig().getInt("world.spawn-y", 64) + 1, 0);
    }

    private void saveResourceIfMissing(String path) {
        if (!new java.io.File(getDataFolder(), path).exists()) {
            saveResource(path, false);
        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public LevelBarManager getLevelBarManager() {
        return levelBarManager;
    }

    public World getOneBlockWorld() {
        return oneBlockWorld;
    }
}
