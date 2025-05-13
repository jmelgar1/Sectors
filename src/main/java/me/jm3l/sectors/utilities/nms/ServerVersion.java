package me.jm3l.sectors.utilities.nms;

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
        String version = getServerVersion();
        if (version.startsWith("v")) {
            String[] parts = version.split("_");
            try {
                if (parts.length >= 3) {
                    int currentMajor = Integer.parseInt(parts[1]);
                    int currentMinor = Integer.parseInt(parts[2].replaceAll("[^0-9]", ""));
                    if (currentMajor > major) {
                        return true;
                    } else if (currentMajor == major) {
                        return currentMinor > minor || (currentMinor == minor && patch == 0);
                    }
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
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
