package com.minecarts.portalforge.command;

import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.BasePortal;
import org.bukkit.entity.Player;

/**
 * Created by IntelliJ IDEA.
 * User: stephen
 * Date: 12/17/11
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortalEdit {
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        if(args.length != 2 ){ return false; }
        Integer portalId = Integer.parseInt(args[1]);
        plugin.setEditingPortalFromId(player,portalId);
        return true;
    }
}
