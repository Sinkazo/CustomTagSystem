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

    // Configuración de paginación
    public int getTagsPerPage() {
        return plugin.getConfig().getInt("pagination.tags-per-page", 28);
    }

    public int getNextPageSlot() {
        return plugin.getConfig().getInt("pagination.next-page.slot", 50);
    }

    public String getNextPageMaterial() {
        return plugin.getConfig().getString("pagination.next-page.material", "ARROW");
    }

    public String getNextPageName() {
        String name = plugin.getConfig().getString("pagination.next-page.name", "&a&lPágina Siguiente »");
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    public int getPreviousPageSlot() {
        return plugin.getConfig().getInt("pagination.previous-page.slot", 48);
    }

    public String getPreviousPageMaterial() {
        return plugin.getConfig().getString("pagination.previous-page.material", "ARROW");
    }

    public String getPreviousPageName() {
        String name = plugin.getConfig().getString("pagination.previous-page.name", "&c&l« Página Anterior");
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    public int getBackButtonSlot() {
        return plugin.getConfig().getInt("pagination.back-button.slot", 49);
    }

    public String getBackButtonMaterial() {
        return plugin.getConfig().getString("pagination.back-button.material", "BARRIER");
    }

    public String getBackButtonName() {
        String name = plugin.getConfig().getString("pagination.back-button.name", "&c&l« Volver");
        return ChatColor.translateAlternateColorCodes('&', name);
    }
}