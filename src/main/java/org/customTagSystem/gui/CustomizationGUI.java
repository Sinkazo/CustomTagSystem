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
import org.customTagSystem.models.TagStyle;

import java.util.ArrayList;
import java.util.List;

public class CustomizationGUI implements Listener {

    private final CustomTagSystem plugin;
    private final Player player;
    private final Inventory inventory;

    public CustomizationGUI(CustomTagSystem plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§l⚙ Personalización de Tags");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setupInventory();
    }

    private void setupInventory() {
        TagStyle currentStyle = plugin.getTagManager().getPlayerTagStyle(player);

        // Título informativo
        ItemStack infoItem = createItem(Material.NETHER_STAR,
                "§6§l⭐ Personaliza tu Tag",
                "§7Cambia el color, estilo y formato",
                "§7de tu tag activo.",
                "",
                "§eCompra las opciones que desees!"
        );
        inventory.setItem(4, infoItem);

        // SECCIÓN DE COLORES
        addColorOptions(currentStyle);

        // SECCIÓN DE ESTILOS DE TEXTO
        addTextStyleOptions(currentStyle);

        // SECCIÓN DE FORMATO
        addFormatOptions(currentStyle);

        // Botón de volver
        ItemStack backItem = createItem(Material.ARROW, "§c« Volver", "§7Volver al menú principal");
        inventory.setItem(49, backItem);

        // Decoración
        addDecoration();
    }

    private void addColorOptions(TagStyle currentStyle) {
        String[] colors = {"red", "gold", "yellow", "green", "aqua", "blue", "light_purple", "dark_red",
                "dark_green", "dark_aqua", "dark_blue", "dark_purple", "white", "gray", "dark_gray", "black"};
        String[] colorCodes = {"§c", "§6", "§e", "§a", "§b", "§9", "§d", "§4",
                "§2", "§3", "§1", "§5", "§f", "§7", "§8", "§0"};
        String[] colorNames = {"Rojo", "Oro", "Amarillo", "Verde", "Aqua", "Azul", "Púrpura Claro", "Rojo Oscuro",
                "Verde Oscuro", "Aqua Oscuro", "Azul Oscuro", "Púrpura Oscuro", "Blanco", "Gris", "Gris Oscuro", "Negro"};

        int[] colorSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};

        // Obtener tag activo del jugador para vista previa
        String activeTagRaw = plugin.getTagManager().getActiveTag(player);
        String previewTag = "[TAG]";
        if (activeTagRaw != null) {
            Tag activeTag = plugin.getTagManager().getTag(activeTagRaw);
            if (activeTag != null) {
                String fullTag = activeTag.getDisplay();
                // Traducir códigos & a §
                fullTag = org.bukkit.ChatColor.translateAlternateColorCodes('&', fullTag);
                // Remover códigos de color para mostrar solo la estructura
                previewTag = fullTag.replaceAll("§[0-9a-fk-or]", "");
            }
        }

