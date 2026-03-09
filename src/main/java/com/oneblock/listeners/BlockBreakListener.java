package com.oneblock.listeners;

import com.oneblock.OneBlockPlugin;
import com.oneblock.island.IslandData;
import com.oneblock.level.LevelManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final OneBlockPlugin plugin;

    public BlockBreakListener(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        IslandData island = plugin.getIslandManager().getIsland(player.getUniqueId()).orElse(null);
        if (island == null) {
            return;
        }

        Location broken = event.getBlock().getLocation();
        Location oneBlock = island.getBlockLocation();
        if (!sameBlock(broken, oneBlock)) {
            return;
        }

        plugin.getPlayerDataManager().incrementBrokenBlocks(player);
        plugin.getLevelBarManager().update(player);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Material nextBlock = LevelManager.getRandomBlock(plugin.getPlayerDataManager().getLevel(player));
            oneBlock.getBlock().setType(nextBlock, false);
            oneBlock.clone().add(0, -1, 0).getBlock().setType(Material.BEDROCK, false);
        }, 1L);
    }

    private boolean sameBlock(Location a, Location b) {
        return a.getWorld() != null
                && b.getWorld() != null
                && a.getWorld().getUID().equals(b.getWorld().getUID())
                && a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ();
    }
}
