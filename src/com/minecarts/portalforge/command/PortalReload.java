package com.minecarts.portalforge.command;


import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.GenericPortal;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import org.bukkit.entity.Player;

public class PortalReload {
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        plugin.reloadConfig();
        return true;
    }
}
