package org.customTagSystem.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.customTagSystem.CustomTagSystem;
import org.customTagSystem.models.TagCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainMenuGUI implements Listener {

    private final CustomTagSystem plugin;
    private final Player player;
    private final Inventory inventory;

    public MainMenuGUI(CustomTagSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, plugin.getConfigManager().getGuiTitle("main-menu"));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // Info del jugador
        ItemStack infoItem = createItem(Material.PLAYER_HEAD,
                "§6§l⭐ Tu Información",
                "§7Tags desbloqueados: §e" + plugin.getTagManager().getUnlockedTagsCount(player),
                "§7Tag activo: §r" + (plugin.getTagManager().getActiveTagDisplay(player).isEmpty() ? "§cNinguno" : plugin.getTagManager().getActiveTagDisplay(player)),
                "",
                "§7Explora las categorías para",
                "§7desbloquear más tags!"
        );
        inventory.setItem(4, infoItem);

        // Categorías
        for (TagCategory category : plugin.getTagManager().getCategories()) {
            int slot = category.getSlot();
            Material material = getMaterial(category.getIcon());

            Map<String, Integer> unlockedByCategory = plugin.getTagManager().getUnlockedTagsByCategory(player);
            int unlocked = unlockedByCategory.getOrDefault(category.getId(), 0);
            int total = category.getTags().size();

            ItemStack item = createItem(material,
                    "§e§l" + category.getDisplayName(),
                    "§7Tags desbloqueados: §a" + unlocked + "§7/§e" + total,
                    "",
                    "§eClick para ver tags"
            );

            inventory.setItem(slot, item);
        }

        // Remover tag activo
        ItemStack removeItem = createItem(Material.BARRIER,
                "§c§lQuitar Tag Activo",
                "§7Remueve tu tag actual",
                "",
                "§cClick para quitar"
        );
        inventory.setItem(49, removeItem);

        // Decoración
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        int[] decorSlots = {0, 1, 7, 8, 9, 17, 36, 44, 45, 46, 52, 53};
        for (int slot : decorSlots) {
            inventory.setItem(slot, glass);
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getMaterial(String materialName) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.PAPER;
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getInventory().equals(inventory)) return;
        if (!(e.getWhoClicked() instanceof Player)) return;

        e.setCancelled(true);

        Player clicker = (Player) e.getWhoClicked();
        if (!clicker.equals(player)) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Remover tag
        if (e.getSlot() == 49) {
            plugin.getTagManager().setActiveTag(player, null);
            player.sendMessage(plugin.getConfigManager().getMessage("tag-removed"));
            player.closeInventory();
            return;
        }

        // Abrir categoría
        for (TagCategory category : plugin.getTagManager().getCategories()) {
            if (e.getSlot() == category.getSlot()) {
                new CategoryGUI(plugin, player, category).open();
                return;
            }
        }
    }
}