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
    public HashMap<String, Location> entryPortalTracker = new HashMap<String, Location>();
    
    public PortalListener(PortalForge plugin){
        this.plugin = plugin;
    }
    
    @Override
    public void onCustomEvent(Event event){
        if(event.getEventName() == "PortalSuccessEvent"){
            PortalSuccessEvent e = (PortalSuccessEvent) event;
            Portal portal = e.getPortal();
            Entity entity = e.getEntity();
            
            if(e.isCancelled()) return;
            //Do the actual teleport next tick, we really dont HAVE to do this, but... it's safest?
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new teleportLater(plugin,entity,portal));

        }
    }
}
