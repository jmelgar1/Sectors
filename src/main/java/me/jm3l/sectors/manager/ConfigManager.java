package me.jm3l.sectors.manager;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import static org.bukkit.ChatColor.translateAlternateColorCodes;

public class ConfigManager {

    public static void loadConfig(final FileConfiguration config){

        // Claim Tool
        DEFAULT_REACH = config.getInt("default-reach");
        MAX_CLAIM_REACH = config.getInt("max-claim-reach");
        MIN_CLAIM_REACH = config.getInt("min-claim-reach");

        MAX_SECTOR_NAME = config.getInt("max-sector-name");
        MAX_CLAIM_WIDTH = config.getInt("max-claim-width");
        MAX_CLAIM_HEIGHT = config.getInt("max-claim-height");
        MIN_CLAIM_WIDTH = config.getInt("min-claim-width");
        MIN_CLAIM_HEIGHT = config.getInt("min-claim-height");
        MAX_DESCRIPTION_LENGTH = config.getInt("max-description-length");
        MAX_MEMBERS = config.getInt("max-members");
        DTR_REGEN = config.getInt("dtr-regen");
        MINIMUM_DTR = config.getInt("minimum-dtr");
        MAXIMUM_DTR = config.getInt("maximum-dtr");
        PEARL_COOLDOWN = config.getInt("enderpearl-cooldown");
        TELEPORT_DELAY = config.getLong("teleport-delay") * 20;
        ARCHER_TAG_LENGTH = config.getLong("archer-tag-length") * 20;
        ARCHER_TAG_DAMAGE_MULTIPLIER = config.getDouble("archer-tag-multiplier");


        FORMATTED_CHAT = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("formatted-chat")));
        NO_SECTOR_FORMATTED_CHAT = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("no-sector-formatted-chat")));

        NO_PERMISSION = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("no-permission")));
        MISSING_ARGUMENT = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("missing-argument")));
        SECTOR_UNCLAIM = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("sector-unclaim")));
        SECTOR_NO_CLAIM = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("sector-no-claim")));
        SECTOR_NO_COMPOUND = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("sector-no-compound")));
        NOT_IN_SECTOR = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("msg-must-be-in-sector")));
        SUCCESS = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("msg-success")));
        MUST_BE_LEADER = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("msg-must-be-leader")));
        PLAYER_NOT_FOUND = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("msg-player-not-found")));
        NO_INVITE = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("msg-no-invite")));
        ALREADY_HAS_CLAIM = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("already-has-claim")));
        TRIED_CLAIM_NO_SELECTION = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("tried-claim-no-selection")));
        CLAIM_TOO_BIG = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("claim-too-big")));
        CLAIM_TOO_NARROW = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("claim-too-small-or-narrow")));
        LAND_ALREADY_CLAIMED = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("land-already-claimed")));
        MUST_HAVE_CLAIM = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("must-have-claim")));
        COMPOUND_MUST_BE_IN_CLAIM = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("compound-must-be-in-claim")));
        ALREADY_IN_SECTOR = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("already-in-sector")));
        NAME_TOO_LONG = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("name-too-long")));
        NAME_TAKEN = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("name-taken")));
        INVALID_NAME = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("invalid-name")));
        SECTOR_FOUNDED = translateAlternateColorCodes('&', config.getString("sector-founded"));
        NEW_LEADER = translateAlternateColorCodes('&', config.getString("new-leader"));
        YOU_ARE_LEADER = translateAlternateColorCodes('&', config.getString("you-are-leader"));
        SECTOR_DISSOLVED = translateAlternateColorCodes('&', config.getString("sector-dissolved"));
        KICKED_FROM_SECTOR = translateAlternateColorCodes('&', config.getString("kicked-from-sector"));
        LEAVE_SECTOR = translateAlternateColorCodes('&', config.getString("leave-sector"));
        JOIN_SECTOR = translateAlternateColorCodes('&', config.getString("joined-sector"));
        TELEPORT_PENDING = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("teleport-pending").replaceAll("\\{seconds}", Long.toString((TELEPORT_DELAY/20)))));
        NOT_A_PLAYER_OR_SECTOR = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("not-a-player-or-sector")));
        ARCHER_TAGGED = LegacyComponentSerializer.legacyAmpersand().deserialize(translateAlternateColorCodes('&', config.getString("archer-tagged")));

        FORMAT_CHAT = config.getBoolean("format-chat");
        MOB_SPAWN_IN_CLAIMS = config.getBoolean("mob-spawn-in-claims");
        ENABLE_RAIDING = config.getBoolean("enable-raiding");
        SHOW_COORDS_IN_INFO = config.getBoolean("show-coords-in-info");
        SHOW_COLOR_IN_PLACEHOLDER = config.getBoolean("show-color-in-placeholder");
        USE_KITS = config.getBoolean("use-kits");
        USE_TP_DELAY = config.getBoolean("use-tp-delay");

    }

    /*
    Configurable things
     */
    public static int MAX_CLAIM_REACH;
    public static int MIN_CLAIM_REACH;
    public static int DEFAULT_REACH;
    public static int MAXIMUM_DTR;
    public static int MAX_SECTOR_NAME;
    public static int MAX_MEMBERS;
    public static int MAX_CLAIM_HEIGHT;
    public static int MAX_CLAIM_WIDTH;
    public static int MIN_CLAIM_HEIGHT;
    public static int MIN_CLAIM_WIDTH;
    public static int MAX_DESCRIPTION_LENGTH;
    public static int DTR_REGEN;
    public static int MINIMUM_DTR;
    public static int PEARL_COOLDOWN;
    public static long ARCHER_TAG_LENGTH;
    public static long TELEPORT_DELAY;
    public static double ARCHER_TAG_DAMAGE_MULTIPLIER;
    public static boolean FORMAT_CHAT;
    public static boolean USE_KITS;
    public static boolean ENABLE_RAIDING;
    public static boolean SHOW_COORDS_IN_INFO;
    public static boolean SHOW_COLOR_IN_PLACEHOLDER;
    public static boolean USE_TP_DELAY;
    public static boolean MOB_SPAWN_IN_CLAIMS;
    public static TextComponent NO_PERMISSION;
    public static TextComponent MISSING_ARGUMENT;
    public static TextComponent SECTOR_NO_CLAIM;
    public static TextComponent SECTOR_NO_COMPOUND;
    public static TextComponent SECTOR_UNCLAIM;
    public static TextComponent FORMATTED_CHAT;
    public static TextComponent NO_SECTOR_FORMATTED_CHAT;
    public static TextComponent NOT_IN_SECTOR;
    public static TextComponent SUCCESS;
    public static TextComponent MUST_BE_LEADER;
    public static TextComponent PLAYER_NOT_FOUND;
    public static TextComponent NOT_A_PLAYER_OR_SECTOR;
    public static TextComponent NO_INVITE;
    public static TextComponent ALREADY_HAS_CLAIM;
    public static TextComponent TRIED_CLAIM_NO_SELECTION;
    public static TextComponent CLAIM_TOO_BIG;
    public static TextComponent CLAIM_TOO_NARROW;
    public static TextComponent LAND_ALREADY_CLAIMED;
    public static TextComponent MUST_HAVE_CLAIM;
    public static TextComponent COMPOUND_MUST_BE_IN_CLAIM;
    public static TextComponent ALREADY_IN_SECTOR;
    public static TextComponent NAME_TOO_LONG;
    public static TextComponent NAME_TAKEN;
    public static TextComponent INVALID_NAME;
    public static String SECTOR_FOUNDED;
    public static String NEW_LEADER;
    public static String YOU_ARE_LEADER;
    public static String SECTOR_DISSOLVED;
    public static String KICKED_FROM_SECTOR;
    public static String LEAVE_SECTOR;
    public static String JOIN_SECTOR;
    public static TextComponent TELEPORT_PENDING;
    public static TextComponent ARCHER_TAGGED;
}
