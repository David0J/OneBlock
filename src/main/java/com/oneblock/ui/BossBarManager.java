package com.oneblock.ui;

import com.oneblock.OneBlockPlugin;
import com.oneblock.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossBarManager {

    private final OneBlockPlugin plugin;
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public BossBarManager(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player) {
        BossBar bossBar = bars.computeIfAbsent(player.getUniqueId(), ignored -> {
            BossBar created = Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID);
            created.setProgress(1.0);
            created.addPlayer(player);
            return created;
        });

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }

        update(player);
    }

    public void update(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getData(player);
        BossBar bossBar = bars.computeIfAbsent(player.getUniqueId(), ignored -> {
            BossBar created = Bukkit.createBossBar("Level 0", BarColor.PURPLE, BarStyle.SOLID);
            created.setProgress(1.0);
            created.addPlayer(player);
            return created;
        });

        bossBar.setTitle("Level " + data.getLevel());
        bossBar.setProgress(1.0);
    }

    public void remove(Player player) {
        BossBar bossBar = bars.remove(player.getUniqueId());
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }

    public void clearAll() {
        bars.values().forEach(BossBar::removeAll);
        bars.clear();
    }
}
