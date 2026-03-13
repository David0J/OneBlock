package me.davidjawhar.oneblock.ui;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerData;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private final OneBlockPlugin plugin;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public BossBarManager(OneBlockPlugin plugin,
                          PlayerDataManager playerDataManager,
                          LevelManager levelManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
    }

    public void show(Player player) {
        BossBar bar = bars.computeIfAbsent(player.getUniqueId(), uuid ->
                Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID));

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        bar.setVisible(true);
        update(player);
    }

    public void hide(Player player) {
        BossBar bar = bars.get(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
            bar.setVisible(false);
        }
    }

    public void update(Player player) {
        PlayerData data = playerDataManager.getOrCreate(player.getUniqueId(), player.getName());
        int blocksBroken = data.getBlocksBroken();
        int level = levelManager.getLevel(blocksBroken);

        playerDataManager.setLevel(player.getUniqueId(), player.getName(), level);

        int currentMin = levelManager.getLevelMinBlocks(level);
        int nextMin = levelManager.getNextLevelMinBlocks(level);

        double progress;
        if (level >= 5 || nextMin <= currentMin) {
            progress = 1.0;
        } else {
            progress = (double) (blocksBroken - currentMin) / (double) (nextMin - currentMin);
        }

        progress = Math.max(0.0, Math.min(1.0, progress));

        BossBar bar = bars.computeIfAbsent(player.getUniqueId(), uuid ->
                Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID));

        bar.setTitle("§5Level " + level + " §7(" + blocksBroken + " broken)");
        bar.setProgress(progress);
        bar.setColor(BarColor.PURPLE);
        bar.setStyle(BarStyle.SOLID);

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        bar.setVisible(true);
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            show(player);
        }
    }

    public void removeAll() {
        for (BossBar bar : bars.values()) {
            bar.removeAll();
        }
        bars.clear();
    }
}
