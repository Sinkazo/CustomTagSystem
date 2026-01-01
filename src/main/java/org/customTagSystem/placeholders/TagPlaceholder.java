package org.customTagSystem.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.customTagSystem.CustomTagSystem;
import org.jetbrains.annotations.NotNull;

public class TagPlaceholder extends PlaceholderExpansion {

    private final CustomTagSystem plugin;

    public TagPlaceholder(CustomTagSystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customtags";
    }

    @Override
    public @NotNull String getAuthor() {
        return "CustomTagSystem";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // %customtags_tag% - Retorna el tag activo
        if (identifier.equals("tag")) {
            return plugin.getTagManager().getActiveTagDisplay(player);
        }

        // %customtags_tag_raw% - Retorna el ID del tag activo
        if (identifier.equals("tag_raw")) {
            String tagId = plugin.getTagManager().getActiveTag(player);
            return tagId != null ? tagId : "";
        }

        // %customtags_unlocked% - Retorna cantidad de tags desbloqueados
        if (identifier.equals("unlocked")) {
            return String.valueOf(plugin.getTagManager().getUnlockedTagsCount(player));
        }

        // %customtags_total% - Retorna cantidad total de tags
        if (identifier.equals("total")) {
            return String.valueOf(plugin.getTagManager().getAllTags().size());
        }

        return null;
    }
}