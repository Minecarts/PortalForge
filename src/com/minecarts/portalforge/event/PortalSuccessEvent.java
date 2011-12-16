package com.minecarts.portalforge.event;

import com.minecarts.portalforge.portal.BasePortal;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PortalSuccessEvent extends Event implements Cancellable {
    private boolean cancel = false;
    private BasePortal portal;

    public PortalSuccessEvent(BasePortal portal) {
        super("PortalSuccessEvent");
        this.portal = portal;
    }

    public BasePortal getPortal(){
        return this.portal;
    }
    public boolean isCancelled() {
        return this.cancel;
    }

    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
