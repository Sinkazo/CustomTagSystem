package org.customTagSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.customTagSystem.CustomTagSystem;
import org.customTagSystem.models.Tag;
import org.customTagSystem.models.TagCategory;
import org.customTagSystem.models.TagStyle;

import java.util.*;
import java.util.stream.Collectors;

public class TagManager {

    private final CustomTagSystem plugin;
    private final Map<String, Tag> tags;
    private final Map<String, TagCategory> categories;
    private final Map<UUID, String> activePlayerTags;
    private final Map<UUID, TagStyle> playerTagStyles;

    public TagManager(CustomTagSystem plugin) {
        this.plugin = plugin;
        this.tags = new HashMap<>();
        this.categories = new HashMap<>();
        this.activePlayerTags = new HashMap<>();
        this.playerTagStyles = new HashMap<>();
    }

    public void loadTags() {
        tags.clear();
        categories.clear();

        ConfigurationSection categoriesSection = plugin.getConfig().getConfigurationSection("categories");
        if (categoriesSection == null) return;

        for (String categoryKey : categoriesSection.getKeys(false)) {
            ConfigurationSection catSection = categoriesSection.getConfigurationSection(categoryKey);
            if (catSection == null) continue;

            String displayName = catSection.getString("display-name", categoryKey);
            String icon = catSection.getString("icon", "PAPER");
            int slot = catSection.getInt("slot", 0);

            TagCategory category = new TagCategory(categoryKey, displayName, icon, slot);
            categories.put(categoryKey, category);

            ConfigurationSection tagsSection = catSection.getConfigurationSection("tags");
            if (tagsSection == null) continue;

            for (String tagKey : tagsSection.getKeys(false)) {
                ConfigurationSection tagSection = tagsSection.getConfigurationSection(tagKey);
                if (tagSection == null) continue;

                String id = categoryKey + "." + tagKey;
                String display = tagSection.getString("display");
                double price = tagSection.getDouble("price", 0);
                String permission = tagSection.getString("permission", "");

                Tag tag = new Tag(id, tagKey, display, price, categoryKey, permission);
                tags.put(id, tag);
                category.addTag(tag);
            }
        }
    }

    public void setActiveTag(Player player, String tagId) {
        if (tagId == null || tagId.isEmpty()) {
            activePlayerTags.remove(player.getUniqueId());
            plugin.getDatabaseManager().setActiveTag(player.getUniqueId(), null);
        } else {
            activePlayerTags.put(player.getUniqueId(), tagId);
            plugin.getDatabaseManager().setActiveTag(player.getUniqueId(), tagId);
        }
        updatePlayerTablist(player);
    }

    public String getActiveTag(Player player) {
        return activePlayerTags.get(player.getUniqueId());
    }

    public String getActiveTagDisplay(Player player) {
        String tagId = getActiveTag(player);
        if (tagId == null) return "";

        Tag tag = tags.get(tagId);
        if (tag == null) return "";

        String display = org.bukkit.ChatColor.translateAlternateColorCodes('&', tag.getDisplay());

        // Aplicar personalización
        TagStyle style = getPlayerTagStyle(player);
        return applyTagStyle(display, style);
    }

    private String applyTagStyle(String tagDisplay, TagStyle style) {
        String result = tagDisplay;

        // Primero aplicar estilo de texto (esto puede modificar el contenido)
        result = style.applyStyle(result);

        // Luego aplicar color (esto envuelve todo con códigos de color)
        if (!style.getColor().equals("default") && !style.getColor().equals("normal")) {
            result = applyColor(result, style.getColor());
        }

        return result;
    }

    private String applyColor(String text, String color) {
        if (color.equals("rainbow")) {
            return applyRainbow(text);
        } else {
            String colorCode = getColorCode(color);
            // Limpiar códigos de color existentes pero mantener formato (bold, etc)
            String cleanText = text.replaceAll("§[0-9a-f]", "");
            return colorCode + cleanText;
        }
    }

    private String applyRainbow(String text) {
        String[] colors = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};
        StringBuilder result = new StringBuilder();
        int colorIndex = 0;

        // Detectar si hay formato bold al inicio
        boolean hasBold = text.startsWith("§l");
        String boldCode = hasBold ? "§l" : "";

        // Limpiar todos los códigos de color/formato para procesar solo el texto
        String cleanText = text.replaceAll("§[0-9a-fk-or]", "");

        for (char c : cleanText.toCharArray()) {
            if (c != ' ' && c != '[' && c != ']') {
                result.append(colors[colorIndex % colors.length]);
                if (hasBold) {
                    result.append("§l");
                }
                colorIndex++;
            }
            result.append(c);
        }

