package me.davidjawhar.oneblock;

import me.davidjawhar.oneblock.command.OBCommand;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.listener.BlockBreakListener;
import me.davidjawhar.oneblock.listener.JoinListener;
import me.davidjawhar.oneblock.listener.ProtectListener;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class OneBlockPlugin extends JavaPlugin {
    private PlayerDataManager playerDataManager;
    private IslandManager islandManager;
    private LevelManager levelManager;
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("players.yml");
        saveResourceIfMissing("islands.yml");

        this.playerDataManager = new PlayerDataManager(this);
        this.levelManager = new LevelManager(this);
        this.islandManager = new IslandManager(this, playerDataManager, levelManager);
        this.bossBarManager = new BossBarManager(this, playerDataManager, levelManager, islandManager);

        islandManager.ensureWorldExists();

        getServer().getPluginManager().registerEvents(new JoinListener(this, islandManager, bossBarManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this, islandManager, playerDataManager, levelManager, bossBarManager), this);
        getServer().getPluginManager().registerEvents(new ProtectListener(this, islandManager), this);

        PluginCommand cmd = Objects.requireNonNull(getCommand("ob"));
        OBCommand executor = new OBCommand(this, islandManager, playerDataManager, levelManager, bossBarManager);
        cmd.setExecutor(executor);
        cmd.setTabCompleter(executor);

        getLogger().info("OneBlock enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) playerDataManager.save();
        if (islandManager != null) islandManager.save();
        if (bossBarManager != null) bossBarManager.clearAll();
    }

    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public IslandManager getIslandManager() { return islandManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }

    public void reloadEverything() {
        reloadConfig();
        levelManager.reload();
        islandManager.reload();
        bossBarManager.refreshAll();
    }

    private void saveResourceIfMissing(String name) {
        if (getResource(name) != null && !new java.io.File(getDataFolder(), name).exists()) {
            saveResource(name, false);
        }
    }
}
