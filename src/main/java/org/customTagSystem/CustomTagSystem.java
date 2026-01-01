package org.customTagSystem;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.customTagSystem.commands.TagCommand;
import org.customTagSystem.database.DatabaseManager;
import org.customTagSystem.listeners.PlayerJoinListener;
import org.customTagSystem.managers.ConfigManager;
import org.customTagSystem.managers.TagManager;
import org.customTagSystem.placeholders.TagPlaceholder;
import net.milkbowl.vault.economy.Economy;

public final class CustomTagSystem extends JavaPlugin {

    private static CustomTagSystem instance;
    private DatabaseManager databaseManager;
    private TagManager tagManager;
    private ConfigManager configManager;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Guardar configuración por defecto
        saveDefaultConfig();

        // Inicializar ConfigManager
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Inicializar base de datos
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("¡Error al inicializar la base de datos! Deshabilitando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Inicializar TagManager
        tagManager = new TagManager(this);
        tagManager.loadTags();

        // Configurar Vault Economy
        if (!setupEconomy()) {
            getLogger().warning("¡Vault no encontrado! Sistema de economía deshabilitado.");
        } else {
            getLogger().info("Vault conectado exitosamente.");
        }

        // Registrar comandos
        getCommand("tags").setExecutor(new TagCommand(this));

        // Registrar listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Registrar PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new TagPlaceholder(this).register();
            getLogger().info("PlaceholderAPI conectado exitosamente.");
        } else {
            getLogger().warning("PlaceholderAPI no encontrado. Los placeholders no estarán disponibles.");
        }

        getLogger().info("CustomTagSystem habilitado exitosamente!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("CustomTagSystem deshabilitado.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static CustomTagSystem getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public TagManager getTagManager() {
        return tagManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public void reload() {
        reloadConfig();
        configManager.loadConfig();
        tagManager.loadTags();
        tagManager.reloadAllPlayerTags();
        getLogger().info("Plugin recargado exitosamente!");
    }
}