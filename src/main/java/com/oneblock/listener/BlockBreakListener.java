package com.oneblock.listener;

import com.oneblock.OneBlockPlugin;
import com.oneblock.data.LevelUpdateResult;
import com.oneblock.data.PlayerData;
import com.oneblock.island.IslandData;
import org.bukkit.Bukkit;
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
        IslandData island = plugin.getIslandManager().getIsland(player);
        if (island == null) {
            return;
        }

        Location islandBlock = island.blockLocation();
        if (!event.getBlock().getLocation().equals(islandBlock)) {
            return;
        }

        LevelUpdateResult result = plugin.getPlayerDataManager().addBrokenBlock(player);
        PlayerData data = plugin.getPlayerDataManager().getData(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Material next = plugin.getLevelManager().getRandomBlock(data.getLevel());
            islandBlock.getBlock().setType(next, false);
        }, 1L);

        plugin.getBossBarManager().update(player);

        if (result.leveledUp()) {
            player.sendMessage("§dYou reached Level " + result.newLevel() + "!");
        }
    }
}
