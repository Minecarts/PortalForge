package com.minecarts.portalforge.event;

import com.minecarts.portalforge.portal.GenericPortal;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PortalSuccessEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private GenericPortal portal;
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PortalSuccessEvent(GenericPortal portal) {
        this.portal = portal;
    }

    public GenericPortal getPortal(){
        return this.portal;
    }
    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
