package com.oneblock.ui;

import com.oneblock.OneBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LevelBarManager {

    private final OneBlockPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public LevelBarManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), uuid ->
                Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID));
        bar.setVisible(true);
        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }
        update(player);
    }

    public void update(Player player) {
        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), uuid ->
                Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID));
        int level = plugin.getPlayerDataManager().getLevel(player);
        int broken = plugin.getPlayerDataManager().getBlocksBroken(player.getUniqueId());
        int currentLevelStart = switch (level) {
            case 5 -> 10000;
            case 4 -> 5000;
            case 3 -> 1500;
            case 2 -> 500;
            case 1 -> 100;
            default -> 0;
        };
        int nextLevelStart = switch (level) {
            case 0 -> 100;
            case 1 -> 500;
            case 2 -> 1500;
            case 3 -> 5000;
            case 4 -> 10000;
            default -> -1;
        };

        bar.setTitle("Level " + level);
        if (nextLevelStart == -1) {
            bar.setProgress(1.0D);
        } else {
            double progress = (double) (broken - currentLevelStart) / (double) (nextLevelStart - currentLevelStart);
            bar.setProgress(Math.max(0.0D, Math.min(1.0D, progress)));
        }
    }

    public void remove(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void removeAll() {
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
        }
        bossBars.clear();
    }
}
