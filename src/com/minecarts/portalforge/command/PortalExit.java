package com.minecarts.portalforge.command;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.BasePortal;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PortalExit {
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        BasePortal portal = plugin.getEditingPortal(player);
        if(portal != null){
            portal.setExitLocation(player.getLocation());
            plugin.updatePortal(player,portal);
        }
        return true;
    }
}
