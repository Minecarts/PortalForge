package com.minecarts.portalforge.listener;

import java.util.HashMap;

import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.BasePortal;
import com.minecarts.portalforge.portal.NetherPortal;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;
import org.bukkit.Location;

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
            if(e.isCancelled()) return;

            BasePortal portal = e.getPortal();
            portal.teleportEntity();
        }
    }
}
