package org.customTagSystem.models;

public class TagStyle {

    private String color;
    private String textStyle;
    private boolean removeBrackets;

    public TagStyle() {
        this.color = "default";
        this.textStyle = "normal";
        this.removeBrackets = false;
    }

    public TagStyle(String color, String textStyle, boolean removeBrackets) {
        this.color = color;
        this.textStyle = textStyle;
        this.removeBrackets = removeBrackets;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(String textStyle) {
        this.textStyle = textStyle;
    }

    public boolean isRemoveBrackets() {
        return removeBrackets;
    }

    public void setRemoveBrackets(boolean removeBrackets) {
        this.removeBrackets = removeBrackets;
    }

    public String applyStyle(String tagText) {
        String result = tagText;

        // Remover brackets si está activado (primero)
        if (removeBrackets) {
            result = result.replace("[", "").replace("]", "");
        }

        // Aplicar estilo de texto
        switch (textStyle.toLowerCase()) {
            case "uppercase":
                result = result.toUpperCase();
                break;
            case "lowercase":
                result = result.toLowerCase();
                break;
            case "smallcaps":
                result = convertToSmallCaps(result);
                break;
            case "bold":
                // Solo añadir el código bold, no duplicarlo si ya existe
                if (!result.startsWith("§l")) {
                    result = "§l" + result;
                }
                break;
        }

        return result;
    }

    private String convertToSmallCaps(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            result.append(getSmallCapChar(c));
        }
        return result.toString();
    }

    private String getSmallCapChar(char c) {
        // Convertir a minúscula primero para el switch
        char lower = Character.toLowerCase(c);
        switch (lower) {
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
}