        return result.toString();
    }

    private String getColorCode(String color) {
        switch (color.toLowerCase()) {
            case "red": return "§c";
            case "gold": return "§6";
            case "yellow": return "§e";
            case "green": return "§a";
            case "aqua": return "§b";
            case "blue": return "§9";
            case "light_purple": return "§d";
            case "dark_red": return "§4";
            case "dark_green": return "§2";
            case "dark_aqua": return "§3";
            case "dark_blue": return "§1";
            case "dark_purple": return "§5";
            case "white": return "§f";
            case "gray": return "§7";
            case "dark_gray": return "§8";
            case "black": return "§0";
            default: return "";
        }
    }

    public void loadPlayerData(Player player) {
        String activeTag = plugin.getDatabaseManager().getActiveTag(player.getUniqueId());
        if (activeTag != null && !activeTag.isEmpty()) {
            activePlayerTags.put(player.getUniqueId(), activeTag);
        }

        // Cargar estilo personalizado
        TagStyle style = plugin.getDatabaseManager().getPlayerTagStyle(player.getUniqueId());
        playerTagStyles.put(player.getUniqueId(), style);

        updatePlayerTablist(player);
    }

    public boolean hasTag(Player player, String tagId) {
        return plugin.getDatabaseManager().hasTag(player.getUniqueId(), tagId);
    }

    public void unlockTag(Player player, String tagId) {
        plugin.getDatabaseManager().unlockTag(player.getUniqueId(), tagId);
    }

    public List<String> getUnlockedTags(Player player) {
        return plugin.getDatabaseManager().getUnlockedTags(player.getUniqueId());
    }

    public Tag getTag(String tagId) {
        return tags.get(tagId);
    }

    public Collection<Tag> getAllTags() {
        return tags.values();
    }

    public Collection<TagCategory> getCategories() {
        return categories.values();
    }

    public TagCategory getCategory(String categoryId) {
        return categories.get(categoryId);
    }

    public int getUnlockedTagsCount(Player player) {
        return getUnlockedTags(player).size();
    }

    public Map<String, Integer> getUnlockedTagsByCategory(Player player) {
        Map<String, Integer> result = new HashMap<>();
        List<String> unlockedTags = getUnlockedTags(player);

        for (TagCategory category : categories.values()) {
            int count = 0;
            for (Tag tag : category.getTags()) {
                if (unlockedTags.contains(tag.getId())) {
                    count++;
                }
            }
            result.put(category.getId(), count);
        }

        return result;
    }

    public void updatePlayerTablist(Player player) {
        String tagDisplay = getActiveTagDisplay(player);
        String prefix = plugin.getConfigManager().getTablistPrefix();
        String suffix = plugin.getConfigManager().getTablistSuffix();

        String finalSuffix = suffix.replace("{tag}", tagDisplay);

        player.setPlayerListName(prefix + player.getName() + finalSuffix);
    }

    public void reloadAllPlayerTags() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    // Métodos de personalización
    public TagStyle getPlayerTagStyle(Player player) {
        return playerTagStyles.getOrDefault(player.getUniqueId(), new TagStyle());
    }

    public boolean hasCustomization(Player player, String customizationId) {
        return plugin.getDatabaseManager().hasCustomization(player.getUniqueId(), customizationId);
    }

    public void unlockCustomization(Player player, String customizationId) {
        plugin.getDatabaseManager().unlockCustomization(player.getUniqueId(), customizationId);
    }

    public void setPlayerTagColor(Player player, String color) {
        TagStyle style = getPlayerTagStyle(player);
        style.setColor(color);
        plugin.getDatabaseManager().savePlayerTagStyle(player.getUniqueId(), style);
        playerTagStyles.put(player.getUniqueId(), style);
        updatePlayerTablist(player);
    }

    public void setPlayerTagTextStyle(Player player, String textStyle) {
        TagStyle style = getPlayerTagStyle(player);
        style.setTextStyle(textStyle);
        plugin.getDatabaseManager().savePlayerTagStyle(player.getUniqueId(), style);
        playerTagStyles.put(player.getUniqueId(), style);
        updatePlayerTablist(player);
    }

    public void setPlayerTagRemoveBrackets(Player player, boolean removeBrackets) {
        TagStyle style = getPlayerTagStyle(player);
        style.setRemoveBrackets(removeBrackets);
        plugin.getDatabaseManager().savePlayerTagStyle(player.getUniqueId(), style);
        playerTagStyles.put(player.getUniqueId(), style);
        updatePlayerTablist(player);
    }
}