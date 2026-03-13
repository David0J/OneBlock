package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class BlockBreakListener implements Listener {

    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;

    public BlockBreakListener(OneBlockPlugin plugin,
                              IslandManager islandManager,
                              PlayerDataManager playerDataManager,
                              LevelManager levelManager) {

        this.plugin = plugin;
        this.islandManager = islandManager;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        UUID playerId = event.getPlayer().getUniqueId();

        IslandData island = islandManager.getIsland(playerId);

        if (island == null) return;

        if (!island.isOneBlock(block.getLocation())) return;

        event.setCancelled(true);

        if (block.getType() == Material.BEDROCK) return;

        playerDataManager.incrementBlocksBroken(playerId);

        Material nextMaterial = levelManager.getNextBlock(playerId);

        long delay = plugin.getConfig().getLong("regeneration-delay-ticks", 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (!nextMaterial.isSolid()) {

                block.setType(Material.DIRT);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block above = block.getRelative(BlockFace.UP);
                    above.setType(nextMaterial);
                });

            } else {

                block.setType(nextMaterial);

            }

        }, delay);
    }
}
