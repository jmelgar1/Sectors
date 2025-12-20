package me.jm3l.sectors.shared.util.nms;

import org.bukkit.Bukkit;

public class ServerVersion {

    /**
     * Gets the version of the server.
     * @return A string representing the server version.
     */
    public static String getServerVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    /**
     * Checks if the current server version is greater than a specified version.
     * @param major Major version number
     * @param minor Minor version number
     * @param patch Patch version number
     * @return true if the current version is greater, false otherwise.
     */
    public static boolean isGreaterThan(int major, int minor, int patch) {
        // For modern Paper versions (1.20.5+), the version format changed
        // Use the actual Minecraft version from Bukkit
        String mcVersion = Bukkit.getBukkitVersion(); // e.g., "1.21.11-R0.1-SNAPSHOT"

        try {
            // Parse Minecraft version (e.g., "1.21.11" from "1.21.11-R0.1-SNAPSHOT")
            String[] versionParts = mcVersion.split("-")[0].split("\\.");

            if (versionParts.length >= 2) {
                int currentMajor = Integer.parseInt(versionParts[0]); // Should be 1
                int currentMinor = Integer.parseInt(versionParts[1]); // e.g., 21
                int currentPatch = versionParts.length >= 3 ? Integer.parseInt(versionParts[2]) : 0; // e.g., 11

                // Compare major.minor.patch (e.g., 1.21.11 vs 1.17.1)
                if (currentMinor > minor) {
                    return true;
                } else if (currentMinor == minor) {
                    return currentPatch > patch;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Extracts and formats the NMS version string for reflection use.
     * @return The formatted NMS version string.
     */
    public static String getNmsVersion() {
        return getServerVersion();
    }
}
