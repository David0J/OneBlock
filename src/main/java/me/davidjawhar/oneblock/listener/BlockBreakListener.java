package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.UUID;

public class BlockBreakListener implements Listener {

    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final BossBarManager bossBarManager;

    public BlockBreakListener(OneBlockPlugin plugin,
                              IslandManager islandManager,
                              PlayerDataManager playerDataManager,
                              LevelManager levelManager,
                              BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block block = event.getBlock();

        IslandData island = islandManager.getIsland(playerId);
        if (island == null) return;

        if (!island.isOneBlock(block.getLocation())) return;

        if (!playerId.equals(island.getOwner())) {
            event.setCancelled(true);
            return;
        }

        if (block.getType() == Material.BEDROCK) {
            event.setCancelled(true);
            return;
        }

        Material oldType = block.getType();
        event.setDropItems(true);

        playerDataManager.incrementBlocksBroken(playerId, player.getName());
        bossBarManager.update(player);

        long delay = plugin.getConfig().getLong("regeneration-delay-ticks", 1L);
        Material nextMaterial = levelManager.getNextBlock(playerId, player.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (block.getType() != Material.AIR && block.getType() != oldType) {
                return;
            }

            Block above = block.getRelative(BlockFace.UP);
            if (above.getType() != Material.AIR && !above.isPassable()) {
                above.setType(Material.AIR);
            }

            if (!nextMaterial.isSolid()) {
                block.setType(Material.DIRT);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (above.getType() == Material.AIR || above.isPassable()) {
                        above.setType(nextMaterial);
                    }
                });
            } else {
                if (above.getType() != Material.AIR && above.isPassable()) {
                    above.setType(Material.AIR);
                }
                block.setType(nextMaterial);
            }
        }, delay);
    }
}
