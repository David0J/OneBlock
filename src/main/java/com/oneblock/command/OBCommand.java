package com.oneblock.command;

import com.oneblock.OneBlockPlugin;
import com.oneblock.data.PlayerData;
import com.oneblock.island.IslandData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "home" -> {
                player.teleport(plugin.getIslandManager().getSpawnLocation(player.getUniqueId()));
                player.sendMessage("§dTeleported to your OneBlock.");
            }
            case "tp" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ob tp <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage("§cThat player is not online.");
                    return true;
                }
                player.teleport(plugin.getIslandManager().getSpawnLocation(target.getUniqueId()));
                player.sendMessage("§dTeleported to " + target.getName() + "'s OneBlock.");
            }
            case "info" -> {
                Player target = player;
                if (args.length >= 2) {
                    Player onlineTarget = Bukkit.getPlayerExact(args[1]);
                    if (onlineTarget == null) {
                        player.sendMessage("§cThat player is not online.");
                        return true;
                    }
                    target = onlineTarget;
                }

                PlayerData data = plugin.getPlayerDataManager().getData(target);
                IslandData island = plugin.getIslandManager().getIsland(target);
                if (island == null) {
                    player.sendMessage("§cNo island found.");
                    return true;
                }

                Location loc = island.blockLocation();
                player.sendMessage("§dOneBlock info for §f" + target.getName());
                player.sendMessage("§7Level: §f" + data.getLevel());
                player.sendMessage("§7Blocks broken: §f" + data.getBlocksBroken());
                player.sendMessage("§7Coordinates: §f" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            }
            case "reset" -> {
                if (!player.hasPermission("oneblock.admin")) {
                    player.sendMessage("§cYou do not have permission.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /ob reset <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    player.sendMessage("§cThat player must be online to reset.");
                    return true;
                }
                plugin.getIslandManager().reset(target);
                plugin.getBossBarManager().update(target);
                target.teleport(plugin.getIslandManager().getSpawnLocation(target.getUniqueId()));
                player.sendMessage("§dReset " + target.getName() + "'s OneBlock.");
                target.sendMessage("§dYour OneBlock was reset.");
            }
            case "reload" -> {
                if (!player.hasPermission("oneblock.admin")) {
                    player.sendMessage("§cYou do not have permission.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getLevelManager().reload();
                plugin.getIslandManager().save();
                plugin.getPlayerDataManager().save();
                Bukkit.getOnlinePlayers().forEach(plugin.getBossBarManager()::update);
                player.sendMessage("§dOneBlock config reloaded.");
            }
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§d/ob home §7- teleport to your OneBlock");
        player.sendMessage("§d/ob tp <player> §7- teleport to a player's OneBlock");
        player.sendMessage("§d/ob info [player] §7- show OneBlock info");
        if (player.hasPermission("oneblock.admin")) {
            player.sendMessage("§d/ob reset <player> §7- reset a player's island");
            player.sendMessage("§d/ob reload §7- reload config");
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
            return completions.stream()
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ROOT)))
                .toList();
        }

        if (args.length == 2 && List.of("tp", "info", "reset").contains(args[0].toLowerCase(Locale.ROOT))) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                .toList();
        }

        return completions;
    }
}
