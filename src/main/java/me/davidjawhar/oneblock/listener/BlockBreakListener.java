package me.davidjawhar.oneblock.listener;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.island.MobSpawnerData;
import me.davidjawhar.oneblock.level.GenerationOutcome;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerData;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;
import java.util.List;
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

        MobSpawnerData spawner = islandManager.getSpawnerByTop(block);
        if (spawner != null) {
            handleMobSpawnerBreak(event, player, block, spawner);
            return;
        }

        IslandData island = islandManager.getIsland(playerId);
        if (island == null || !island.isOneBlock(block.getLocation())) return;

        if (!playerId.equals(island.getOwner())) {
            event.setCancelled(true);
            player.sendMessage("§cOnly the island owner can break the main OneBlock.");
            return;
        }

        if (block.getType() == Material.BEDROCK) {
            event.setCancelled(true);
            return;
        }

        Material oldType = block.getType();
        event.setDropItems(false);

        dropNormalOrSpecialLoot(player, block, oldType);

        playerDataManager.incrementBlocksBroken(playerId, player.getName());
        if (oldType == Material.IRON_ORE) {
            playerDataManager.incrementIronOresBroken(playerId, player.getName());
        }

        applyHunger(player);
        maybeGiveMilestoneItems(player);
        bossBarManager.update(player);

        long delay = plugin.getConfig().getLong("regeneration-delay-ticks", 20L);
        GenerationOutcome nextOutcome = levelManager.getNextOutcome(playerId, player.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> regenerateMainOneBlock(player, block, nextOutcome), delay);
    }

    private void handleMobSpawnerBreak(BlockBreakEvent event, Player player, Block block, MobSpawnerData spawner) {
        if (!spawner.getOwner().equals(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§cOnly the spawner owner can use this bedrock.");
            return;
        }

        event.setDropItems(false);
        long delay = plugin.getConfig().getLong("regeneration-delay-ticks", 20L);
        spawnMob(block.getLocation(), spawner.getType());
        Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.DIRT), delay);
    }

    private void regenerateMainOneBlock(Player player, Block block, GenerationOutcome outcome) {
        Block above = block.getRelative(BlockFace.UP);
        if (!above.isEmpty()) above.setType(Material.AIR);

        Material display = outcome.getDisplayMaterial();

        if (!display.isSolid()) {
            block.setType(Material.DIRT);
            Bukkit.getScheduler().runTask(plugin, () -> above.setType(display));
        } else {
            block.setType(display);
        }

        switch (outcome.getType()) {
            case TREE_BLOCK -> {
                block.setType(Material.DIRT);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    above.setType(Material.AIR);
                    block.getWorld().generateTree(block.getLocation(), pickTreeType(player));
                }, 1L);
            }
            case ORE_CLUSTER_BLOCK -> Bukkit.getScheduler().runTaskLater(plugin, () -> generateOreCluster(player, block), 1L);

            // TNT now stays as a normal TNT block and does NOT ignite automatically
            case TNT_BLOCK -> block.setType(Material.TNT);

            default -> {}
        }
    }

    private void dropNormalOrSpecialLoot(Player player, Block block, Material oldType) {
        World world = block.getWorld();
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        switch (oldType) {
            case BARREL -> dropChestLoot(world, dropLoc, uuid, name);

            // TNT should behave like a normal block reward now
            case TNT -> world.dropItemNaturally(dropLoc, new ItemStack(Material.TNT, 1));

            default -> {
                Collection<ItemStack> normalDrops = block.getDrops(player.getInventory().getItemInMainHand(), player);
                if (normalDrops.isEmpty()) {
                    normalDrops = List.of(new ItemStack(oldType));
                }
                for (ItemStack drop : normalDrops) {
                    world.dropItemNaturally(dropLoc, drop);
                    if (levelManager.shouldGiveBonusDrops(uuid, name)) {
                        ItemStack extra = drop.clone();
                        extra.setAmount(Math.min(64, levelManager.bonusDropAmount(uuid, name)));
                        world.dropItemNaturally(dropLoc, extra);
                    }
                }
                if (oldType == Material.ICE) {
                    world.dropItemNaturally(dropLoc, new ItemStack(Material.WATER_BUCKET));
                }
                if (oldType == Material.BASALT) {
                    world.dropItemNaturally(dropLoc, new ItemStack(Material.LAVA_BUCKET));
                }
            }
        }
    }

    private void dropChestLoot(World world, Location dropLoc, UUID uuid, String name) {
        world.dropItemNaturally(dropLoc, new ItemStack(Material.BREAD, 2));
        if (Math.random() < 0.6) world.dropItemNaturally(dropLoc, new ItemStack(Material.OAK_SAPLING, 2));
        if (Math.random() < 0.5) world.dropItemNaturally(dropLoc, new ItemStack(Material.COAL, 4));
        if (Math.random() < 0.4) world.dropItemNaturally(dropLoc, new ItemStack(Material.IRON_INGOT, 2));
        if (Math.random() < 0.25) world.dropItemNaturally(dropLoc, new ItemStack(Material.GOLDEN_APPLE, 1));
        if (playerDataManager.getIronOresBroken(uuid, name) >= 5 && Math.random() < 0.25) {
            world.dropItemNaturally(dropLoc, new ItemStack(Material.WATER_BUCKET, 1));
        }
        if (levelManager.getLevel(uuid, name) >= 3 && Math.random() < 0.2) {
            world.dropItemNaturally(dropLoc, new ItemStack(Material.LAVA_BUCKET, 1));
        }
        if (levelManager.getLevel(uuid, name) >= 4 && Math.random() < 0.12) {
            world.dropItemNaturally(dropLoc, plugin.createCustomItem(Material.CRYING_OBSIDIAN, "§5End Portal Core", OneBlockPlugin.ITEM_END_PORTAL_CORE));
        }
    }

    private void generateOreCluster(Player player, Block center) {
        Material centerMaterial = center.getType();
        center.setType(Material.DIRT);
        int[][] offsets = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] off : offsets) {
            Block target = center.getWorld().getBlockAt(center.getX() + off[0], center.getY(), center.getZ() + off[1]);
            if (target.getType() == Material.AIR) {
                target.setType(levelManager.randomOreForCluster(player.getUniqueId(), player.getName()));
            }
        }
        if (centerMaterial == Material.TUFF) {
            center.getWorld().playSound(center.getLocation(), Sound.BLOCK_STONE_BREAK, 1f, 1f);
        }
    }

    private void spawnMob(Location loc, MobSpawnerData.Type type) {
        Location spawnLoc = loc.add(0.5, 1.0, 0.5);
        World world = spawnLoc.getWorld();
        if (world == null) return;

        EntityType entityType = switch (type) {
            case ANIMAL -> pickAnimalType();
            case HOSTILE -> pickHostileType();
            case END -> EntityType.ENDERMAN;
        };
        world.spawnEntity(spawnLoc, entityType);
    }

    private EntityType pickAnimalType() {
        double roll = Math.random();
        if (roll < 0.6) return EntityType.SHEEP;
        if (roll < 0.78) return EntityType.COW;
        if (roll < 0.9) return EntityType.CHICKEN;
        return EntityType.PIG;
    }

    private EntityType pickHostileType() {
        double roll = Math.random();
        if (roll < 0.4) return EntityType.ZOMBIE;
        if (roll < 0.75) return EntityType.SKELETON;
        if (roll < 0.92) return EntityType.SPIDER;
        return EntityType.BLAZE;
    }

    private TreeType pickTreeType(Player player) {
        int level = levelManager.getLevel(player.getUniqueId(), player.getName());
        if (level >= 3 && Math.random() < 0.2) return TreeType.DARK_OAK;
        if (level >= 2 && Math.random() < 0.2) return TreeType.REDWOOD;
        if (Math.random() < 0.3) return TreeType.BIRCH;
        return TreeType.TREE;
    }

    private void applyHunger(Player player) {
        PlayerData data = playerDataManager.get(player.getUniqueId(), player.getName());
        if (data.getBlocksBroken() >= data.getNextHungerCheckpoint()) {
            player.setFoodLevel(Math.max(0, player.getFoodLevel() - 1));
            data.setNextHungerCheckpoint(data.getNextHungerCheckpoint() + 75);
        }
    }

    private void maybeGiveMilestoneItems(Player player) {
        PlayerData data = playerDataManager.get(player.getUniqueId(), player.getName());
        int level = levelManager.getLevel(player.getUniqueId(), player.getName());
        PlayerInventory inv = player.getInventory();

        if (level >= 1 && !data.isAnimalSpawnerGiven()) {
            inv.addItem(plugin.createCustomItem(Material.BEDROCK, "§aAnimal Bedrock", OneBlockPlugin.ITEM_ANIMAL_SPAWNER));
            data.setAnimalSpawnerGiven(true);
            player.sendMessage("§aYou unlocked Animal Bedrock.");
        }
        if (level >= 3 && !data.isHostileSpawnerGiven()) {
            inv.addItem(plugin.createCustomItem(Material.BEDROCK, "§cHostile Bedrock", OneBlockPlugin.ITEM_HOSTILE_SPAWNER));
            data.setHostileSpawnerGiven(true);
            player.sendMessage("§cYou unlocked Hostile Bedrock.");
        }
        if (level >= 4 && !data.isEndSpawnerGiven()) {
            inv.addItem(plugin.createCustomItem(Material.BEDROCK, "§5End Bedrock", OneBlockPlugin.ITEM_END_SPAWNER));
            data.setEndSpawnerGiven(true);
            player.sendMessage("§5You unlocked End Bedrock.");
        }
    }
}