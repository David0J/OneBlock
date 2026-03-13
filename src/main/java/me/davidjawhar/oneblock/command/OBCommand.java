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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class OBCommand implements CommandExecutor, TabCompleter {
    private final OneBlockPlugin plugin;
    private final IslandManager islandManager;
    private final PlayerDataManager playerDataManager;
    private final LevelManager levelManager;
    private final BossBarManager bossBarManager;

    public OBCommand(OneBlockPlugin plugin, IslandManager islandManager, PlayerDataManager playerDataManager, LevelManager levelManager, BossBarManager bossBarManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
        this.playerDataManager = playerDataManager;
        this.levelManager = levelManager;
        this.bossBarManager = bossBarManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/ob home, /ob tp <player>, /ob info [player], /ob reset <player>, /ob reload");
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "home" -> {
                if (!(sender instanceof Player player)) return onlyPlayer(sender);
                if (!player.hasPermission("oneblock.home")) return noPerm(player);
                islandManager.teleportHome(player);
                bossBarManager.show(player);
                player.sendMessage(ChatColor.GREEN + "Teleported to your island.");
                return true;
            }
            case "tp" -> {
                if (!(sender instanceof Player player)) return onlyPlayer(sender);
                if (!player.hasPermission("oneblock.tp")) return noPerm(player);
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /ob tp <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found online.");
                    return true;
                }
                IslandData island = islandManager.getIsland(target.getUniqueId());
                if (island == null) {
                    player.sendMessage(ChatColor.RED + "That player has no island yet.");
                    return true;
                }
                player.teleport(island.getTeleportLocation(Objects.requireNonNull(islandManager.getWorld())));
                player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + "'s island.");
                return true;
            }
            case "info" -> {
                OfflinePlayer target;
                if (args.length >= 2) target = Bukkit.getOfflinePlayer(args[1]);
                else if (sender instanceof Player p) target = p;
                else {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob info <player>");
                    return true;
                }
                PlayerData data = playerDataManager.get(target.getUniqueId());
                IslandData island = islandManager.getIsland(target.getUniqueId());
                if (data == null || island == null) {
                    sender.sendMessage(ChatColor.RED + "No OneBlock data for that player.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "OneBlock info for " + target.getName());
                sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getLevel());
                sender.sendMessage(ChatColor.YELLOW + "Blocks broken: " + ChatColor.WHITE + data.getBlocksBroken());
                sender.sendMessage(ChatColor.YELLOW + "Coordinates: " + ChatColor.WHITE + island.getX() + ", " + island.getY() + ", " + island.getZ());
                return true;
            }
            case "reset" -> {
                if (!sender.hasPermission("oneblock.admin")) return noPerm(sender);
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob reset <player>");
                    return true;
                }
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                islandManager.resetIsland(target.getUniqueId(), target.getName() == null ? args[1] : target.getName());
                Player online = target.getPlayer();
                if (online != null) {
                    islandManager.teleportHome(online);
                    bossBarManager.show(online);
                }
                sender.sendMessage(ChatColor.GREEN + "Reset island for " + args[1] + ".");
                return true;
            }
            case "reload" -> {
                if (!sender.hasPermission("oneblock.admin")) return noPerm(sender);
                plugin.reloadEverything();
                sender.sendMessage(ChatColor.GREEN + "OneBlock reloaded.");
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                return true;
            }
        }
    }

    private boolean noPerm(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "No permission.");
        return true;
    }

    private boolean onlyPlayer(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Players only.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("home", "tp", "info", "reset", "reload"), args[0]);
        }
        if (args.length == 2 && List.of("tp", "info", "reset").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return filter(names, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> input, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return input.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(lower)).collect(Collectors.toList());
    }
}
