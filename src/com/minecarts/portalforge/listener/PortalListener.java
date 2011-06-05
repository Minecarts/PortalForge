package com.minecarts.portalforge.listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.Location;

import com.minecarts.portalforge.event.*;
import com.minecarts.portalforge.helper.teleportLater;
import com.minecarts.portalforge.portal.PortalType;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.PortalForge;


public class PortalListener extends CustomEventListener{
    private PortalForge plugin;
    private HashMap<String, Location> entryPortalTracker = new HashMap<String, Location>();
    
    public PortalListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onCustomEvent(Event event){
        if(event.getEventName() == "PortalSuccessEvent"){
            PortalSuccessEvent e = (PortalSuccessEvent) event;
            Portal portal = e.getPortal().clone();
            Entity entity = e.getEntity();
           
            
            if(portal.type == PortalType.GENERIC){
                //Verify there is an endpoint for this generic
                if(portal.endPoint == null){
                    ((Player)entity).sendMessage("Portal is not linked. Portaling aborted.");
                    plugin.log("Portal " + portal.id + " is not linked. Aborted portal for " + entity);
                    return;
                }
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));
            } else if (portal.type == PortalType.HOME){
                //Check if it's a player and send them to their home point if so
                ((Player)entity).sendMessage("Home portals are not currently implemented. Sorry!");
            } else if (portal.type == PortalType.NETHER){
                if(!(entity instanceof Player)){ //Only players can portal to the nether
                    plugin.log("A non player entity tried portaling to the nether");
                    return;
                }
                Player player = (Player) entity;

                if(player.getLocation().getWorld().getName().equalsIgnoreCase("world_nether")){
                    //If they're in the nether, send them back to where they entered from
                    if(portal.endPoint == null){
                        if(this.entryPortalTracker.containsKey(player.getName())){
                            portal.endPoint = this.entryPortalTracker.remove(player.getName());
                            Location newLoc = plugin.findSafeExit(portal.endPoint); //Try to find a safe location near this portal
                            if(newLoc != null){
                                newLoc.setX(newLoc.getX() + 0.5);
                                newLoc.setY(newLoc.getY() + 1);
                                newLoc.setZ(newLoc.getZ() + 0.5);
                                portal.endPoint = newLoc;
                            }
                        } else {
                            portal.endPoint = Bukkit.getServer().getWorld("world").getSpawnLocation(); //Send them to the spawn
                        }
                    }
                } else {
                    //Else, send them to the spawn point in the nether, but log the portal they used to send them back
                    this.entryPortalTracker.put(player.getName(),entity.getLocation()); 
                    if(portal.endPoint == null){
                        portal.endPoint = Bukkit.getServer().getWorld("world_nether").getSpawnLocation();
                    }
                }

                //Do the actual teleport next tick, we really dont HAVE to do this, but... it's safest?
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));

            } else if (portal.type == PortalType.SKYLAND){
                if(portal.endPoint == null){
                    portal.endPoint = Bukkit.getServer().getWorld("world_skyland").getSpawnLocation();
                }
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));
            } else {
                if(entity instanceof Player){
                    plugin.log("ERROR: " + portal.id +" has unknown portal type: " + portal.type);
                    ((Player)entity).sendMessage("Unknown portal type: " + portal.type);
                }
            }
            //Handle it
        }
    }
}
