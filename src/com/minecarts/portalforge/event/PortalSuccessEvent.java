package com.minecarts.portalforge.event;

import com.minecarts.portalforge.portal.GenericPortal;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PortalSuccessEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private GenericPortal portal;

    public PortalSuccessEvent(GenericPortal portal) {
        super("PortalSuccessEvent");
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
