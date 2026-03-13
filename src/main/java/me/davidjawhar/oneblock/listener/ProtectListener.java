package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProtectListener implements Listener {
    private final IslandManager islandManager;

    public ProtectListener(me.davidjawhar.oneblock.OneBlockPlugin plugin, IslandManager islandManager) {
        this.islandManager = islandManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        for (IslandData data : islandManager.getIslands().values()) {
            if (block.getWorld().getName().equals(data.getWorldName())
                    && block.getX() == data.getX()
                    && block.getY() == data.getY() - 1
                    && block.getZ() == data.getZ()) {
                event.setCancelled(true);
                return;
            }
        }
        if (islandManager.isOfficialOneBlock(block) && islandManager.getOwner(block) != null
                && !event.getPlayer().getUniqueId().equals(islandManager.getOwner(block))
                && event.getPlayer().hasPermission("oneblock.admin") == false) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isCoreProtected);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isCoreProtected)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isCoreProtected)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLiquid(BlockFromToEvent event) {
        if (isCoreProtected(event.getToBlock())) event.setCancelled(true);
    }

    private boolean isCoreProtected(Block block) {
        if (islandManager.isOfficialOneBlock(block)) return true;
        for (IslandData data : islandManager.getIslands().values()) {
            if (block.getWorld().getName().equals(data.getWorldName())
                    && block.getX() == data.getX()
                    && block.getY() == data.getY() - 1
                    && block.getZ() == data.getZ()) return block.getType() == Material.BEDROCK;
        }
        return false;
    }
}
