package com.oneblock.listeners;

import com.oneblock.OneBlockPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class QuitListener implements Listener {

    private final OneBlockPlugin plugin;

    public QuitListener(OneBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getLevelBarManager().remove(event.getPlayer());
    }
}
