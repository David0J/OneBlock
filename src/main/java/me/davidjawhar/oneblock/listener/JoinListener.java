package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
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

    public JoinListener(OneBlockPlugin plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        IslandData island = islandManager.getOrCreateIsland(player);

        teleportToIsland(player, island);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();

        IslandData island = islandManager.getIsland(player.getUniqueId());

        if (island != null) {
            Location location = getIslandLocation(island);
            event.setRespawnLocation(location);
        }
    }

    private void teleportToIsland(Player player, IslandData island) {

        Location location = getIslandLocation(island);

        Bukkit.getScheduler().runTask(plugin, () -> player.teleport(location));
    }

    private Location getIslandLocation(IslandData island) {

        World world = Bukkit.getWorld(plugin.getConfig().getString("world-name", "oneblock_world"));

        return new Location(
                world,
                island.getX() + 0.5,
                island.getY() + 1,
                island.getZ() + 0.5
        );
    }
}
