package com.minecarts.portalforge.command;

import com.minecarts.portalforge.PortalForge;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalDone {
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        if(plugin.getEditingPortal(player) == null){
            player.sendMessage("You're currently not editing a portal.");
            return true;
        }
        plugin.clearEditingPortal(player);
        return true;
    }
}
