package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class JoinListener implements Listener {

    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final BossBarManager bossBarManager;

    public JoinListener(OneBlockPlugin plugin,
                        IslandManager islandManager,
                        BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        IslandData island = islandManager.getOrCreateIsland(player);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.teleport(getIslandLocation(island));
            bossBarManager.show(player);
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        IslandData island = islandManager.getIsland(player.getUniqueId());
        if (island == null) return;

        event.setRespawnLocation(getIslandLocation(island));
        Bukkit.getScheduler().runTaskLater(plugin, () -> bossBarManager.show(player), 20L);
    }

    private Location getIslandLocation(IslandData island) {
        String worldName = plugin.getConfig().getString("world-name", "oneblock_world");
        World world = Bukkit.getWorld(worldName);
        return new Location(world, island.getX() + 0.5, island.getY() + 1.0, island.getZ() + 0.5);
    }
}
