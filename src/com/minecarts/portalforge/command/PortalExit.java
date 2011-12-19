package com.minecarts.portalforge.command;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.GenericPortal;
import org.bukkit.entity.Player;

public class PortalExit {
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        GenericPortal portal = plugin.getEditingPortal(player);
        if(portal != null){
            portal.setExitLocation(player.getLocation());
            plugin.updatePortal(player,portal);
        } else {
            player.sendMessage("You must be editing a portal to set an exit.");
        }
        return true;
    }
}
