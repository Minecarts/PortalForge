package com.minecarts.portalforge.listener;

import com.minecarts.portalforge.event.PortalSuccessEvent;
import com.minecarts.portalforge.portal.GenericPortal;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import com.minecarts.portalforge.PortalForge;


public class PortalListener extends CustomEventListener{
    private PortalForge plugin;
    public PortalListener(PortalForge plugin){
        this.plugin = plugin;
    }

    @Override
    public void onCustomEvent(Event event){
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
