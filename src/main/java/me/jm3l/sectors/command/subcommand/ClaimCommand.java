package me.jm3l.sectors.command.subcommand;

import com.sk89q.worldedit.internal.annotation.Selection;
import me.jm3l.sectors.FileUtils.ConfigManager;
import me.jm3l.sectors.Sectors;
import me.jm3l.sectors.command.SubCommand;
import me.jm3l.sectors.exceptions.NotInSector;
import me.jm3l.sectors.objects.Claim;
import me.jm3l.sectors.objects.ClaimSelection;
import me.jm3l.sectors.objects.Sector;
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
            p.sendMessage(ConfigManager.TRIED_CLAIM_NO_SELECTION);
            p.getInventory().addItem(plugin.getWand());
            return;
        }

        ClaimSelection selection = plugin.getData().getSelection(p);
        Claim c = new Claim(selection, plugin);

        // claim min-width check
        if (c.getBounds().getWidthX() <= ConfigManager.MIN_CLAIM_WIDTH || c.getBounds().getWidthZ() <= ConfigManager.MIN_CLAIM_WIDTH) {
            p.sendMessage(ConfigManager.CLAIM_TOO_NARROW);
            return;
        }

        // claim max-width check
        if (c.getBounds().getWidthX() >= ConfigManager.MAX_CLAIM_WIDTH || c.getBounds().getWidthZ() >= ConfigManager.MAX_CLAIM_WIDTH) {
            p.sendMessage(ConfigManager.CLAIM_TOO_BIG);
            return;
        }

        // max-claim height check
        if (c.getBounds().getHeight() >= ConfigManager.MAX_CLAIM_HEIGHT) {
            p.sendMessage(ConfigManager.CLAIM_TOO_BIG);
            return;
        }

        // min-claim height check
        if (c.getBounds().getHeight() <= ConfigManager.MIN_CLAIM_HEIGHT) {
            p.sendMessage(ConfigManager.CLAIM_TOO_NARROW);
            return;
        }

        if (c.overlapsExisting()) {
            p.sendMessage(ConfigManager.LAND_ALREADY_CLAIMED);
            return;
        }

        s.setClaim(c);
        p.sendMessage(ConfigManager.SUCCESS);
    }
}
