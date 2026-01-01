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
import org.customTagSystem.models.Tag;
import org.customTagSystem.models.TagCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryGUI implements Listener {

    private final CustomTagSystem plugin;
    private final Player player;
    private final TagCategory category;
    private final Inventory inventory;

    public CategoryGUI(CustomTagSystem plugin, Player player, TagCategory category) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
        this.inventory = Bukkit.createInventory(null, 54, "§8Tags » " + category.getDisplayName());

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        int slot = 10;
        for (Tag tag : category.getTags()) {
            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;

            boolean hasTag = plugin.getTagManager().hasTag(player, tag.getId());
            boolean isActive = tag.getId().equals(plugin.getTagManager().getActiveTag(player));

            Material material;
            List<String> lore = new ArrayList<>();

            if (hasTag) {
                material = isActive ? Material.LIME_DYE : Material.PAPER;
                lore.add("§7Vista previa: §r" + tag.getDisplay());
                lore.add("");
                if (isActive) {
                    lore.add("§a✔ Tag Activo");
                } else {
                    lore.add("§7Estado: §aDesbloqueado");
                    lore.add("");
                    lore.add("§eClick para activar");
                }
            } else {
                material = Material.GRAY_DYE;
                lore.add("§7Vista previa: §r" + tag.getDisplay());
                lore.add("");
                lore.add("§7Estado: §cBloqueado");
                lore.add("§7Precio: §6" + tag.getPrice() + " monedas");
                lore.add("");

                if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
                    lore.add("§c✖ Sin permiso necesario");
                    lore.add("§7Permiso: §c" + tag.getPermission());
                } else if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) >= tag.getPrice()) {
                    lore.add("§aClick para comprar");
                } else {
                    lore.add("§c✖ Dinero insuficiente");
                }
            }

            ItemStack item = createItem(material, "§e" + tag.getName(), lore);
            inventory.setItem(slot, item);
            slot++;
        }

        // Botón de volver
        ItemStack backItem = createItem(Material.ARROW, "§c« Volver", "§7Volver al menú principal");
        inventory.setItem(49, backItem);

        // Decoración
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        int[] decorSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 18, 27, 36, 17, 26, 35, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int decorSlot : decorSlots) {
            inventory.setItem(decorSlot, glass);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        return createItem(material, name, loreList);
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

        // Botón volver
        if (e.getSlot() == 49) {
            new MainMenuGUI(plugin, player).open();
            return;
        }

        // Buscar tag clickeado
        int slot = e.getSlot();
        int index = 0;
        int checkSlot = 10;

        for (Tag tag : category.getTags()) {
            if (checkSlot == 17) checkSlot = 19;
            if (checkSlot == 26) checkSlot = 28;
            if (checkSlot == 35) checkSlot = 37;

            if (checkSlot == slot) {
                handleTagClick(tag);
                return;
            }

            checkSlot++;
            if (checkSlot >= 44) break;
        }
    }

    private void handleTagClick(Tag tag) {
        boolean hasTag = plugin.getTagManager().hasTag(player, tag.getId());
        boolean isActive = tag.getId().equals(plugin.getTagManager().getActiveTag(player));

        if (hasTag) {
            if (isActive) {
                player.sendMessage(plugin.getConfigManager().getMessage("tag-already-active"));
            } else {
                plugin.getTagManager().setActiveTag(player, tag.getId());
                player.sendMessage(plugin.getConfigManager().getMessage("tag-activated")
                        .replace("{tag}", tag.getDisplay()));
                player.closeInventory();
            }
        } else {
            // Verificar permiso
            if (!tag.getPermission().isEmpty() && !player.hasPermission(tag.getPermission())) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission-tag"));
                return;
            }

            // Verificar economía
            if (plugin.getEconomy() == null) {
                player.sendMessage("§cSistema de economía no disponible.");
                return;
            }

            if (plugin.getEconomy().getBalance(player) < tag.getPrice()) {
                player.sendMessage(plugin.getConfigManager().getMessage("insufficient-money")
                        .replace("{price}", String.valueOf(tag.getPrice())));
                return;
            }

            // Comprar tag
            plugin.getEconomy().withdrawPlayer(player, tag.getPrice());
            plugin.getTagManager().unlockTag(player, tag.getId());
            player.sendMessage(plugin.getConfigManager().getMessage("tag-purchased")
                    .replace("{tag}", tag.getDisplay())
                    .replace("{price}", String.valueOf(tag.getPrice())));

            // Actualizar GUI
            player.closeInventory();
            new CategoryGUI(plugin, player, category).open();
        }
    }
}