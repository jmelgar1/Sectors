package me.jm3l.sectors.utilities.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtilities {
    public static TextComponent createAlertIcon(TextColor color) {
        return Component.text("[💡] ").color(color);
    }

    public static TextComponent createXIcon(TextColor color) {
        return Component.text("[✘] ").color(color);
    }

    public static TextComponent createSuccessIcon(TextColor color) {
        return Component.text("[✔] ").color(color);
    }

    public static TextComponent createTeleportIcon(TextColor color) {
        return Component.text("[⤼] ").color(color);
    }

    public static TextComponent createSectorIcon(TextColor color) {
        return Component.text("[🎲] ").color(color);
    }

    public static TextComponent createCrownIcon(TextColor color) {
        return Component.text("[👑] ").color(color);
    }

    public static TextComponent createKickIcon(TextColor color) {
        return Component.text("[👢] ").color(color);
    }

    public static TextComponent createLeaveIcon(TextColor color){
        return Component.text("[⬅] ").color(color);
    }

    public static TextComponent createJoinIcon(TextColor color){
        return Component.text("[➡] ").color(color);
    }

    public static TextComponent createSpongeIcon(TextColor color) {
        return Component.text("⧠").color(color);
    }
}
