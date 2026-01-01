package org.customTagSystem.models;

import java.util.ArrayList;
import java.util.List;

public class TagCategory {

    private final String id;
    private final String displayName;
    private final String icon;
    private final int slot;
    private final List<Tag> tags;

    public TagCategory(String id, String displayName, String icon, int slot) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.slot = slot;
        this.tags = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }
}