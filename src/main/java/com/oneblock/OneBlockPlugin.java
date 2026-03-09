package com.oneblock;

import com.oneblock.command.OBCommand;
import com.oneblock.data.PlayerDataManager;
import com.oneblock.island.IslandManager;
import com.oneblock.level.LevelManager;
import com.oneblock.listener.BlockBreakListener;
import com.oneblock.listener.JoinListener;
import com.oneblock.ui.BossBarManager;
import com.oneblock.world.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class OneBlockPlugin extends JavaPlugin {

    private World oneBlockWorld;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private IslandManager islandManager;
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.oneBlockWorld = createVoidWorld();
        this.playerDataManager = new PlayerDataManager(this);
        this.levelManager = new LevelManager(this);
        this.bossBarManager = new BossBarManager(this);
        this.islandManager = new IslandManager(this);

        playerDataManager.load();
        islandManager.load();

        registerListeners();
        registerCommands();

        Bukkit.getOnlinePlayers().forEach(player -> {
            playerDataManager.ensurePlayer(player);
            islandManager.ensureIsland(player);
            bossBarManager.show(player);
        });

        getLogger().info("OneBlock enabled.");
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.clearAll();
        }
        if (islandManager != null) {
            islandManager.save();
        }
        if (playerDataManager != null) {
            playerDataManager.save();
        }
    }

    private World createVoidWorld() {
        String worldName = getConfig().getString("world.name", "oneblock_world");
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new VoidChunkGenerator());
        World world = creator.createWorld();

        Objects.requireNonNull(world, "Failed to create world " + worldName);

        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        return world;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("ob");
        Objects.requireNonNull(command, "Command /ob is missing from plugin.yml");
        OBCommand executor = new OBCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    public World getOneBlockWorld() {
        return oneBlockWorld;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
}
