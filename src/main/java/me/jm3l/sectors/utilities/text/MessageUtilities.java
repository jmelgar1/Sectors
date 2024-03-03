package me.jm3l.sectors.utilities.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtilities {
    public static TextComponent createTextComponent(String text, TextColor color) {
        return Component.text(text).color(color);
    }

    public static TextComponent appendTextComponents(TextComponent original, String text, TextColor color) {
        return original.append(createTextComponent(text, color));
    }

    public static TextComponent createCommandComponent(String prefix, String command, String suffix, TextColor prefixColor, TextColor commandColor, TextColor suffixColor) {
        return Component.text(prefix).color(prefixColor)
            .append(Component.text(command).color(commandColor))
            .append(Component.text(suffix).color(suffixColor));
    }
}
