package com.minecarts.portalforge.command;


import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.BasePortal;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import com.minecarts.portalforge.portal.internal.PortalType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortalCreate{
    public static boolean handleCommand(PortalForge plugin,Player player, String[] args){
        //Attempt to create the portal
        try{
            BasePortal portal = new BasePortal();
            portal.setActivation(PortalActivation.INSTANT);
            portal.setType(PortalType.GENERIC);
            plugin.createPortal(player,portal);
        } catch (Exception e){
            //TODO: Only catch the exceptions that DBQuery can throw.. which ar.e. what? :(
            e.printStackTrace();
        }

        //Set their inventory to portal block
        return true;
    }
}
