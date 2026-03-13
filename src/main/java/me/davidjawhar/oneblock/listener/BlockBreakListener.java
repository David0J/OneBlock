package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerData;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final BossBarManager bossBarManager;

    public BlockBreakListener(OneBlockPlugin plugin, IslandManager islandManager, PlayerDataManager playerDataManager, LevelManager levelManager, BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
        this.bossBarManager = bossBarManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!islandManager.isOfficialOneBlock(block)) return;

        if (!islandManager.isPlayerOneBlock(player, block)) {
            if (plugin.getConfig().getBoolean("owner-only-break", true)) {
                return;
            }
        }

        PlayerData data = playerDataManager.getOrCreate(player.getUniqueId(), player.getName());
        long oldBroken = data.getBlocksBroken();
        int oldLevel = data.getLevel();
        data.setBlocksBroken(oldBroken + 1);
        int newLevel = levelManager.getLevelForBroken(data.getBlocksBroken());
        data.setLevel(newLevel);
        playerDataManager.save();

        islandManager.regenerateOneBlock(player, block);
        bossBarManager.update(player);

        if (newLevel > oldLevel) {
            player.sendMessage(ChatColor.LIGHT_PURPLE + "You reached Level " + newLevel + "!");
        }
    }
}
