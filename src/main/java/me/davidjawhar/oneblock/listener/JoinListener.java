package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final BossBarManager bossBarManager;

    public JoinListener(OneBlockPlugin plugin, IslandManager islandManager, BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getPlayerDataManager().getOrCreate(player.getUniqueId(), player.getName());
        islandManager.getOrCreateIsland(player);
        if (plugin.getConfig().getBoolean("teleport-returning-on-join", true)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> islandManager.teleportHome(player));
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> bossBarManager.show(player), 5L);
    }
}
