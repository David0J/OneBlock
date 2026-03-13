package me.davidjawhar.oneblock;

import me.davidjawhar.oneblock.command.OBCommand;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.listener.BlockBreakListener;
import me.davidjawhar.oneblock.listener.JoinListener;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import me.davidjawhar.oneblock.world.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public class OneBlockPlugin extends JavaPlugin {

    private IslandManager islandManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        createVoidWorld();

        this.playerDataManager = new PlayerDataManager(this);
        this.islandManager = new IslandManager(this);
        this.levelManager = new LevelManager(this, playerDataManager);
        this.bossBarManager = new BossBarManager(this, playerDataManager, levelManager);

        getServer().getPluginManager().registerEvents(
                new JoinListener(this, islandManager, bossBarManager), this
        );
        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(this, islandManager, playerDataManager, levelManager, bossBarManager), this
        );

        OBCommand obCommand = new OBCommand(this, islandManager, playerDataManager, levelManager, bossBarManager);
        if (getCommand("ob") != null) {
            getCommand("ob").setExecutor(obCommand);
            getCommand("ob").setTabCompleter(obCommand);
        }

        Bukkit.getScheduler().runTaskTimer(this, bossBarManager::updateAll, 40L, 40L);

        getLogger().info("OneBlock enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.save();
        }
        if (bossBarManager != null) {
            bossBarManager.removeAll();
        }
    }

    private void createVoidWorld() {
        String worldName = getConfig().getString("world-name", "oneblock_world");
        World world = Bukkit.getWorld(worldName);
        if (world != null) return;

        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new VoidChunkGenerator());
        creator.generateStructures(false);
        Bukkit.createWorld(creator);
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }
}
