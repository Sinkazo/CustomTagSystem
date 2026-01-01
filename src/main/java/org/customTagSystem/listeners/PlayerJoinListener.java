package org.customTagSystem.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.customTagSystem.CustomTagSystem;

public class PlayerJoinListener implements Listener {

    private final CustomTagSystem plugin;

    public PlayerJoinListener(CustomTagSystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getTagManager().loadPlayerData(event.getPlayer());
    }
}