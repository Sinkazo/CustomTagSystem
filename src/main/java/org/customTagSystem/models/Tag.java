package org.customTagSystem.models;

public class Tag {

    private final String id;
    private final String name;
    private final String display;
    private final double price;
    private final String category;
    private final String permission;

    public Tag(String id, String name, String display, double price, String category, String permission) {
        this.id = id;
        this.name = name;
        this.display = display;
        this.price = price;
        this.category = category;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getPermission() {
        return permission;
    }
}