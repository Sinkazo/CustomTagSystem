package org.customTagSystem.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.customTagSystem.CustomTagSystem;
import org.customTagSystem.models.Tag;
import org.customTagSystem.models.TagCategory;

import java.util.*;
import java.util.stream.Collectors;

public class TagManager {

    private final CustomTagSystem plugin;
    private final Map<String, Tag> tags;
    private final Map<String, TagCategory> categories;
    private final Map<UUID, String> activePlayerTags;

    public TagManager(CustomTagSystem plugin) {
        this.plugin = plugin;
        this.tags = new HashMap<>();
        this.categories = new HashMap<>();
        this.activePlayerTags = new HashMap<>();
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
        return tag != null ? org.bukkit.ChatColor.translateAlternateColorCodes('&', tag.getDisplay()) : "";
    }

    public void loadPlayerData(Player player) {
        String activeTag = plugin.getDatabaseManager().getActiveTag(player.getUniqueId());
        if (activeTag != null && !activeTag.isEmpty()) {
            activePlayerTags.put(player.getUniqueId(), activeTag);
        }
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
}