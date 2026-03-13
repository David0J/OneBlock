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
            sender.sendMessage(ChatColor.YELLOW + "/ob home");
            sender.sendMessage(ChatColor.YELLOW + "/ob tp <player>");
            sender.sendMessage(ChatColor.YELLOW + "/ob info [player]");
            sender.sendMessage(ChatColor.YELLOW + "/ob reset <player>");
            sender.sendMessage(ChatColor.YELLOW + "/ob reload");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "home" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                islandManager.teleportHome(player);
                bossBarManager.show(player);
                sender.sendMessage(ChatColor.GREEN + "Teleported to your island.");
                return true;
            }

            case "tp" -> {
                if (!sender.hasPermission("oneblock.admin")) {
                    sender.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Players only.");
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob tp <player>");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found or offline.");
                    return true;
                }

                IslandData island = islandManager.getIsland(target.getUniqueId());
                if (island == null) {
                    sender.sendMessage(ChatColor.RED + "That player has no island yet.");
                    return true;
                }

                Location loc = islandManager.getIslandTeleportLocation(island);
                if (loc == null) {
                    sender.sendMessage(ChatColor.RED + "Island world not loaded.");
                    return true;
                }

                player.teleport(loc);
                sender.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + "'s island.");
                return true;
            }

            case "info" -> {
                if (args.length >= 2) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                    PlayerData data = playerDataManager.getOrCreate(target.getUniqueId(), target.getName() == null ? args[1] : target.getName());
                    IslandData island = islandManager.getIsland(target.getUniqueId());

                    sender.sendMessage(ChatColor.GOLD + "OneBlock info for " + (target.getName() == null ? args[1] : target.getName()) + ":");
                    sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getLevel());
                    sender.sendMessage(ChatColor.YELLOW + "Blocks Broken: " + ChatColor.WHITE + data.getBlocksBroken());

                    if (island != null) {
                        sender.sendMessage(ChatColor.YELLOW + "Coords: " + ChatColor.WHITE + island.getX() + ", " + island.getY() + ", " + island.getZ());
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "Coords: " + ChatColor.WHITE + "No island yet");
                    }
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "Usage: /ob info <player>");
                    return true;
                }

                PlayerData data = playerDataManager.getOrCreate(player.getUniqueId(), player.getName());
                IslandData island = islandManager.getIsland(player.getUniqueId());

                sender.sendMessage(ChatColor.GOLD + "Your OneBlock info:");
                sender.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + data.getLevel());
                sender.sendMessage(ChatColor.YELLOW + "Blocks Broken: " + ChatColor.WHITE + data.getBlocksBroken());

                if (island != null) {
                    sender.sendMessage(ChatColor.YELLOW + "Coords: " + ChatColor.WHITE + island.getX() + ", " + island.getY() + ", " + island.getZ());
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "Coords: " + ChatColor.WHITE + "No island yet");
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
                if (target.getName() == null && !target.hasPlayedBefore()) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("home");
            completions.add("tp");
            completions.add("info");
            completions.add("reset");
            completions.add("reload");
            return filter(completions, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("tp")
                || args[0].equalsIgnoreCase("info")
                || args[0].equalsIgnoreCase("reset"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
            return filter(completions, args[1]);
        }

        return completions;
    }

    private List<String> filter(List<String> input, String prefix) {
        List<String> out = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String s : input) {
            if (s.toLowerCase().startsWith(lower)) {
                out.add(s);
            }
        }
        return out;
    }
}
