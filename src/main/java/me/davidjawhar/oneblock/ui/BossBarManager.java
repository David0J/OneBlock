package me.davidjawhar.oneblock.ui;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.player.PlayerData;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.level.LevelManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final IslandManager islandManager;
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public BossBarManager(OneBlockPlugin plugin, PlayerDataManager playerDataManager, LevelManager levelManager, IslandManager islandManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
        this.islandManager = islandManager;
    }

    public void show(Player player) {
        if (!plugin.getConfig().getBoolean("show-level-bossbar", true)) return;
        if (!islandManager.isInOneBlockWorld(player)) return;
        PlayerData data = playerDataManager.getOrCreate(player.getUniqueId(), player.getName());
        BossBar bar = bars.computeIfAbsent(player.getUniqueId(), id -> Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID));
        bar.setTitle(colorize(plugin.getConfig().getString("bossbar-title-format", "&5Level %level%").replace("%level%", String.valueOf(data.getLevel()))));
        bar.setProgress(1.0);
        if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        bar.setVisible(true);
    }

    public void hide(Player player) {
        BossBar bar = bars.get(player.getUniqueId());
        if (bar != null) bar.removePlayer(player);
    }

    public void update(Player player) {
        show(player);
    }

    public void refreshAll() {
        for (Player player : Bukkit.getOnlinePlayers()) show(player);
    }

    public void clearAll() {
        for (BossBar bar : bars.values()) {
            bar.removeAll();
        }
        bars.clear();
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
