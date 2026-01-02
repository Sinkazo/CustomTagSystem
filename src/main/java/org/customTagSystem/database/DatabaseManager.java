package org.customTagSystem.database;

import org.customTagSystem.CustomTagSystem;
import org.customTagSystem.models.TagStyle;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final CustomTagSystem plugin;
    private Connection connection;

    public DatabaseManager(CustomTagSystem plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder + "/tags.db");

            createTables();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_tags (" +
                            "uuid TEXT NOT NULL," +
                            "tag_id TEXT NOT NULL," +
                            "PRIMARY KEY (uuid, tag_id)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS active_tags (" +
                            "uuid TEXT PRIMARY KEY," +
                            "tag_id TEXT" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_customizations (" +
                            "uuid TEXT NOT NULL," +
                            "customization_id TEXT NOT NULL," +
                            "PRIMARY KEY (uuid, customization_id)" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_tag_styles (" +
                            "uuid TEXT PRIMARY KEY," +
                            "color TEXT DEFAULT 'default'," +
                            "text_style TEXT DEFAULT 'normal'," +
                            "remove_brackets INTEGER DEFAULT 0" +
                            ")"
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al crear tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hasTag(UUID uuid, String tagId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM player_tags WHERE uuid = ? AND tag_id = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, tagId);

            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al verificar tag: " + e.getMessage());
            return false;
        }
    }

    public void unlockTag(UUID uuid, String tagId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_tags (uuid, tag_id) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, tagId);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al desbloquear tag: " + e.getMessage());
        }
    }

    public List<String> getUnlockedTags(UUID uuid) {
        List<String> tags = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT tag_id FROM player_tags WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                tags.add(result.getString("tag_id"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener tags: " + e.getMessage());
        }
        return tags;
    }

    public String getActiveTag(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT tag_id FROM active_tags WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("tag_id");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener tag activo: " + e.getMessage());
        }
        return null;
    }

    public void setActiveTag(UUID uuid, String tagId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO active_tags (uuid, tag_id) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, tagId);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al establecer tag activo: " + e.getMessage());
        }
    }

    // Métodos de personalización
    public boolean hasCustomization(UUID uuid, String customizationId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM player_customizations WHERE uuid = ? AND customization_id = ?")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, customizationId);

            ResultSet result = statement.executeQuery();
            return result.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al verificar personalización: " + e.getMessage());
            return false;
        }
    }

    public void unlockCustomization(UUID uuid, String customizationId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_customizations (uuid, customization_id) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, customizationId);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al desbloquear personalización: " + e.getMessage());
        }
    }

    public TagStyle getPlayerTagStyle(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT color, text_style, remove_brackets FROM player_tag_styles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String color = result.getString("color");
                String textStyle = result.getString("text_style");
                boolean removeBrackets = result.getInt("remove_brackets") == 1;
                return new TagStyle(color, textStyle, removeBrackets);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al obtener estilo de tag: " + e.getMessage());
        }
        return new TagStyle();
    }

    public void savePlayerTagStyle(UUID uuid, TagStyle style) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT OR REPLACE INTO player_tag_styles (uuid, color, text_style, remove_brackets) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, style.getColor());
            statement.setString(3, style.getTextStyle());
            statement.setInt(4, style.isRemoveBrackets() ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al guardar estilo de tag: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al cerrar la base de datos: " + e.getMessage());
        }
    }
}