        for (int i = 0; i < colors.length && i < colorSlots.length; i++) {
            boolean owned = plugin.getTagManager().hasCustomization(player, "color_" + colors[i]);
            boolean active = currentStyle.getColor().equals(colors[i]);
            double price = plugin.getConfigManager().getCustomizationPrice("colors." + colors[i]);

            Material material = active ? Material.LIME_DYE : (owned ? Material.PAPER : Material.GRAY_DYE);

            List<String> lore = new ArrayList<>();
            lore.add("§7Vista previa: " + colorCodes[i] + previewTag);
            lore.add("");

            if (active) {
                lore.add("§a✔ Color Activo");
            } else if (owned) {
                lore.add("§7Estado: §aDesbloqueado");
                lore.add("");
                lore.add("§eClick para activar");
            } else {
                lore.add("§7Estado: §cBloqueado");
                lore.add("§7Precio: §6" + price);
                lore.add("§7monedas");
                lore.add("");
                if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) >= price) {
                    lore.add("§aClick para comprar");
                } else {
                    lore.add("§c✖ Dinero insuficiente");
                }
            }

            ItemStack item = createItem(material, colorCodes[i] + "Color " + colorNames[i], lore);
            inventory.setItem(colorSlots[i], item);
        }

        // Rainbow especial
        boolean rainbowOwned = plugin.getTagManager().hasCustomization(player, "color_rainbow");
        boolean rainbowActive = currentStyle.getColor().equals("rainbow");
        double rainbowPrice = plugin.getConfigManager().getCustomizationPrice("colors.rainbow");

        Material rainbowMaterial = rainbowActive ? Material.LIME_DYE : (rainbowOwned ? Material.PAPER : Material.GRAY_DYE);

        List<String> rainbowLore = new ArrayList<>();

        // Aplicar efecto rainbow a la vista previa
        String rainbowPreview = applyRainbowPreview(previewTag);
        rainbowLore.add("§7Vista previa: " + rainbowPreview);
        rainbowLore.add("");

        if (rainbowActive) {
            rainbowLore.add("§a✔ Color Activo");
        } else if (rainbowOwned) {
            rainbowLore.add("§7Estado: §aDesbloqueado");
            rainbowLore.add("");
            rainbowLore.add("§eClick para activar");
        } else {
            rainbowLore.add("§7Estado: §cBloqueado");
            rainbowLore.add("§7Precio: §6" + rainbowPrice);
            rainbowLore.add("§7monedas");
            rainbowLore.add("");
            if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) >= rainbowPrice) {
                rainbowLore.add("§aClick para comprar");
            } else {
                rainbowLore.add("§c✖ Dinero insuficiente");
            }
        }

        ItemStack rainbowItem = createItem(rainbowMaterial, "§c§lCo§6§llo§e§llor §a§lRa§b§lli§9§lnbo§d§lw", rainbowLore);
        inventory.setItem(30, rainbowItem);
    }

    private String applyRainbowPreview(String text) {
        String[] colors = {"§c", "§6", "§e", "§a", "§b", "§9", "§d"};
        StringBuilder result = new StringBuilder();
        int colorIndex = 0;

        for (char c : text.toCharArray()) {
            if (c != ' ' && c != '[' && c != ']') {
                result.append(colors[colorIndex % colors.length]);
                colorIndex++;
            }
            result.append(c);
        }

        return result.toString();
    }

    private void addTextStyleOptions(TagStyle currentStyle) {
        String[] styles = {"uppercase", "lowercase", "smallcaps", "bold"};
        String[] styleNames = {"MAYÚSCULAS", "minúsculas", "ꜱᴍᴀʟʟ ᴄᴀᴘꜱ", "§lNegritas"};
        int[] styleSlots = {32, 33, 34, 37};

        // Obtener tag activo del jugador para vista previa
        String activeTagRaw = plugin.getTagManager().getActiveTag(player);
        String previewTag = "[TAG]";
        if (activeTagRaw != null) {
            Tag activeTag = plugin.getTagManager().getTag(activeTagRaw);
            if (activeTag != null) {
                String fullTag = activeTag.getDisplay();
                // Traducir códigos & a §
                fullTag = org.bukkit.ChatColor.translateAlternateColorCodes('&', fullTag);
                // Remover códigos de color/formato para mostrar solo la estructura
                previewTag = fullTag.replaceAll("§[0-9a-fk-or]", "");
            }
        }

        for (int i = 0; i < styles.length; i++) {
            boolean owned = plugin.getTagManager().hasCustomization(player, "style_" + styles[i]);
            boolean active = currentStyle.getTextStyle().equals(styles[i]);
            double price = plugin.getConfigManager().getCustomizationPrice("text_styles." + styles[i]);

            Material material = active ? Material.LIME_DYE : (owned ? Material.PAPER : Material.GRAY_DYE);

            // Aplicar el estilo a la vista previa
            String styledPreview = previewTag;
            if (styles[i].equals("uppercase")) {
                styledPreview = previewTag.toUpperCase();
            } else if (styles[i].equals("lowercase")) {
                styledPreview = previewTag.toLowerCase();
            } else if (styles[i].equals("smallcaps")) {
                styledPreview = convertToSmallCapsPreview(previewTag);
            } else if (styles[i].equals("bold")) {
                styledPreview = "§l" + previewTag;
            }

            List<String> lore = new ArrayList<>();
            lore.add("§7Vista previa: §e" + styledPreview);
            lore.add("");

            if (active) {
                lore.add("§a✔ Estilo Activo");
            } else if (owned) {
                lore.add("§7Estado: §aDesbloqueado");
                lore.add("");
                lore.add("§eClick para activar");
            } else {
                lore.add("§7Estado: §cBloqueado");
                lore.add("§7Precio: §6" + price);
                lore.add("§7monedas");
                lore.add("");
                if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) >= price) {
                    lore.add("§aClick para comprar");
                } else {
                    lore.add("§c✖ Dinero insuficiente");
                }
            }

            ItemStack item = createItem(material, "§e" + styleNames[i], lore);
            inventory.setItem(styleSlots[i], item);
        }
    }

    private String convertToSmallCapsPreview(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            result.append(getSmallCapChar(c));
        }
        return result.toString();
    }

    private String getSmallCapChar(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a': return "ᴀ";
            case 'b': return "ʙ";
            case 'c': return "ᴄ";
            case 'd': return "ᴅ";
            case 'e': return "ᴇ";
            case 'f': return "ꜰ";
            case 'g': return "ɢ";
            case 'h': return "ʜ";
            case 'i': return "ɪ";
            case 'j': return "ᴊ";
            case 'k': return "ᴋ";
            case 'l': return "ʟ";
            case 'm': return "ᴍ";
            case 'n': return "ɴ";
            case 'o': return "ᴏ";
            case 'p': return "ᴘ";
            case 'q': return "ǫ";
            case 'r': return "ʀ";
            case 's': return "ꜱ";
            case 't': return "ᴛ";
            case 'u': return "ᴜ";
            case 'v': return "ᴠ";
            case 'w': return "ᴡ";
            case 'x': return "x";
            case 'y': return "ʏ";
            case 'z': return "ᴢ";
            default: return String.valueOf(c);
        }
    }

    private void addFormatOptions(TagStyle currentStyle) {
        boolean bracketsOwned = plugin.getTagManager().hasCustomization(player, "format_no_brackets");
        boolean bracketsActive = currentStyle.isRemoveBrackets();
        double bracketsPrice = plugin.getConfigManager().getCustomizationPrice("formats.no_brackets");

        Material bracketsMaterial = bracketsActive ? Material.LIME_DYE : (bracketsOwned ? Material.PAPER : Material.GRAY_DYE);

        // Obtener tag activo del jugador para vista previa
        String activeTagRaw = plugin.getTagManager().getActiveTag(player);
        String previewTag = "TAG";
        if (activeTagRaw != null) {
            Tag activeTag = plugin.getTagManager().getTag(activeTagRaw);
            if (activeTag != null) {
                String fullTag = activeTag.getDisplay();
                // Traducir códigos & a §
                fullTag = org.bukkit.ChatColor.translateAlternateColorCodes('&', fullTag);
                // Remover colores/formato y brackets para mostrar solo el contenido
                previewTag = fullTag.replaceAll("§[0-9a-fk-or]", "").replace("[", "").replace("]", "");
            }
        }

        List<String> bracketsLore = new ArrayList<>();
        bracketsLore.add("§7Vista previa: §e" + previewTag);
        bracketsLore.add("§7Remueve los corchetes [ ]");
        bracketsLore.add("");

        if (bracketsActive) {
            bracketsLore.add("§a✔ Formato Activo");
        } else if (bracketsOwned) {
            bracketsLore.add("§7Estado: §aDesbloqueado");
            bracketsLore.add("");
            bracketsLore.add("§eClick para activar");
        } else {
            bracketsLore.add("§7Estado: §cBloqueado");
            bracketsLore.add("§7Precio: §6" + bracketsPrice);
            bracketsLore.add("§7monedas");
            bracketsLore.add("");
            if (plugin.getEconomy() != null && plugin.getEconomy().getBalance(player) >= bracketsPrice) {
                bracketsLore.add("§aClick para comprar");
            } else {
                bracketsLore.add("§c✖ Dinero insuficiente");
            }
        }

        ItemStack bracketsItem = createItem(bracketsMaterial, "§eSin Corchetes", bracketsLore);
        inventory.setItem(41, bracketsItem);
    }

    private void addDecoration() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        int[] decorSlots = {0, 1, 2, 3, 5, 6, 7, 8, 9, 18, 27, 36, 17, 26, 35, 44, 45, 46, 47, 48, 50, 51, 52, 53,
                31, 38, 39, 40, 42, 43};
        for (int slot : decorSlots) {
            inventory.setItem(slot, glass);
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

        int slot = e.getSlot();

        // Botón volver
        if (slot == 49) {
            new MainMenuGUI(plugin, player).open();
            return;
        }

        // Detectar qué se clickeó
        handleCustomizationClick(slot);
    }

    private void handleCustomizationClick(int slot) {
        String[] colors = {"red", "gold", "yellow", "green", "aqua", "blue", "light_purple", "dark_red",
                "dark_green", "dark_aqua", "dark_blue", "dark_purple", "white", "gray", "dark_gray", "black"};
        int[] colorSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29};

        // Verificar colores
        for (int i = 0; i < colorSlots.length; i++) {
            if (slot == colorSlots[i] && i < colors.length) {
                handlePurchase("color_" + colors[i], "colors." + colors[i], colors[i], "color");
                return;
            }
        }

        // Rainbow
        if (slot == 30) {
            handlePurchase("color_rainbow", "colors.rainbow", "rainbow", "color");
            return;
        }

        // Estilos de texto
        String[] styles = {"uppercase", "lowercase", "smallcaps", "bold"};
        int[] styleSlots = {32, 33, 34, 37};

        for (int i = 0; i < styleSlots.length; i++) {
            if (slot == styleSlots[i] && i < styles.length) {
                handlePurchase("style_" + styles[i], "text_styles." + styles[i], styles[i], "style");
                return;
            }
        }

        // Sin corchetes
        if (slot == 41) {
            handlePurchase("format_no_brackets", "formats.no_brackets", "true", "brackets");
            return;
        }
    }

    private void handlePurchase(String customizationId, String priceKey, String value, String type) {
        boolean owned = plugin.getTagManager().hasCustomization(player, customizationId);
        double price = plugin.getConfigManager().getCustomizationPrice(priceKey);

        if (owned) {
            // Activar
            if (type.equals("color")) {
                plugin.getTagManager().setPlayerTagColor(player, value);
                player.sendMessage("§a¡Color de tag actualizado!");
            } else if (type.equals("style")) {
                plugin.getTagManager().setPlayerTagTextStyle(player, value);
                player.sendMessage("§a¡Estilo de texto actualizado!");
            } else if (type.equals("brackets")) {
                boolean current = plugin.getTagManager().getPlayerTagStyle(player).isRemoveBrackets();
                plugin.getTagManager().setPlayerTagRemoveBrackets(player, !current);
                player.sendMessage(current ? "§a¡Corchetes restaurados!" : "§a¡Corchetes removidos!");
            }
            // Actualizar GUI de forma fluida
            refreshInventory();
        } else {
            // Comprar
            if (plugin.getEconomy() == null) {
                player.sendMessage("§cSistema de economía no disponible.");
                return;
            }

            if (plugin.getEconomy().getBalance(player) < price) {
                player.sendMessage("§cNo tienes suficiente dinero. Precio: §6" + price);
                return;
            }

            plugin.getEconomy().withdrawPlayer(player, price);
            plugin.getTagManager().unlockCustomization(player, customizationId);
            player.sendMessage("§a¡Personalización comprada por §6$" + price + "§a!");

            // Actualizar GUI de forma fluida
            refreshInventory();
        }
    }

    private void refreshInventory() {
        // Limpiar solo las áreas necesarias, no todo el inventario
        inventory.clear();
        setupInventory();
    }
}