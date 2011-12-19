package com.minecarts.portalforge.command;


import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.GenericPortal;
import com.minecarts.portalforge.portal.internal.PortalActivation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PortalGoto {
    public static boolean handleCommand(final PortalForge plugin,final Player player, String[] args){
        final GenericPortal portal = plugin.getEditingPortal(player);
        if(portal == null){
            player.sendMessage("You must be editing a portal to goto it.");
            return true;
        }

        //Mark the player as portaling so we do not immediately trigger portal upon landing
        plugin.entityPortalingAdd(player, PortalActivation.INSTANT);

        //Update the portal to have a enter location
        plugin.setPortalEnterLocation(portal);

        //Teleport the player to the enter location, in 1 tick
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
            public void run() {
                player.teleport(portal.getEnterLocation());
            }
        },1); //Teleport delay ticks later

        //Clear the portaling 1 tick later
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
            public void run() {
                plugin.entityPortalingRemove(player, PortalActivation.INSTANT);
            }
        },20 * 5); //Reset portaling state 5 seconds later
        return true;
    }
}
