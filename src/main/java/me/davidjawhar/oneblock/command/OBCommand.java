package me.davidjawhar.oneblock.command;

import me.davidjawhar.oneblock.OneBlockPlugin;
import me.davidjawhar.oneblock.island.IslandData;
import me.davidjawhar.oneblock.island.IslandManager;
import me.davidjawhar.oneblock.level.LevelManager;
import me.davidjawhar.oneblock.player.PlayerData;
import me.davidjawhar.oneblock.player.PlayerDataManager;
import me.davidjawhar.oneblock.ui.BossBarManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OBCommand implements CommandExecutor, TabCompleter {

    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final BossBarManager bossBarManager;

    public OBCommand(OneBlockPlugin plugin,
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/ob home, /ob tp <player>, /ob info [player], /ob trust <player>, /ob untrust <player>, /ob trusted, /ob reset <player>, /ob reload");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "home" -> {
                if (!(sender instanceof Player player)) return playersOnly(sender);
                islandManager.teleportHome(player);
                bossBarManager.show(player);
                return true;
            }
            case "tp" -> {
                if (!(sender instanceof Player player)) return playersOnly(sender);
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob tp <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                IslandData island = islandManager.getIsland(target.getUniqueId());
                if (island == null) {
                    sender.sendMessage(ChatColor.RED + "That player has no island yet.");
                    return true;
                }
                boolean allowed = sender.hasPermission("oneblock.admin") || island.isTrusted(player.getUniqueId());
                if (!allowed) {
                    sender.sendMessage(ChatColor.RED + "You are not trusted on that island.");
                    return true;
                }
                Location loc = islandManager.getIslandTeleportLocation(island);
                if (loc != null) player.teleport(loc);
                return true;
            }
            case "info" -> {
                if (args.length >= 2) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    PlayerData data = playerDataManager.getOrCreate(target.getUniqueId(), target.getName() == null ? args[1] : target.getName());
                    IslandData island = islandManager.getIsland(target.getUniqueId());
                    sendInfo(sender, target.getName() == null ? args[1] : target.getName(), data, island);
                    return true;
                }
                if (!(sender instanceof Player player)) return playersOnly(sender);
                sendInfo(sender, player.getName(), playerDataManager.getOrCreate(player.getUniqueId(), player.getName()), islandManager.getIsland(player.getUniqueId()));
                return true;
            }
            case "trust" -> {
                if (!(sender instanceof Player player)) return playersOnly(sender);
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob trust <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player must be online.");
                    return true;
                }
                islandManager.trustPlayer(player.getUniqueId(), target.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Trusted " + target.getName());
                return true;
            }
            case "untrust" -> {
                if (!(sender instanceof Player player)) return playersOnly(sender);
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob untrust <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                islandManager.untrustPlayer(player.getUniqueId(), target.getUniqueId());
                sender.sendMessage(ChatColor.YELLOW + "Untrusted " + (target.getName() == null ? args[1] : target.getName()));
                return true;
            }
            case "trusted" -> {
                if (!(sender instanceof Player player)) return playersOnly(sender);
                IslandData island = islandManager.getIsland(player.getUniqueId());
                if (island == null || island.getTrusted().isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "No trusted players.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "Trusted players:");
                for (UUID uuid : island.getTrusted()) {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                    sender.sendMessage(ChatColor.YELLOW + "- " + (op.getName() == null ? uuid.toString() : op.getName()));
                }
                return true;
            }
            case "reset" -> {
                if (!sender.hasPermission("oneblock.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob reset <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                playerDataManager.reset(target.getUniqueId(), target.getName() == null ? args[1] : target.getName());
                if (target.isOnline()) {
                    Player onlineTarget = Objects.requireNonNull(target.getPlayer());
                    islandManager.resetIsland(onlineTarget);
                    bossBarManager.update(onlineTarget);
                }
                sender.sendMessage(ChatColor.GREEN + "Reset OneBlock progress for " + (target.getName() == null ? args[1] : target.getName()) + ".");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("oneblock.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                islandManager.load();
                playerDataManager.load();
                bossBarManager.updateAll();
                sender.sendMessage(ChatColor.GREEN + "OneBlock config reloaded.");
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
            }
        }
    }

    private void sendInfo(CommandSender sender, String name, PlayerData data, IslandData island) {
        sender.sendMessage(ChatColor.GOLD + "OneBlock info for " + name + ":");
        sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getLevel());
        sender.sendMessage(ChatColor.YELLOW + "Blocks Broken: " + ChatColor.WHITE + data.getBlocksBroken());
        sender.sendMessage(ChatColor.YELLOW + "Iron Ores Broken: " + ChatColor.WHITE + data.getIronOresBroken());
        if (island != null) {
            sender.sendMessage(ChatColor.YELLOW + "Coords: " + ChatColor.WHITE + island.getX() + ", " + island.getY() + ", " + island.getZ());
        }
    }

    private boolean playersOnly(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Players only.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(List.of("home", "tp", "info", "trust", "untrust", "trusted", "reset", "reload"));
            return filter(completions, args[0]);
        }
        if (args.length == 2 && List.of("tp", "info", "reset", "trust", "untrust").contains(args[0].toLowerCase())) {
            for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
            return filter(completions, args[1]);
        }
        return completions;
    }

    private List<String> filter(List<String> input, String prefix) {
        List<String> out = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String s : input) if (s.toLowerCase().startsWith(lower)) out.add(s);
        return out;
    }
}
