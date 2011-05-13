package com.minecarts.portalforge.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.minecarts.portalforge.event.*;
import com.minecarts.portalforge.helper.teleportLater;
import com.minecarts.portalforge.portal.PortalType;
import com.minecarts.portalforge.portal.Portal;
import com.minecarts.portalforge.PortalForge;

public class PortalListener extends CustomEventListener{
    private PortalForge plugin;
    public PortalListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onCustomEvent(Event event){
        if(event.getEventName() == "PortalSuccessEvent"){
            PortalSuccessEvent e = (PortalSuccessEvent) event;
            Portal portal = e.getPortal();
            Entity entity = e.getEntity();
            
            if(portal.type == PortalType.GENERIC){
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));
            } else if (portal.type == PortalType.HOME){
                //Check if it's a player and send them to their home point if so
            } else if (portal.type == PortalType.NETHER){
                //TO THE NETHER!
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
