package me.davidjawhar.oneblock;

import me.davidjawhar.oneblock.command.OBCommand;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.listener.BlockBreakListener;
import me.davidjawhar.oneblock.listener.JoinListener;
import me.davidjawhar.oneblock.listener.ProtectListener;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import me.davidjawhar.oneblock.world.VoidChunkGenerator;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class OneBlockPlugin extends JavaPlugin {

    public static final String ITEM_ANIMAL_SPAWNER = "animal_spawner";
    public static final String ITEM_HOSTILE_SPAWNER = "hostile_spawner";
    public static final String ITEM_END_SPAWNER = "end_spawner";
    public static final String ITEM_END_PORTAL_CORE = "end_portal_core";

    private IslandManager islandManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private BossBarManager bossBarManager;
    private NamespacedKey itemTypeKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.itemTypeKey = new NamespacedKey(this, "custom_item_type");

        createVoidWorld();

        this.playerDataManager = new PlayerDataManager(this);
        this.islandManager = new IslandManager(this);
        this.levelManager = new LevelManager(this, playerDataManager);
        this.bossBarManager = new BossBarManager(this, playerDataManager, levelManager);

        getServer().getPluginManager().registerEvents(new JoinListener(this, islandManager, bossBarManager), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this, islandManager, playerDataManager, levelManager, bossBarManager), this);
        getServer().getPluginManager().registerEvents(new ProtectListener(this, islandManager), this);

        OBCommand obCommand = new OBCommand(this, islandManager, playerDataManager, levelManager, bossBarManager);
        if (getCommand("ob") != null) {
            getCommand("ob").setExecutor(obCommand);
            getCommand("ob").setTabCompleter(obCommand);
        }

        registerRecipes();

        Bukkit.getScheduler().runTaskTimer(this, bossBarManager::updateAll, 40L, 40L);

        // show bar for players already online after /reload or plugin reload
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                bossBarManager.show(player);
            }
        }, 20L);

        getLogger().info("OneBlock enabled.");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) playerDataManager.save();
        if (islandManager != null) islandManager.save();
        if (bossBarManager != null) bossBarManager.removeAll();
    }

    private void createVoidWorld() {
        String worldName = getConfig().getString("world-name", "oneblock_world");
        if (Bukkit.getWorld(worldName) != null) return;
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new VoidChunkGenerator());
        creator.generateStructures(false);
        Bukkit.createWorld(creator);
    }

    private void registerRecipes() {
        ItemStack endCore = createCustomItem(Material.CRYING_OBSIDIAN, "§5End Portal Core", ITEM_END_PORTAL_CORE);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(this, "end_portal_core"), endCore);
        recipe.shape("EOE", "ODO", "EOE");
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('O', Material.OBSIDIAN);
        recipe.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(recipe);
    }

    public ItemStack createCustomItem(Material material, String displayName, String type) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(itemTypeKey, PersistentDataType.STRING, type);
            item.setItemMeta(meta);
        }
        return item;
    }

    public String getCustomItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
    }

    public IslandManager getIslandManager() { return islandManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public LevelManager getLevelManager() { return levelManager; }
    public BossBarManager getBossBarManager() { return bossBarManager; }
}