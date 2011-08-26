package com.minecarts.portalforge.listener;

import java.text.MessageFormat;

import com.minecarts.portalforge.portal.NetherPortal;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
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

            //Don't let items or monsters portal, just destory them
            if(entity instanceof ItemStack || entity instanceof Monster){
                entity.remove();
                return;
            }

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
                    plugin.finalizeAndFireEvent(entity, portal); //Portal should be a success!
                } else if(portal.activation == PortalActivation.DELAYED) {
                    //Do nothing, becuase this will be handled in the PlayerListener event, but we still want to clear portaling state here
                } else {
                    plugin.log("Unknown portal activation method for portal " + portal.id + ": " + portal.activation.name());
                }
                //Clear the portaling state after 5 seconds so they can use instant portals again
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new clearPortalingState(plugin,entity),20 * 5);
        } catch (Exception x){
            x.printStackTrace();
        }
        
        //e.setCancelled(true);
    }
}
