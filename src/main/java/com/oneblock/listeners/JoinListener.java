package com.oneblock.listeners;

import com.oneblock.OneBlockPlugin;
import com.oneblock.island.IslandData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

    private final OneBlockPlugin plugin;

    public JoinListener(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().touchPlayerName(player);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            IslandData island = plugin.getIslandManager().getIsland(player.getUniqueId())
                    .orElseGet(() -> plugin.getIslandManager().createIsland(player));

            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(island.getTeleportLocation());
            plugin.getLevelBarManager().show(player);
        });
    }
}
