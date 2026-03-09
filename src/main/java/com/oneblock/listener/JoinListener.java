package com.oneblock.listener;

import com.oneblock.OneBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinListener implements Listener {

    private final OneBlockPlugin plugin;

    public JoinListener(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerDataManager().ensurePlayer(player);
        plugin.getIslandManager().ensureIsland(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(plugin.getIslandManager().getSpawnLocation(player.getUniqueId()));
            plugin.getBossBarManager().show(player);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBossBarManager().remove(event.getPlayer());
    }
}
