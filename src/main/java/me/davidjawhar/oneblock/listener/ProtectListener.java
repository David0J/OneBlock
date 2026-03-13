package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.island.MobSpawnerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

public class ProtectListener implements Listener {

    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;

    public ProtectListener(OneBlockPlugin plugin, IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        IslandData island = islandManager.getIsland(player.getUniqueId());
        if (island != null && block.getType() == Material.BEDROCK && same(block.getLocation(), island.getX(), island.getY() - 1, island.getZ(), island.getWorldName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        String type = plugin.getCustomItemType(item);
        if (type == null) return;

        Block placed = event.getBlockPlaced();
        Player player = event.getPlayer();
        String world = placed.getWorld().getName();

        switch (type) {
            case OneBlockPlugin.ITEM_ANIMAL_SPAWNER -> {
                islandManager.addSpawner(new MobSpawnerData(player.getUniqueId(), MobSpawnerData.Type.ANIMAL, placed.getX(), placed.getY(), placed.getZ(), world));
                placed.getRelative(BlockFace.UP).setType(Material.DIRT);
                player.sendMessage("§aAnimal bedrock activated.");
            }
            case OneBlockPlugin.ITEM_HOSTILE_SPAWNER -> {
                islandManager.addSpawner(new MobSpawnerData(player.getUniqueId(), MobSpawnerData.Type.HOSTILE, placed.getX(), placed.getY(), placed.getZ(), world));
                placed.getRelative(BlockFace.UP).setType(Material.DIRT);
                player.sendMessage("§cHostile bedrock activated.");
            }
            case OneBlockPlugin.ITEM_END_SPAWNER -> {
                islandManager.addSpawner(new MobSpawnerData(player.getUniqueId(), MobSpawnerData.Type.END, placed.getX(), placed.getY(), placed.getZ(), world));
                placed.getRelative(BlockFace.UP).setType(Material.DIRT);
                player.sendMessage("§5End bedrock activated.");
            }
            case OneBlockPlugin.ITEM_END_PORTAL_CORE -> {
                Location center = placed.getLocation();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        center.getWorld().getBlockAt(center.getBlockX() + dx, center.getBlockY(), center.getBlockZ() + dz).setType(Material.END_PORTAL);
                    }
                }
                player.sendMessage("§5The End portal opens.");
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(islandManager::isAnyIslandCoreBlock);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(islandManager::isAnyIslandCoreBlock);
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(islandManager::isAnyIslandCoreBlock)) event.setCancelled(true);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(islandManager::isAnyIslandCoreBlock)) event.setCancelled(true);
    }

    private boolean same(Location loc, int x, int y, int z, String world) {
        return loc.getWorld() != null && loc.getWorld().getName().equals(world)
                && loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z;
    }
}
