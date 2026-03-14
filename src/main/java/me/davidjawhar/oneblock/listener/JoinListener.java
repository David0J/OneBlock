package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
        Player player = event.getPlayer();
        IslandData island = islandManager.getOrCreateIsland(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(islandManager.getIslandTeleportLocation(island));

            // show a bit later so it survives join + teleport properly
            Bukkit.getScheduler().runTaskLater(plugin, () -> bossBarManager.show(player), 20L);
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        IslandData island = islandManager.getIsland(player.getUniqueId());
        if (island == null) {
            island = islandManager.getOrCreateIsland(player);
        }

        event.setRespawnLocation(islandManager.getIslandTeleportLocation(island));
        Bukkit.getScheduler().runTaskLater(plugin, () -> bossBarManager.show(player), 20L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> bossBarManager.show(player), 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bossBarManager.hide(event.getPlayer());
    }
}