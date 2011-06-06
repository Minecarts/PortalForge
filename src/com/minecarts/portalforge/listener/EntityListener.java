package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.minecarts.portalforge.event.*;
import com.minecarts.portalforge.PortalForge;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.portal.PortalActivation;
import com.minecarts.portalforge.portal.PortalType;
import com.minecarts.portalforge.helper.clearPortalingState;

public class EntityListener extends org.bukkit.event.entity.EntityListener{ 
    
    private PortalForge plugin;
    public EntityListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onEntityPortalEnter(EntityPortalEnterEvent e){
        //Fetch the portalId
        try{
            Entity entity = e.getEntity();
            if(plugin.entityPortaling.contains(entity)) return; //If they're already portaling, skip em.
            plugin.entityPortaling.add(entity);
            Location blockLocation = e.getLocation().getBlock().getLocation(); //maybe just e.getLocation()?
            Portal portal = plugin.dbHelper.getPortalFromBlockLocation(blockLocation);
                if(portal == null){
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,entity),200); //clear 10 seconds later
                    plugin.log(MessageFormat.format("{0} tried using a portal not in the DB at: {1}",entity,entity.getLocation()));
                    return;
                }
                
                if(entity instanceof Player){
                    String dest = "[No Destination]";
                    if(portal.endPoint != null){
                       dest = MessageFormat.format("({0},{1},{2})", portal.endPoint.getX(),portal.endPoint.getY(),portal.endPoint.getZ());
                    }
                    plugin.logDebug((Player)entity, MessageFormat.format("TOUCHED: [{0}] #{1} [{2}] {3}", 
                            portal.type.name(),
                            portal.id,
                            portal.activation,
                            dest));
                }

                if(portal.activation == PortalActivation.INSTANT){
                    //Portal should be a success!
                    plugin.finalizeAndFireEvent(entity, portal);
                } else if(portal.activation == PortalActivation.DELAYED) {
                    //And clear their state 5 seconds later, they should have portaled by then?
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,entity),20 * 5);
                } else {
                    plugin.log("Unknown portal activation method for portal " + portal.id + ": " + portal.activation.name());
                }
        } catch (Exception x){
            x.printStackTrace();
        }
        
        //e.setCancelled(true);
    }
}
