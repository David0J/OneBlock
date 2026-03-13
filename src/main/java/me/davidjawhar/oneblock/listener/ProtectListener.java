package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.UUID;

public class ProtectListener implements Listener {

    private final IslandManager islandManager;

    public ProtectListener(IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        IslandData island = islandManager.getIsland(player.getUniqueId());
        if (island == null) return;

        if (isCoreBlock(block, island)) {
            if (block.getType() == Material.BEDROCK) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isAnyIslandCoreBlock);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isAnyIslandCoreBlock);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isAnyIslandCoreBlock)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isAnyIslandCoreBlock)) {
            event.setCancelled(true);
        }
    }

    private boolean isCoreBlock(Block block, IslandData island) {
        Location loc = block.getLocation();

        // Main OneBlock
        if (island.isOneBlock(loc)) {
            return true;
        }

        // Bedrock under the OneBlock
        return loc.getWorld() != null
                && loc.getWorld().getName().equals(island.getWorldName())
                && loc.getBlockX() == island.getX()
                && loc.getBlockY() == island.getY() - 1
                && loc.getBlockZ() == island.getZ();
    }

    private boolean isAnyIslandCoreBlock(Block block) {
        Location loc = block.getLocation();

        for (IslandData island : islandManager.getAllIslands().values()) {
            if (loc.getWorld() == null) continue;
            if (!loc.getWorld().getName().equals(island.getWorldName())) continue;

            // OneBlock
            if (island.isOneBlock(loc)) {
                return true;
            }

            // Bedrock under it
            if (loc.getBlockX() == island.getX()
                    && loc.getBlockY() == island.getY() - 1
                    && loc.getBlockZ() == island.getZ()) {
                return true;
            }
        }

        return false;
    }
}
