package org.customTagSystem.managers;

import org.bukkit.ChatColor;
import org.customTagSystem.CustomTagSystem;

public class ConfigManager {

    private final CustomTagSystem plugin;

    public ConfigManager(CustomTagSystem plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    public String getMessage(String path) {
        String message = plugin.getConfig().getString("messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getGuiTitle(String path) {
        String title = plugin.getConfig().getString("gui-titles." + path, "");
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public String getTablistPrefix() {
        String prefix = plugin.getConfig().getString("tablist.prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }

    public String getTablistSuffix() {
        String suffix = plugin.getConfig().getString("tablist.suffix", " {tag}");
        return ChatColor.translateAlternateColorCodes('&', suffix);
    }
}