package org.customTagSystem.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final int page;
    private final int maxPage;
    private final int tagsPerPage;

    public CategoryGUI(CustomTagSystem plugin, Player player, TagCategory category) {
        this(plugin, player, category, 1);
    }

    public CategoryGUI(CustomTagSystem plugin, Player player, TagCategory category, int page) {
        this.plugin = plugin;
        this.player = player;
        this.category = category;
        this.page = page;

        // Obtener configuración de paginación
        this.tagsPerPage = plugin.getConfigManager().getTagsPerPage();

        // Calcular páginas totales
        int totalTags = category.getTags().size();
        this.maxPage = (int) Math.ceil((double) totalTags / tagsPerPage);

        this.inventory = Bukkit.createInventory(null, 54, "§8Tags » " + category.getDisplayName() + " §8(Pág. " + page + "/" + maxPage + ")");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        // Slots para tags (28 espacios disponibles)
        int[] tagSlots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

        // Obtener tags de esta página
        List<Tag> allTags = category.getTags();
        int startIndex = (page - 1) * tagsPerPage;
        int endIndex = Math.min(startIndex + tagsPerPage, allTags.size());

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (slotIndex >= tagSlots.length) break;

            Tag tag = allTags.get(i);
            boolean hasTag = plugin.getTagManager().hasTag(player, tag.getId());
            boolean isActive = tag.getId().equals(plugin.getTagManager().getActiveTag(player));

            Material material;
            List<String> lore = new ArrayList<>();
            String displayName;

            // Traducir el display del tag para mostrarlo correctamente
            String translatedDisplay = org.bukkit.ChatColor.translateAlternateColorCodes('&', tag.getDisplay());

            // Crear previsualización con el nombre del jugador
            String preview = ChatColor.WHITE + player.getName() + " " + translatedDisplay;

            if (hasTag) {
                material = isActive ? Material.LIME_DYE : Material.PAPER;
                displayName = "§e" + tag.getName();
                lore.add("§7Vista previa: §r" + preview);
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
                displayName = "§7" + tag.getName();
                lore.add("§7Vista previa: §r" + preview);
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

            ItemStack item = createItem(material, displayName, lore);
            inventory.setItem(tagSlots[slotIndex], item);
            slotIndex++;
        }

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

        // Botón de volver (después de decoración para que no sea sobrescrito)
        int backSlot = plugin.getConfigManager().getBackButtonSlot();
        Material backMaterial = getMaterial(plugin.getConfigManager().getBackButtonMaterial());
        String backName = plugin.getConfigManager().getBackButtonName();

        ItemStack backItem = createItem(backMaterial, backName, "§7Volver al menú principal");
        inventory.setItem(backSlot, backItem);

        // Botón de página anterior (después de decoración para que no sea sobrescrito)
        if (page > 1) {
            int prevSlot = plugin.getConfigManager().getPreviousPageSlot();
            Material prevMaterial = getMaterial(plugin.getConfigManager().getPreviousPageMaterial());
            String prevName = plugin.getConfigManager().getPreviousPageName();

            ItemStack prevItem = createItem(prevMaterial, prevName,
                    "§7Página actual: §e" + page,
                    "",
                    "§eClick para ir a la página " + (page - 1));
            inventory.setItem(prevSlot, prevItem);
        }

        // Botón de página siguiente (después de decoración para que no sea sobrescrito)
        if (page < maxPage) {
            int nextSlot = plugin.getConfigManager().getNextPageSlot();
            Material nextMaterial = getMaterial(plugin.getConfigManager().getNextPageMaterial());
            String nextName = plugin.getConfigManager().getNextPageName();

            ItemStack nextItem = createItem(nextMaterial, nextName,
                    "§7Página actual: §e" + page,
                    "",
                    "§eClick para ir a la página " + (page + 1));
            inventory.setItem(nextSlot, nextItem);
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

        int slot = e.getSlot();

        // Botón volver
        if (slot == plugin.getConfigManager().getBackButtonSlot()) {
            new MainMenuGUI(plugin, player).open();
            return;
        }

        // Botón página anterior
        if (slot == plugin.getConfigManager().getPreviousPageSlot() && page > 1) {
            new CategoryGUI(plugin, player, category, page - 1).open();
            return;
        }

        // Botón página siguiente
        if (slot == plugin.getConfigManager().getNextPageSlot() && page < maxPage) {
            new CategoryGUI(plugin, player, category, page + 1).open();
            return;
        }

        // Verificar si es un slot de tag
        int[] tagSlots = {10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43};

        int slotIndex = -1;
        for (int i = 0; i < tagSlots.length; i++) {
            if (tagSlots[i] == slot) {
                slotIndex = i;
                break;
            }
        }

        if (slotIndex != -1) {
            int tagIndex = (page - 1) * tagsPerPage + slotIndex;
            if (tagIndex < category.getTags().size()) {
                Tag tag = category.getTags().get(tagIndex);
                handleTagClick(tag);
            }
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
                String translatedTag = org.bukkit.ChatColor.translateAlternateColorCodes('&', tag.getDisplay());
                player.sendMessage(plugin.getConfigManager().getMessage("tag-activated")
                        .replace("{tag}", translatedTag));
                // Actualizar inventario de forma fluida
                refreshInventory();
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
            String translatedTag = org.bukkit.ChatColor.translateAlternateColorCodes('&', tag.getDisplay());
            player.sendMessage(plugin.getConfigManager().getMessage("tag-purchased")
                    .replace("{tag}", translatedTag)
                    .replace("{price}", String.valueOf(tag.getPrice())));

            // Actualizar inventario de forma fluida
            refreshInventory();
        }
    }

    private void refreshInventory() {
        // Limpiar y recargar solo el contenido necesario
        inventory.clear();
        setupInventory();
    }
}