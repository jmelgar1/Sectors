package me.jm3l.sectors.claim.commands;

import me.jm3l.sectors.claim.ClaimUtilities;
import me.jm3l.sectors.claim.wand.ClaimToolInventoryUtilities;
import me.jm3l.sectors.core.config.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.core.command.SubCommand;
import me.jm3l.sectors.sector.exceptions.NotInSector;
import me.jm3l.sectors.sector.Sector;
import me.jm3l.sectors.claim.Claim;
import me.jm3l.sectors.claim.ClaimSelection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ClaimCommand implements SubCommand {

    @Override
    public String getDescription() {
        return "Get a wand to make your land claim / finalize your sector claim";
    }

    @Override
    public String getPermission() {
        return "sec.player.claim";
    }

    @Override
    public void perform(Player p, String[] args, Sectors plugin) throws NotInSector {
        Sector s = plugin.getData().getSectorOrError(p);
        if (!s.getLeader().equals(p.getUniqueId())) {
            p.sendMessage(ConfigManager.MUST_BE_LEADER);
            return;
        }
        if (s.hasClaim()) {
            p.sendMessage(ConfigManager.ALREADY_HAS_CLAIM);
            return;
        }
        if (!plugin.getData().hasSelection(p)) {
            final TextComponent givenClaimTool = Component.text("You've been given a ").color(TextColor.color(0x64B5F6))
                .append(Component.text("Claim Tool!").color(TextColor.color(0x303F9F)));

            final TextComponent outline = Component.text("↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽↽");
            final TextComponent expandReach = Component.text("- [Scroll Up] :: ").append(Component.text("Expand Reach").color(TextColor.color(0x9CCC65)));
            final TextComponent reduceReach = Component.text("- [Scroll Down] :: ").append(Component.text("Reduce Reach").color(TextColor.color(0xE57373)));
            final TextComponent setPoint = Component.text("- [Left Click] :: ").append(Component.text("Set Point").color(TextColor.color(0x1976D2)));
            final TextComponent resetSelection = Component.text("- [").append(Component.keybind("key.swapOffhand")).append(Component.text("] :: ").append(Component.text("Reset Selection").color(TextColor.color(0xBA68C8))));

            final Component combinedMessage = Component.empty()
                .append(outline)
                .append(Component.newline())
                .append(givenClaimTool)
                .append(Component.newline())
                .append(expandReach)
                .append(Component.newline())
                .append(reduceReach)
                .append(Component.newline())
                .append(setPoint)
                .append(Component.newline())
                .append(resetSelection)
                .append(Component.newline())
                .append(outline);

            p.sendMessage(combinedMessage);
            ClaimToolInventoryUtilities.fillHotbarWithWand(p, plugin.getEvents().getSavedHotbars(), plugin);
            return;
        }

        ClaimSelection selection = plugin.getData().getSelection(p);
        Claim c = new Claim(selection, plugin);

        // claim min-width check
        if (c.getBounds().getWidthX() <= ConfigManager.MIN_CLAIM_WIDTH || c.getBounds().getWidthZ() <= ConfigManager.MIN_CLAIM_WIDTH) {
            p.sendMessage("x: " + c.getBounds().getWidthX());
            p.sendMessage("z: " + c.getBounds().getWidthZ());
            p.sendMessage("min claim width: " + ConfigManager.MIN_CLAIM_WIDTH);
            p.sendMessage(ConfigManager.CLAIM_TOO_NARROW);
            return;
        }

        // claim max-width check
        if (c.getBounds().getWidthX() >= ConfigManager.MAX_CLAIM_WIDTH || c.getBounds().getWidthZ() >= ConfigManager.MAX_CLAIM_WIDTH) {
            p.sendMessage("x: " + c.getBounds().getWidthX());
            p.sendMessage("z: " + c.getBounds().getWidthZ());
            p.sendMessage("max claim width: " + ConfigManager.MAX_CLAIM_WIDTH);
            p.sendMessage(ConfigManager.CLAIM_TOO_BIG);
            return;
        }

        // max-claim height check
        if (c.getBounds().getHeight() >= ConfigManager.MAX_CLAIM_HEIGHT) {
            p.sendMessage("y: " + c.getBounds().getHeight());
            p.sendMessage("max claim height: " + ConfigManager.MAX_CLAIM_HEIGHT);
            p.sendMessage(ConfigManager.CLAIM_TOO_BIG);
            return;
        }

        // min-claim height check
        if (c.getBounds().getHeight() <= ConfigManager.MIN_CLAIM_HEIGHT) {
            p.sendMessage("y: " + c.getBounds().getHeight());
            p.sendMessage("min claim height: " + ConfigManager.MIN_CLAIM_HEIGHT);
            p.sendMessage(ConfigManager.CLAIM_TOO_NARROW);
            return;
        }

        if (c.overlapsExisting()) {
            p.sendMessage(ConfigManager.LAND_ALREADY_CLAIMED);
            return;
        }

        s.setClaim(c);
        p.getInventory().remove(plugin.getWand());
        plugin.getData().getSelections().remove(p);

        // Initialize claim and set home
        boolean hasSolidBlocks = ClaimUtilities.hasSolidBlocks(c);
        if (!hasSolidBlocks) {
            p.sendMessage(Component.text("No solid blocks detected. Generating spawn platform...").color(TextColor.color(0x2196F3)));
        }

        Location homeLocation = ClaimUtilities.initializeClaimHome(c);
        if (homeLocation != null) {
            s.setHome(homeLocation);
            p.sendMessage(Component.text("Default home set!").color(TextColor.color(0x4CAF50)));
        }

        p.sendMessage(ConfigManager.SUCCESS);
    }
}
