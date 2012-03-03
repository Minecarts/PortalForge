package com.minecarts.portalforge.listener;

import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.GenericPortal;

import com.minecarts.portalforge.PortalForge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


public class PortalListener implements Listener {
    private PortalForge plugin;
    public PortalListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCustomEvent(PortalSuccessEvent event){
        if(event.getEventName().equals("PortalSuccessEvent")){
            PortalSuccessEvent e = (PortalSuccessEvent) event;
            if(e.isCancelled()) return;

            GenericPortal portal = e.getPortal();

            if(portal.portalingEntityIsPlayer()){
                plugin.log(portal.getPortalingPlayer().getName() + " used " + portal.getType().name() + " portal " + portal.getId());
            } else {
                plugin.log(portal.getPortalingEntity().getClass().getName() + " used " + portal.getType().name() + " portal " + portal.getId());
            }

            portal.teleportEntity();
        }
    }
}
