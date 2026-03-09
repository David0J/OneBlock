package com.oneblock.commands;

import com.oneblock.OneBlockPlugin;
import com.oneblock.island.IslandData;
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
import java.util.UUID;

public final class OBCommand implements CommandExecutor, TabCompleter {

    private final OneBlockPlugin plugin;

    public OBCommand(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "home" -> handleHome(player);
            case "tp" -> handleTeleport(player, args);
            case "info" -> handleInfo(player, args);
            case "reset" -> handleReset(player, args);
            case "reload" -> handleReload(player);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleHome(Player player) {
        IslandData island = plugin.getIslandManager().getIsland(player.getUniqueId()).orElse(null);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "You do not have an island yet.");
            return;
        }
        player.teleport(island.getTeleportLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to your OneBlock.");
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ob tp <player>");
            return;
        }

        UUID targetUuid = resolvePlayerUuid(args[1]);
        if (targetUuid == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        IslandData island = plugin.getIslandManager().getIsland(targetUuid).orElse(null);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "That player does not have an island.");
            return;
        }

        player.teleport(island.getTeleportLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to " + plugin.getPlayerDataManager().getStoredName(targetUuid) + "'s OneBlock.");
    }

    private void handleInfo(Player player, String[] args) {
        UUID targetUuid = player.getUniqueId();
        if (args.length >= 2) {
            UUID resolved = resolvePlayerUuid(args[1]);
            if (resolved == null) {
                player.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }
            targetUuid = resolved;
        }

        IslandData island = plugin.getIslandManager().getIsland(targetUuid).orElse(null);
        if (island == null) {
            player.sendMessage(ChatColor.RED + "That player does not have an island.");
            return;
        }

        int level = plugin.getPlayerDataManager().getLevel(targetUuid);
        int blocksBroken = plugin.getPlayerDataManager().getBlocksBroken(targetUuid);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "---- OneBlock Info ----");
        player.sendMessage(ChatColor.GRAY + "Player: " + ChatColor.WHITE + plugin.getPlayerDataManager().getStoredName(targetUuid));
        player.sendMessage(ChatColor.GRAY + "Level: " + ChatColor.WHITE + level);
        player.sendMessage(ChatColor.GRAY + "Blocks Broken: " + ChatColor.WHITE + blocksBroken);
        player.sendMessage(ChatColor.GRAY + "Coords: " + ChatColor.WHITE
                + island.getBlockLocation().getBlockX() + ", "
                + island.getBlockLocation().getBlockY() + ", "
                + island.getBlockLocation().getBlockZ());
    }

    private void handleReset(Player player, String[] args) {
        if (!player.hasPermission("oneblock.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /ob reset <player>");
            return;
        }

        Player onlineTarget = Bukkit.getPlayerExact(args[1]);
        if (onlineTarget == null) {
            player.sendMessage(ChatColor.RED + "That player must be online to reset their island.");
            return;
        }

        plugin.getIslandManager().resetIsland(onlineTarget);
        plugin.getLevelBarManager().update(onlineTarget);
        player.sendMessage(ChatColor.GREEN + "Reset island for " + onlineTarget.getName() + ".");
        onlineTarget.sendMessage(ChatColor.YELLOW + "Your OneBlock island has been reset.");
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("oneblock.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission.");
            return;
        }

        plugin.reloadConfig();
        plugin.getPlayerDataManager().reload();
        plugin.getIslandManager().loadFromData();
        for (Player online : Bukkit.getOnlinePlayers()) {
            plugin.getLevelBarManager().update(online);
        }
        player.sendMessage(ChatColor.GREEN + "OneBlock reloaded.");
    }

    private UUID resolvePlayerUuid(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return online.getUniqueId();
        }

        UUID stored = plugin.getPlayerDataManager().findUuidByName(input);
        if (stored != null) {
            return stored;
        }

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(input)) {
                return offlinePlayer.getUniqueId();
            }
        }
        return null;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "OneBlock commands:");
        player.sendMessage(ChatColor.GRAY + "/ob home");
        player.sendMessage(ChatColor.GRAY + "/ob tp <player>");
        player.sendMessage(ChatColor.GRAY + "/ob info [player]");
        if (player.hasPermission("oneblock.admin")) {
            player.sendMessage(ChatColor.GRAY + "/ob reset <player>");
            player.sendMessage(ChatColor.GRAY + "/ob reload");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("home");
            completions.add("tp");
            completions.add("info");
            if (sender.hasPermission("oneblock.admin")) {
                completions.add("reset");
                completions.add("reload");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("reset"))) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                completions.add(online.getName());
            }
            return filter(completions, args[1]);
        }

        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        String lower = input.toLowerCase();
        return options.stream().filter(option -> option.toLowerCase().startsWith(lower)).toList();
    }
}
